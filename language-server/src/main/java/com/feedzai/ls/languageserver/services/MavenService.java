/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.ls.languageserver.services;

import java.io.FileReader;
import java.io.IOException;
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
public final class MavenService {
  /** Logger for this class. */
  Logger logger = LoggerFactory.getLogger(MavenService.class);

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
   * Read a property from the pom.xml file.
   *
   * @param property the property to read.
   * @return the property value.
   */
  public String readProperty(final String property) {
    return this.mavenProject.getModel().getProperties().getProperty(property);
  }
}
