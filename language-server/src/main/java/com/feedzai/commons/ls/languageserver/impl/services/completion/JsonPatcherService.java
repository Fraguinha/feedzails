/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.services.completion;

import com.feedzai.commons.ls.languageserver.api.CompletionService;
import com.feedzai.commons.ls.languageserver.impl.JsonPatch;
import com.feedzai.commons.ls.languageserver.impl.services.GitlabService;
import com.feedzai.commons.ls.languageserver.impl.services.MavenService;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.utils.SourceRoot;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Json Patcher Service.
 *
 * @since 0.1.0
 */
public final class JsonPatcherService extends AbstractCompletionService
    implements CompletionService {
  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(JsonPatcherService.class);

  /**
   * Constructor.
   *
   * @param cachePath The cache path.
   */
  public JsonPatcherService(String cachePath) {
    super(
        "676",
        cachePath,
        new MavenService("pom.xml"),
        new GitlabService("https://gitlab.feedzai.com"));

    this.parse();
  }

  /** Get the associated file extensions. */
  @Override
  public List<String> getAssociatedFileExtensions() {
    return List.of("json");
  }

  /** Parse the JSON patcher. */
  @Override
  public void parse() {
    try {
      final File[] jsonPatcherFiles =
          new File(this.cachingService.getCachePath().toUri()).listFiles();
      final String jsonPatcherSource =
          Arrays.stream(Objects.requireNonNull(jsonPatcherFiles))
              .filter(
                  file ->
                      file.getName().startsWith("json-patcher-" + this.getProjectVersion() + "-"))
              .findFirst()
              .orElseThrow()
              .toString();

      final SourceRoot sourceRoot =
          new SourceRoot(Paths.get(jsonPatcherSource, "json-patcher-lib"));

      sourceRoot.tryToParse();

      this.compilationUnits = sourceRoot.getCompilationUnits();

      final List<List<String>> patchInstructions =
          this.compilationUnits.stream()
              .flatMap(
                  compilation ->
                      compilation.findAll(ClassOrInterfaceDeclaration.class).stream()
                          .filter(
                              classOrInterfaceDeclaration ->
                                  classOrInterfaceDeclaration
                                      .getNameAsString()
                                      .equals("PathPatchInstruction")))
              .findFirst()
              .orElseThrow()
              .stream()
              .filter(NormalAnnotationExpr.class::isInstance)
              .map(NormalAnnotationExpr.class::cast)
              .filter(node -> node.getNameAsString().equals("JsonSubTypes.Type"))
              .map(
                  node ->
                      node.getPairs().stream()
                          .map(pair -> pair.getValue().toString())
                          .collect(Collectors.toList()))
              .collect(Collectors.toList());

      for (List<String> patchInstruction : patchInstructions) {
        final String clazz = patchInstruction.get(0).replace("Immutable", "").replace(".class", "");
        final String op = patchInstruction.get(1).replace("\"", "");

        final JsonPatch patch = new JsonPatch(op, findFields(clazz));

        this.addCompletionItem(op, op + " patch", patch.toSnippet());
      }
    } catch (IOException e) {
      this.logger.error("Error parsing json-patcher", e);
    }
  }

  /** Find the fields of a patch. */
  private List<String> findFields(String clazz) {
    final List<String> fields = new ArrayList<>();

    final ClassOrInterfaceDeclaration interfaceDeclaration =
        this.compilationUnits.stream()
            .flatMap(
                compilation ->
                    compilation.findAll(ClassOrInterfaceDeclaration.class).stream()
                        .filter(
                            classOrInterfaceDeclaration ->
                                classOrInterfaceDeclaration.getNameAsString().equals(clazz)))
            .findFirst()
            .orElseThrow();

    interfaceDeclaration.stream()
        .filter(MethodDeclaration.class::isInstance)
        .map(MethodDeclaration.class::cast)
        .filter(methodDeclaration -> methodDeclaration.getParameters().isEmpty())
        .filter(
            methodDeclaration ->
                methodDeclaration.stream()
                    .filter(SingleMemberAnnotationExpr.class::isInstance)
                    .map(SingleMemberAnnotationExpr.class::cast)
                    .noneMatch(node -> node.getNameAsString().equals("JsonIgnore")))
        .forEach(
            methodDeclaration -> {
              final String field = methodDeclaration.getNameAsString();
              try {
                final SingleMemberAnnotationExpr property =
                    methodDeclaration.stream()
                        .filter(SingleMemberAnnotationExpr.class::isInstance)
                        .map(SingleMemberAnnotationExpr.class::cast)
                        .filter(node -> node.getNameAsString().equals("JsonProperty"))
                        .findFirst()
                        .orElseThrow();
                fields.add(property.getMemberValue().toString().replace("\"", ""));
              } catch (Exception e) {
                fields.add(field);
              }
            });

    if (!clazz.equals("PathPatchInstruction")) {
      final String extendedInterface =
          interfaceDeclaration.getExtendedTypes().get(0).getNameAsString();

      final List<String> newFields = findFields(extendedInterface);
      fields.addAll(
          newFields.stream().filter(field -> !fields.contains(field)).collect(Collectors.toList()));
    }

    return fields;
  }
}
