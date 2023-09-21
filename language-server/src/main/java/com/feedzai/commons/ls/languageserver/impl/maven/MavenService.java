/*
 * The copyright of this file belongs to Feedzai. The file cannot be reproduced in whole or in part,
 * stored in a retrieval system, transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.maven;

import com.feedzai.commons.ls.languageserver.api.BuildService;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Maven Service.
 *
 * @since 0.1.0
 */
public final class MavenService implements BuildService {
  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(MavenService.class);

  /**
   * Get the artifact version based on groupId and artifactId.
   *
   * @param groupId the group ID of the artifact.
   * @param artifactId the artifact ID.
   * @return the artifact version.
   */
  public Optional<String> getVersion(final String groupId, final String artifactId) {
    final String home = System.getProperty("user.home");
    final File m2 = new File(home, ".m2/repository");

    final File artifactDirectory = new File(m2, groupId.replace('.', '/') + "/" + artifactId);

    if (!artifactDirectory.exists()) {
      logger.warn("Artifact directory not found: {}", artifactDirectory);
      return Optional.empty();
    }

    final File[] versionDirectories = artifactDirectory.listFiles(File::isDirectory);

    Arrays.sort(
        versionDirectories, Comparator.comparing(File::getName, MavenService::versionCompare));

    if (versionDirectories.length > 0) {
      return Optional.of(versionDirectories[versionDirectories.length - 1].getName());
    } else {
      logger.warn("No versions found for artifact: {}", artifactDirectory);
      return Optional.empty();
    }
  }

  /**
   * Compare two version strings.
   *
   * @param v1 Version 1
   * @param v2 Version 2
   * @return A negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second.
   */
  private static int versionCompare(String v1, String v2) {
    String[] parts1 = v1.split("\\.");
    String[] parts2 = v2.split("\\.");

    return Arrays.compare(
        Arrays.stream(parts1).mapToInt(Integer::parseInt).toArray(),
        Arrays.stream(parts2).mapToInt(Integer::parseInt).toArray());
  }
}
