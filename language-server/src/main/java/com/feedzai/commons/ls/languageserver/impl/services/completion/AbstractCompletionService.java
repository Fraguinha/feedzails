/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.services.completion;

import com.feedzai.commons.ls.languageserver.api.BuildService;
import com.feedzai.commons.ls.languageserver.api.CachingService;
import com.feedzai.commons.ls.languageserver.api.CompletionService;
import com.feedzai.commons.ls.languageserver.api.RepositoryService;
import com.feedzai.commons.ls.languageserver.impl.services.CacheService;
import com.github.javaparser.ast.CompilationUnit;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Completion Service.
 *
 * @since 0.1.0
 */
public abstract class AbstractCompletionService implements CompletionService {
  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(AbstractCompletionService.class);

  /** The build service. */
  protected final BuildService buildService;

  /** The repository service. */
  protected final RepositoryService repositoryService;

  /** The caching service. */
  protected final CachingService cachingService;

  /** The project id. */
  @Getter private final String projectId;

  /** The version. */
  @Getter private final String projectVersion;

  /** The completion items. */
  @Getter private final List<CompletionItem> completionItems = new ArrayList<>();

  /** The compilation units. */
  protected List<CompilationUnit> compilationUnits;

  /** Constructor. */
  public AbstractCompletionService(
      String projectId,
      String cachePath,
      BuildService buildService,
      RepositoryService repositoryService) {
    this.projectId = projectId;
    this.projectVersion =
        buildService
            .getVersion("com.feedzai.commons.json-patcher", "json-patcher-maven-plugin")
            .orElse(repositoryService.getLatestVersion(this.projectId));
    this.buildService = buildService;
    this.repositoryService = repositoryService;
    this.cachingService =
        new CacheService(this, repositoryService, Path.of(cachePath, this.projectVersion));
  }

  /** Parse the project. */
  public abstract void parse();

  /** Add the completion items. */
  protected void addCompletionItem(String label, String detail, String insertText) {
    final CompletionItem completionItem = new CompletionItem();

    completionItem.setKind(CompletionItemKind.Snippet);
    completionItem.setLabel(label);
    completionItem.setDetail(detail);
    completionItem.setInsertText(insertText);
    completionItem.setInsertTextFormat(InsertTextFormat.Snippet);

    this.completionItems.add(completionItem);
  }
}
