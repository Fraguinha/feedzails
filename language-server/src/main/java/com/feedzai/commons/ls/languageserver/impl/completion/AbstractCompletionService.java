/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.commons.ls.languageserver.impl.completion;

import com.feedzai.commons.ls.languageserver.api.BuildService;
import com.feedzai.commons.ls.languageserver.api.CompletionService;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
  @Getter private final BuildService buildService;

  /** The service name. */
  @Getter private final String name;

  /** The version. */
  @Getter private final String version;

  /** The completion items. */
  @Getter private final List<CompletionItem> completionItems = new ArrayList<>();

  /** Constructor. */
  protected AbstractCompletionService(
      String name, String groupId, String artifactId, BuildService buildService) {
    this.name = name;
    this.version = buildService.getVersion(groupId, artifactId).orElseThrow();
    this.buildService = buildService;
  }

  /** Load the classes. */
  protected abstract void process();

  /**
   * Load the classes from the JAR file.
   *
   * @param jarFile The JAR file path.
   * @return The classes.
   */
  protected List<Class<?>> loadClassesFromJar(String jarFile) {
    final List<Class<?>> classes = new ArrayList<>();

    try (JarFile jar = new JarFile(new File(jarFile))) {
      final Enumeration<JarEntry> entries = jar.entries();
      final URL[] url = {new URL("jar:file:" + jarFile + "!/")};

      try (URLClassLoader classLoader = URLClassLoader.newInstance(url)) {
        while (entries.hasMoreElements()) {
          final JarEntry entry = entries.nextElement();

          if (entry.getName().endsWith(".class")) {
            final String className = entry.getName().replace("/", ".").replace(".class", "");
            final Class<?> cls = classLoader.loadClass(className);
            classes.add(cls);
          }
        }

        return classes;
      }
    } catch (Exception e) {
      this.logger.error("Error loading classes from JAR file", e);
      return classes;
    }
  }

  /**
   * Add the completion items.
   *
   * @param label The label.
   * @param detail The detail.
   * @param insertText The insert text.
   */
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
