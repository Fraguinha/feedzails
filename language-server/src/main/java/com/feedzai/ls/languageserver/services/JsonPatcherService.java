/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.ls.languageserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.utils.SourceRoot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Json Patcher Service.
 *
 * @since 0.1.0
 */
public final class JsonPatcherService {

  private final String projectId;

  /** The version. */
  private final String version;

  /** The JSON patcher path. */
  private final String jsonPatcherPath;

  /** The compilation units. */
  private List<CompilationUnit> compilationUnits;

  /** The completion items. */
  private final List<CompletionItem> completionItems = new ArrayList<>();

  /** Logger for this class. */
  Logger logger = LoggerFactory.getLogger(JsonPatcherService.class);

  /**
   * Constructor.
   *
   * @param version The version.
   * @param cachePath The cache path.
   */
  public JsonPatcherService(String version, String cachePath) {
    this.projectId = "676";
    this.version = version != null ? version : getLatestVersion();
    this.jsonPatcherPath = cachePath + this.version;

    setupCache();

    parseJsonPatcher();
  }

  /** Get the latest version of JSON patcher. */
  private String getLatestVersion() {
    final String jsonPatcherProjectUrl =
        String.format(
            "https://gitlab.feedzai.com/api/v4/projects/%s/repository/tags", this.projectId);

    try {
      final HttpClient client = HttpClient.newHttpClient();
      final HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(jsonPatcherProjectUrl))
              .GET()
              .setHeader("PRIVATE-TOKEN", System.getenv("GITLAB_TOKEN"))
              .build();

      final HttpResponse<InputStream> response =
          client.send(request, HttpResponse.BodyHandlers.ofInputStream());

      final ObjectMapper objectMapper = new ObjectMapper();
      final List<Map<String, Object>> tags =
          objectMapper.readValue(response.body(), new TypeReference<>() {});

      return (String) tags.get(0).get("name");
    } catch (IOException | InterruptedException e) {
      this.logger.error("Error getting latest tag", e);
    }

    return null;
  }

  /** Set up the JSON patcher cache. */
  private void setupCache() {
    final File jsonPatcherFile = new File(this.jsonPatcherPath);

    if (!jsonPatcherFile.exists() && this.version != null) {
      try {
        Files.createDirectories(Paths.get(jsonPatcherPath));

        downloadJsonPatcherZip();

        extractJsonPatcherZip();
      } catch (IOException e) {
        this.logger.error("Error creating JSON patcher cache directory", e);
      }
    }
  }

  /** Download the JSON patcher. */
  private void downloadJsonPatcherZip() {
    final String jsonPatcherProjectUrl =
        String.format(
            "https://gitlab.feedzai.com/api/v4/projects/%s/repository/archive.zip?sha=%s",
            this.projectId, this.version);

    try {
      final HttpClient client = HttpClient.newHttpClient();
      final HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(jsonPatcherProjectUrl))
              .GET()
              .setHeader("PRIVATE-TOKEN", System.getenv("GITLAB_TOKEN"))
              .build();

      final HttpResponse<InputStream> response =
          client.send(request, HttpResponse.BodyHandlers.ofInputStream());

      final Path destination = Paths.get(this.jsonPatcherPath, "json-patcher.zip");
      Files.copy(response.body(), destination, StandardCopyOption.REPLACE_EXISTING);

      response.body().close();
    } catch (IOException | InterruptedException e) {
      this.logger.error("Error downloading json-patcher", e);
    }
  }

  /** Extract the JSON patcher. */
  private void extractJsonPatcherZip() {
    final Path jsonPatcherZip = Paths.get(this.jsonPatcherPath, "json-patcher.zip");

    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(jsonPatcherZip))) {
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        final String filename = zipEntry.getName();
        final Path filepath = Paths.get(this.jsonPatcherPath, filename);

        if (zipEntry.isDirectory()) {
          Files.createDirectories(filepath);
        } else {
          Files.copy(zis, filepath, StandardCopyOption.REPLACE_EXISTING);
        }

        zipEntry = zis.getNextEntry();
      }
    } catch (IOException e) {
      this.logger.error("Error extracting json-patcher", e);
    }
  }

  /** Parse the JSON patcher. */
  public void parseJsonPatcher() {
    try {
      final File[] jsonPatcherFiles = new File(this.jsonPatcherPath).listFiles();
      final String jsonPatcherSource =
          Arrays.stream(Objects.requireNonNull(jsonPatcherFiles))
              .filter(file -> file.getName().startsWith("json-patcher-" + this.version + "-"))
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

        final Patch patch = new Patch(op, findFields(clazz));

        addCompletionItem(
            CompletionItemKind.Snippet,
            op,
            op + " patch",
            patch.toSnippet(),
            InsertTextFormat.Snippet);
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

  /** Add the completion items. */
  private void addCompletionItem(
      CompletionItemKind kind,
      String label,
      String detail,
      String insertText,
      InsertTextFormat format) {
    CompletionItem completionItem = new CompletionItem();

    completionItem.setKind(kind);
    completionItem.setLabel(label);
    completionItem.setDetail(detail);
    completionItem.setInsertText(insertText);
    completionItem.setInsertTextFormat(format);

    this.completionItems.add(completionItem);
  }

  /**
   * Get the JSON patcher completion items.
   *
   * @return the JSON patcher completion items.
   */
  public List<CompletionItem> getJsonPatcherCompletionItems() {
    return this.completionItems;
  }
}
