/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.completion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.feedzai.commons.ls.languageserver.api.CompletionService;
import com.feedzai.commons.ls.languageserver.impl.MavenService;
import com.feedzai.commons.ls.languageserver.impl.completion.patches.JsonPatch;
import java.util.ArrayList;
import java.util.List;
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

  /** Constructor. */
  public JsonPatcherService() {
    super(
        "json-patcher",
        "com.feedzai.commons.json-patcher",
        "json-patcher-maven-plugin",
        new MavenService("pom.xml"));

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

    for (JsonPatch patch : patches) {
      this.addCompletionItem(patch.getLabel(), patch.getDetail(), patch.toSnippet());
    }
  }

  /**
   * Get the patch instructions.
   *
   * @param jarFile The JAR file path.
   * @return The patch instructions.
   */
  private List<Class<?>> getClasses(String jarFile) {
    final List<Class<?>> classes = loadClassesFromJar(jarFile);

    final List<Class<?>> filteredClasses = new ArrayList<>();
    for (Class<?> cls : classes) {
      if (cls.getName().contains("Immutable") && !cls.getName().contains("$")) {
        filteredClasses.add(cls);
      }
    }

    return filteredClasses;
  }

  /**
   * Create the patches.
   *
   * @param classes The classes.
   * @return The patches.
   */
  private List<JsonPatch> createPatches(List<Class<?>> classes) {
    final List<JsonPatch> patches = new ArrayList<>();

    for (Class<?> cls : classes) {
      try {
        final JsonSchema schema = new JsonSchemaGenerator(new ObjectMapper()).generateSchema(cls);

        final String name = cls.getName();
        final String label = getLabel(name);
        final String detail = getDetail(name);

        patches.add(new JsonPatch(label, detail, schema));
      } catch (Exception e) {
        this.logger.error("Error generating JSON schema", e);
      }
    }

    return patches;
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
      return name.replace("com.feedzai.commons.json.patch.instruction.impl.", "");
    }

    if (name.contains("conditional")) {
      return name.replace("com.feedzai.commons.json.patch.conditional.impl.", "");
    }

    return "";
  }
}
