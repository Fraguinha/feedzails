/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.ls.languageserver;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * The Feedzai Language Server.
 *
 * @since 0.1.0
 */
public final class FeedzaiLanguageServer implements LanguageServer, LanguageClientAware {
  /** The text document service. */
  private final org.eclipse.lsp4j.services.TextDocumentService textDocumentService;

  /** The workspace service. */
  private final org.eclipse.lsp4j.services.WorkspaceService workspaceService;

  /** The client. */
  private LanguageClient client;

  /** The error code. */
  private int errorCode = 1;

  /** Constructor. */
  public FeedzaiLanguageServer() {
    this.textDocumentService = new FeedzaiTextDocumentService();
    this.workspaceService = new FeedzaiWorkspaceService();
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(final InitializeParams initializeParams) {
    final InitializeResult initializeResult = new InitializeResult(new ServerCapabilities());

    initializeResult.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
    initializeResult.getCapabilities().setCompletionProvider(new CompletionOptions());

    return CompletableFuture.supplyAsync(() -> initializeResult);
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    errorCode = 0;

    return null;
  }

  @Override
  public void exit() {
    System.exit(errorCode);
  }

  @Override
  public org.eclipse.lsp4j.services.TextDocumentService getTextDocumentService() {
    return this.textDocumentService;
  }

  @Override
  public org.eclipse.lsp4j.services.WorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  @Override
  public void connect(final LanguageClient languageClient) {
    this.client = languageClient;
  }
}
