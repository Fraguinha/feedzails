/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

package com.feedzai.ls.launcher;

import com.feedzai.ls.languageserver.FeedzaiLanguageServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * The Stdio Launcher.
 *
 * @since 0.1.0
 */
public final class StdioLauncher {
  /** Constructor. */
  private StdioLauncher() {}

  /**
   * Main method.
   *
   * @param args The arguments.
   * @throws ExecutionException If an error occurs.
   * @throws InterruptedException If an error occurs.
   */
  public static void main(final String[] args) throws ExecutionException, InterruptedException {
    LogManager.getLogManager().reset();
    Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    globalLogger.setLevel(Level.OFF);

    startServer(System.in, System.out);
  }

  /**
   * Starts the server.
   *
   * @param in The input stream.
   * @param out The output stream.
   * @throws ExecutionException If an error occurs.
   * @throws InterruptedException If an error occurs.
   */
  private static void startServer(final InputStream in, final OutputStream out)
      throws ExecutionException, InterruptedException {
    FeedzaiLanguageServer feedzaiLanguageServer = new FeedzaiLanguageServer();

    Launcher<LanguageClient> launcher =
        LSPLauncher.createServerLauncher(feedzaiLanguageServer, in, out);

    feedzaiLanguageServer.connect(launcher.getRemoteProxy());

    launcher.startListening().get();
  }
}
