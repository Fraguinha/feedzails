/*
 * The copyright of this file belongs to Feedzai. The file cannot be reproduced in whole or in part,
 * stored in a retrieval system, transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.completion.patches;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import java.util.Iterator;
import java.util.Map;
import java.util.PrimitiveIterator;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Json patch.
 *
 * @since 0.1.0
 */
public final class JsonPatch {
  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(JsonPatch.class);

  /** patch label. */
  @Getter private final String label;

  /** patch detail. */
  @Getter private final String detail;

  /** patch fields. */
  @Getter private final JsonSchema schema;

  /** patch placeholder. */
  private int placeholder = 1;

  /** Constructor. */
  public JsonPatch(String label, String detail, JsonSchema schema) {
    this.label = label;
    this.detail = detail;
    this.schema = schema;
  }

  /** Get patch snippet. */
  public String toSnippet() {
    return getSnippetRecursively(this.schema, new StringBuilder(), 0);
  }

  /** Get patch snippet recursively. */
  private String getSnippetRecursively(
      JsonSchema schema, StringBuilder stringBuilder, int indentation) {
    if (schema.isObjectSchema()) {
      handleObjectSchema(schema, stringBuilder, indentation);
    } else if (schema.isArraySchema()) {
      stringBuilder.append("[");
      stringBuilder.append(String.format("$%d", this.placeholder++));
      stringBuilder.append("]");
    } else if (schema.isStringSchema()) {
      stringBuilder.append("\"");
      stringBuilder.append(String.format("$%d", this.placeholder++));
      stringBuilder.append("\"");
    } else {
      stringBuilder.append(String.format("$%d", this.placeholder++));
    }

    return stringBuilder.toString();
  }

  /**
   * Handle object schema.
   *
   * @param schema schema.
   * @param stringBuilder string builder.
   * @param indentation indentation.
   */
  private void handleObjectSchema(JsonSchema schema, StringBuilder stringBuilder, int indentation) {
    stringBuilder.append("{\n");

    if (indentation == 0) {
      indent(stringBuilder, indentation + 1);
      if (this.label.contains("patch")) {
        stringBuilder.append(
            String.format("\"op\": \"%s\",%n", this.label.split(" ")[0].toLowerCase()));
      }
      if (this.label.contains("condition")) {
        stringBuilder.append(
            String.format("\"operator\": \"%s\",%n", toUpperSnakeCase(this.label.split(" ")[0])));
      }
    }

    Map<String, JsonSchema> properties = schema.asObjectSchema().getProperties();
    Iterator<String> iterator = properties.keySet().iterator();
    while (iterator.hasNext()) {
      final String key = iterator.next();
      final JsonSchema value = properties.get(key);

      final String snippet = getSnippetRecursively(value, new StringBuilder(), indentation + 1);

      indent(stringBuilder, indentation + 1);
      if (snippet.equals(String.format("{%n%s}", "\t".repeat(indentation + 1)))) {
        stringBuilder.append(String.format("\"%s\": {$%d}", key, this.placeholder++));
      } else {
        stringBuilder.append(String.format("\"%s\": %s", key, snippet));
      }

      if (iterator.hasNext()) {
        stringBuilder.append(",");
      }

      stringBuilder.append("\n");
    }

    indent(stringBuilder, indentation);
    stringBuilder.append("}");
  }

  /**
   * Indent the snippet.
   *
   * @param stringBuilder string builder.
   * @param indentation indentation.
   */
  private void indent(StringBuilder stringBuilder, int indentation) {
    if (indentation > 0) {
      stringBuilder.append("\t".repeat(indentation));
    }
  }

  /**
   * Convert to upper snake case.
   *
   * @param input input string.
   * @return upper snake case string.
   */
  private String toUpperSnakeCase(String input) {
    final StringBuilder stringBuilder = new StringBuilder();

    PrimitiveIterator.OfInt characters = input.chars().iterator();
    while (characters.hasNext()) {
      final int c = characters.next();

      if (Character.isUpperCase(c) && stringBuilder.length() > 0) {
        stringBuilder.append("_");
      }

      stringBuilder.append(Character.toUpperCase((char) c));
    }

    return stringBuilder.toString();
  }
}
