#!/bin/bash
#
## Script for starting Record'in blochchain software

## Needed to support fulldataset size (>1GiB)  for mining
java -Xms3G -Xmx3G -XX:+UseG1GC -jar Recordin-19.01-RELEASE.jar
#java -Xms2G -Xmx2G  -jar Recordin-18.10-SNAPSHOT.jar



