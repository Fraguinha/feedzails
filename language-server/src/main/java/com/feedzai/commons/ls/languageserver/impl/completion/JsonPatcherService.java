/*
 * The copyright of this file belongs to Feedzai. The file cannot be reproduced in whole or in part,
 * stored in a retrieval system, transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.completion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.feedzai.commons.ls.languageserver.impl.completion.patches.JsonPatch;
import com.feedzai.commons.ls.languageserver.impl.maven.MavenService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Json Patcher Service.
 *
 * @since 0.1.0
 */
public final class JsonPatcherService extends AbstractCompletionService {
  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(JsonPatcherService.class);

  /** Constructor. */
  public JsonPatcherService() {
    super(
        "json-patcher", "com.feedzai.commons.json-patcher", "json-patcher-lib", new MavenService());

    this.process();
  }

  /** Get the associated file extensions. */
  @Override
  public List<String> getAssociatedFileExtensions() {
    return List.of("json");
  }

  /** Process the completion items. */
  @Override
  public void process() {
    final String jarFilePath =
        String.format(
            "%s/.m2/repository/%s/%s/%4$s/json-patcher-lib-%4$s.jar",
            System.getenv("HOME"),
            "com/feedzai/commons",
            "json-patcher/json-patcher-lib",
            this.getVersion());

    final List<JsonPatch> patches = createPatches(getClasses(jarFilePath));

    patches.forEach(
        patch -> this.addCompletionItem(patch.getLabel(), patch.getDetail(), patch.toSnippet()));
  }

  /**
   * Get the patch instructions.
   *
   * @param jarFile The JAR file path.
   * @return The patch instructions.
   */
  private Stream<Class<?>> getClasses(String jarFile) {
    return loadClassesFromJar(jarFile).stream()
        .filter(cls -> cls.getName().contains("Immutable") && !cls.getName().contains("$"));
  }

  /**
   * Create the patches.
   *
   * @param classes The classes.
   * @return The patches.
   */
  private List<JsonPatch> createPatches(Stream<Class<?>> classes) {
    return classes
        .map(
            cls -> {
              try {
                JsonSchema schema = new JsonSchemaGenerator(new ObjectMapper()).generateSchema(cls);
                String name = cls.getName();
                String label = getLabel(name);
                String detail = getDetail(name);
                return new JsonPatch(label, detail, schema);
              } catch (Exception e) {
                logger.error("Error generating JSON schema", e);
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Get the label.
   *
   * @param name The name.
   * @return The label.
   */
  private String getLabel(String name) {
    if (name.contains("instruction")) {
      return name.replace("com.feedzai.commons.json.patch.instruction.impl.Immutable", "")
          .replace("PathPatchInstruction", "")
          .concat(" patch");
    }

    if (name.contains("conditional")) {
      return name.replace("com.feedzai.commons.json.patch.conditional.impl.Immutable", "")
          .concat(" condition");
    }

    return "";
  }

  /**
   * Get the detail.
   *
   * @param name The name.
   * @return The detail.
   */
  private String getDetail(String name) {
    if (name.contains("instruction")) {
      return name.replace("com.feedzai.commons.json.patch.instruction.impl.Immutable", "");
    }

    if (name.contains("conditional")) {
      return name.replace("com.feedzai.commons.json.patch.conditional.impl.Immutable", "");
    }

    return "";
  }
}
