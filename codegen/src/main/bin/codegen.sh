#!/bin/sh

java -Xmx1024m -XX:MaxPermSize=1024m -cp ${project.build.finalName}-jar-with-dependencies.jar ${root.packagename.slash}/codegen/App $*
