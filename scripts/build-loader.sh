#!/usr/bin/env bash
# Reuse installed trusted tools; never disable TLS validation.
set -euo pipefail
cd "$(dirname "$0")/.."
target=${1:?Usage: build-loader.sh fabric|neoforge|forge [Gradle tasks...]}
shift
case "$target" in
 fabric) java=25; gradle=9.8.2;;
 neoforge) java=21; gradle=8.14.3;;
 forge) java=17; gradle=8.14.3;;
 *) echo 'Unknown loader' >&2; exit 2;;
esac
if [[ -x /workspace/.rustify-tools/jdk$java/bin/java ]]; then
 export JAVA_HOME=/workspace/.rustify-tools/jdk$java
 export PATH="$JAVA_HOME/bin:$PATH"
fi
options=()
if [[ -n ${HTTPS_PROXY:-} ]]; then
 read -r proxy_host proxy_port < <(python3 -c 'import os,urllib.parse; p=urllib.parse.urlparse(os.environ["HTTPS_PROXY"]); print(p.hostname,p.port or 80)')
 options+=("-Dhttps.proxyHost=$proxy_host" "-Dhttps.proxyPort=$proxy_port")
fi
if [[ -f /etc/ssl/certs/java/cacerts ]]; then options+=('-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'); fi
if [[ $# == 0 ]]; then set -- build; fi
if [[ -x /workspace/.rustify-tools/gradle-$gradle/bin/gradle ]]; then
 exec /workspace/.rustify-tools/gradle-$gradle/bin/gradle -p "$target" --no-daemon "${options[@]}" "$@"
fi
exec gradle -p "$target" --no-daemon "${options[@]}" "$@"
