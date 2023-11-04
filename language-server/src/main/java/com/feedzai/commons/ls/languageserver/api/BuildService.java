/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.api;

import java.util.Optional;

/**
 * Build Service.
 *
 * @since 0.1.0
 */
public interface BuildService {
  /**
   * Get the artifact version.
   *
   * @param groupId The group id.
   * @param artifactId The artifact id.
   * @return The artifact version.
   */
  Optional<String> getVersion(final String groupId, final String artifactId);
}
