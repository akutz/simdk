#!/bin/sh

java -Xmx2048m -XX:MaxPermSize=1024m -cp ${project.build.finalName}-jar-with-dependencies.jar ${root.packagename.slash}/viexport/App %*
