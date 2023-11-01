/*
 * The copyright of this file belongs to Feedzai. The file cannot be
 * reproduced in whole or in part, stored in a retrieval system,
 * transmitted in any form, or by any means electronic, mechanical,
 * photocopying, or otherwise, without the prior permission of the owner.
 *
 * © 2023 Feedzai, Strictly Confidential
 */

import { workspace, type ExtensionContext } from "vscode";

import {
  LanguageClient,
  type LanguageClientOptions,
  type ServerOptions,
} from "vscode-languageclient/node";

let client: LanguageClient;

async function startLangServer(command: string, args: string[]): Promise<void> {
  const serverOptions: ServerOptions = {
    command,
    args,
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: ["json", "yml", "j2", "properties", "xml"],
    synchronize: {
      configurationSection: "feedzails",
    },
  };

  await new LanguageClient(command, serverOptions, clientOptions).start();
}

export async function activate(context: ExtensionContext): Promise<void> {
  const executable = workspace
    .getConfiguration("feedzails")
    .get<string>("executable");
  const token = workspace
    .getConfiguration("feedzails")
    .get<string>("gitlab_token");

  if (executable === undefined || token === undefined) {
    return;
  }

  await startLangServer(executable, [token]);
}

export function deactivate(): Thenable<void> | undefined {
  if (client != null) {
    return client.stop();
  }
  return undefined;
}
