#!/bin/sh
DIR=`dirname $(readlink -f $0)`

cat<<EOF
# Cloud Agent variables
CLOUD_AGENT_DIR="$DIR"
CLOUD_AGENT_JAR="$DIR/cloud-agent-aws.jar"

# Extending standard variables
JAVA_OPTS="\$JAVA_OPTS -javaagent:\$CLOUD_AGENT_JAR"

EOF

