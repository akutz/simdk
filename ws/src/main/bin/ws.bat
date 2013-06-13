cd ..
set MAVEN_OPTS="-Xmx1536m -XX:MaxPermSize=1024m"
mvn -Dsun.nio.ch.disableSystemWideOverlappingFileLockCheck=1 jetty:run
