/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.api;

import java.util.List;
import org.eclipse.lsp4j.CompletionItem;

/**
 * Completion Service.
 *
 * @since 0.1.0
 */
public interface CompletionService {
  /**
   * Get the project id.
   *
   * @return the project id.
   */
  String getProjectId();

  /**
   * Get the project version.
   *
   * @return the project version.
   */
  String getProjectVersion();

  /**
   * Get the completions for a given service.
   *
   * @return the completion.
   */
  List<CompletionItem> getCompletionItems();

  /**
   * Get the associated file extensions.
   *
   * @return the associated file extensions.
   */
  List<String> getAssociatedFileExtensions();
}
