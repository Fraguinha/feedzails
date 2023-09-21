# feedzails

### Dependencies

You will need:

- Java 11 or greater. install with:

  ```
  sdk install java 21.0.1-amzn
  ```

- Node 20. install with:

  ```
  nvm install 20.9.0
  ```

### Building

Compile project:

```sh
sdk use java 21.0.1-amzn && mvn clean install
```

Compile vscode extension:

```sh
nvm use 20 && npm --prefix vscode-extension/ install && npm --prefix vscode-extension/ run build
```

Install vscode extension:

```sh
code --install-extension vscode-extension/build/feedzails-0.1.0.vsix
```

### Configuration

in vscode, add these configurations to your settings.json:

```jsonc
{
  // Feedzai
  "feedzails.executable": "/path/to/feedzails/feedzails.sh",
}
```
in neovim, add the following configurations to your init.lua:

```lua
-- Feedzai
vim.lsp.start({
	cmd = { vim.fn.glob("/path/to/feedzails/feedzails.sh") },
	root_dir = vim.fn.getcwd(),
})
```
