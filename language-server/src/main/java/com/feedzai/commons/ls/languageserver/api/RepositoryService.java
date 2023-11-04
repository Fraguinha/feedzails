/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.api;

import java.io.InputStream;

/**
 * Repository Service.
 *
 * @since 0.1.0
 */
public interface RepositoryService {
  /**
   * Get the repository.
   *
   * @param id the id.
   * @return the repository.
   */
  InputStream getRepository(String id, String version);

  /**
   * Get the latest version.
   *
   * @param id the id.
   * @return the latest version.
   */
  String getLatestVersion(String id);
}
