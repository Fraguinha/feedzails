/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl;

import java.util.List;

/**
 * Patch.
 *
 * @since 0.1.0
 */
public class JsonPatch {
  /** patch name. */
  private final String name;

  /** patch fields. */
  private final List<String> fields;

  /** Constructor. */
  public JsonPatch(String name, List<String> fields) {
    this.name = name;
    this.fields = fields;
  }

  /** Get patch snippet. */
  public String toSnippet() {
    final StringBuilder patch = new StringBuilder();

    patch.append("{\n");
    patch.append(String.format("\t\"op\": \"%s\",%n", name));

    for (int i = 0; i < fields.size(); i++) {
      patch.append(String.format("\t\"%s\": \"$%d\"", this.fields.get(i), i + 1));
      if (i < fields.size() - 1) {
        patch.append(",\n");
      } else {
        patch.append("\n");
      }
    }

    patch.append("}");

    return patch.toString();
  }
}
