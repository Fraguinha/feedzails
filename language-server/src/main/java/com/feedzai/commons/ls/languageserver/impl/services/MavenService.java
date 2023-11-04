/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.services;

import com.feedzai.commons.ls.languageserver.api.BuildService;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

  /** Maven project. */
  private MavenProject mavenProject;

  /** Constructor. */
  public MavenService(String pom) {
    try {
      this.mavenProject = new MavenProject(new MavenXpp3Reader().read(new FileReader(pom)));
    } catch (IOException | XmlPullParserException e) {
      this.logger.error("Error reading pom.xml file", e);
    }
  }

  /**
   * Get the artifact version based on groupId and artifactId.
   *
   * @param groupId the group ID of the artifact
   * @param artifactId the artifact ID
   * @return the artifact version.
   */
  public Optional<String> getVersion(final String groupId, final String artifactId) {
    return this.mavenProject.getModel().getBuild().getPluginManagement().getPlugins().stream()
        .filter(
            plugin ->
                plugin.getGroupId().equals(groupId) && plugin.getArtifactId().equals(artifactId))
        .findFirst()
        .map(Plugin::getVersion)
        .map(
            version ->
                this.mavenProject
                    .getProperties()
                    .getProperty(version.substring(2, version.length() - 1), version));
  }
}
