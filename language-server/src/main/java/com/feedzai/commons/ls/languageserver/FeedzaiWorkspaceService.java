/*
 * The copyright of this file belongs to Feedzai. The file cannot be reproduced in whole or in part,
 * stored in a retrieval system, transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * The Feedzai Workspace Service.
 *
 * @since 0.1.0
 */
public final class FeedzaiWorkspaceService implements org.eclipse.lsp4j.services.WorkspaceService {
  @Override
  public CompletableFuture<
          Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>>
      symbol(final WorkspaceSymbolParams workspaceSymbolParams) {
    return null;
  }

  @Override
  public void didChangeConfiguration(
      final DidChangeConfigurationParams didChangeConfigurationParams) {}

  @Override
  public void didChangeWatchedFiles(
      final DidChangeWatchedFilesParams didChangeWatchedFilesParams) {}
}
