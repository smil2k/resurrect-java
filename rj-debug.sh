#!/bin/sh

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -Xmx10G -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar runnable/target/*.jar $*