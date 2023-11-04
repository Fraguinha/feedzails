/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver;

import com.feedzai.commons.ls.languageserver.api.CompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * The Feedzai Text Document Service.
 *
 * @since 0.1.0
 */
public final class FeedzaiTextDocumentService
    implements org.eclipse.lsp4j.services.TextDocumentService {
  /** The JSON patcher service. */
  final List<CompletionService> completionServices;

  /**
   * Constructor.
   *
   * @param completionServices The completion services.
   */
  public FeedzaiTextDocumentService(List<CompletionService> completionServices) {
    this.completionServices = completionServices;
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      final CompletionParams completionParams) {
    return CompletableFuture.supplyAsync(
        () -> {
          final List<CompletionItem> completionItems = new ArrayList<>();

          this.completionServices.stream()
              .filter(
                  completionService ->
                      completionService.getAssociatedFileExtensions().stream()
                          .anyMatch(
                              extension ->
                                  completionParams.getTextDocument().getUri().endsWith(extension)))
              .forEach(
                  completionService ->
                      completionItems.addAll(completionService.getCompletionItems()));

          return Either.forLeft(completionItems);
        });
  }

  @Override
  public CompletableFuture<CompletionItem> resolveCompletionItem(
      final CompletionItem completionItem) {
    return null;
  }

  @Override
  public CompletableFuture<Hover> hover(final HoverParams params) {
    return null;
  }

  @Override
  public CompletableFuture<SignatureHelp> signatureHelp(final SignatureHelpParams params) {
    return null;
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
      definition(final DefinitionParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(
      final ReferenceParams referenceParams) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
      final DocumentHighlightParams params) {
    return null;
  }

  @Override
  public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
      final DocumentSymbolParams documentSymbolParams) {
    return null;
  }

  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
      final CodeActionParams codeActionParams) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends CodeLens>> codeLens(final CodeLensParams codeLensParams) {
    return null;
  }

  @Override
  public CompletableFuture<CodeLens> resolveCodeLens(final CodeLens codeLens) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> formatting(
      final DocumentFormattingParams documentFormattingParams) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> rangeFormatting(
      final DocumentRangeFormattingParams documentRangeFormattingParams) {
    return null;
  }

  @Override
  public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(
      final DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
    return null;
  }

  @Override
  public CompletableFuture<WorkspaceEdit> rename(final RenameParams renameParams) {
    return null;
  }

  @Override
  public void didOpen(final DidOpenTextDocumentParams didOpenTextDocumentParams) {}

  @Override
  public void didChange(final DidChangeTextDocumentParams didChangeTextDocumentParams) {}

  @Override
  public void didClose(final DidCloseTextDocumentParams didCloseTextDocumentParams) {}

  @Override
  public void didSave(final DidSaveTextDocumentParams didSaveTextDocumentParams) {}
}
