/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedzai.commons.ls.languageserver.api.RepositoryService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Gitlab Service.
 *
 * @since 0.1.0
 */
public class GitlabService implements RepositoryService {
  /** Error message. */
  private static final String ERROR_MESSAGE = "Error downloading json-patcher";

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(GitlabService.class);

  /** Gitlab url. */
  private final String gitlabUrl;

  /** Gitlab token. */
  private final String gitlabToken;

  /** Constructor. */
  public GitlabService(String gitlabUrl) {
    this.gitlabUrl = gitlabUrl;
    this.gitlabToken = System.getenv("GITLAB_TOKEN");
  }

  /**
   * Get a project from gitlab.
   *
   * @param id the project id.
   * @param version the project version.
   * @return the project.
   */
  @Override
  public InputStream getRepository(String id, String version) {
    final String url =
        String.format(
            "%s/api/v4/projects/%s/repository/archive.zip?sha=%s", this.gitlabUrl, id, version);

    try {
      final HttpClient client = HttpClient.newHttpClient();

      final HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .GET()
              .setHeader("PRIVATE-TOKEN", this.gitlabToken)
              .build();

      final HttpResponse<InputStream> response =
          client.send(request, HttpResponse.BodyHandlers.ofInputStream());

      return response.body();
    } catch (IOException | InterruptedException e) {
      this.logger.error("Error downloading json-patcher", e);
      return null;
    }
  }

  /**
   * Get the latest version of a project.
   *
   * @param id the project id.
   * @return the latest version.
   */
  @Override
  public String getLatestVersion(String id) {
    final String jsonPatcherProjectUrl =
        String.format("https://gitlab.feedzai.com/api/v4/projects/%s/repository/tags", id);

    try {
      final HttpClient client = HttpClient.newHttpClient();
      final HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(jsonPatcherProjectUrl))
              .GET()
              .setHeader("PRIVATE-TOKEN", this.gitlabToken)
              .build();

      final HttpResponse<InputStream> response =
          client.send(request, HttpResponse.BodyHandlers.ofInputStream());

      final ObjectMapper objectMapper = new ObjectMapper();
      final List<Map<String, Object>> tags =
          objectMapper.readValue(response.body(), new TypeReference<>() {});

      return (String) tags.get(0).get("name");
    } catch (IOException | InterruptedException e) {
      this.logger.error("Error getting latest tag", e);
      return null;
    }
  }
}
