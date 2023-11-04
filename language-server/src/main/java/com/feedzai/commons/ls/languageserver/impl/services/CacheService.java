/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.services;

import com.feedzai.commons.ls.languageserver.api.CachingService;
import com.feedzai.commons.ls.languageserver.api.CompletionService;
import com.feedzai.commons.ls.languageserver.api.RepositoryService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Cache Service.
 *
 * @since 0.1.0
 */
public class CacheService implements CachingService {
  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(CacheService.class);

  /** Repository service. */
  private final RepositoryService repositoryService;

  /** Completion service. */
  private final CompletionService completionService;

  /** Cache path. */
  @Getter private final Path cachePath;

  /**
   * Constructor.
   *
   * @param completionService The completion service.
   * @param repositoryService The repository service.
   * @param cachePath The cache path.
   */
  public CacheService(
      CompletionService completionService, RepositoryService repositoryService, Path cachePath) {
    this.repositoryService = repositoryService;
    this.completionService = completionService;
    this.cachePath = cachePath;

    this.cacheRepository();
  }

  /** Cache the repository. */
  @Override
  public void cacheRepository() {
    try {
      Files.createDirectories(this.cachePath);

      final InputStream repository =
          this.repositoryService.getRepository(
              this.completionService.getProjectId(), this.completionService.getProjectVersion());

      final Path destination = Paths.get(this.cachePath.toString(), "json-patcher.zip");

      Files.copy(repository, destination, StandardCopyOption.REPLACE_EXISTING);

      this.extractJsonPatcherZip();

      repository.close();
    } catch (IOException e) {
      logger.error("Error saving json-patcher to cache", e);
    }
  }

  /** Extract the JSON patcher. */
  private void extractJsonPatcherZip() {
    final Path jsonPatcherZip = Paths.get(String.valueOf(this.cachePath), "json-patcher.zip");

    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(jsonPatcherZip))) {
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        final String filename = zipEntry.getName();
        final Path filepath = Paths.get(String.valueOf(this.cachePath), filename);

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
}
