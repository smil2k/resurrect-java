#!/bin/sh

java -Xmx10G -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar runnable/target/*.jar $*