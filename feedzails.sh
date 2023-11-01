#!/usr/bin/env bash

#
# The copyright of this file belongs to Feedzai. The file cannot be
# reproduced in whole or in part, stored in a retrieval system,
# transmitted in any form, or by any means electronic, mechanical,
# photocopying, or otherwise, without the prior permission of the owner.
#
# © 2023 Feedzai, Strictly Confidential
#

# This script is used to launch the lsp server.

JAVA=$(find ~/.sdkman/candidates/java/*/bin/java | grep -v "current" | sort -V | tail -n 1)
JAVA_VERSION=$("$JAVA" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
	echo "No Java version >= 11 found. Please install one with the command: sdk install java [version]"
	exit 1
fi

if [ $# -ne 1 ]; then
	echo "No GitLab token. Usage: $0 [gitlab-token]"
	exit 1
fi

GITLAB_TOKEN="$1" "$JAVA" -jar "$(dirname "$0")"/language-server-launcher/target/language-server-launcher.jar
