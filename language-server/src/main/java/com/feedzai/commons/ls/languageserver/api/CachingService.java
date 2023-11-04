/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.api;

import java.nio.file.Path;

/**
 * Caching Service.
 *
 * @since 0.1.0
 */
public interface CachingService {
  /** Cache the repository. */
  void cacheRepository();

  /**
   * Get the cache path.
   *
   * @return the cache path.
   */
  Path getCachePath();
}
