java -cp "./lib/javassist-3.22.0-CR1.jar;./target/lib/slf4j-api-1.7.25.jar;./target/lib/slf4j-log4j12-1.7.25.jar;./target/lib/log4j-1.2.17.jar;./target/classes;./target/test-classes" -javaagent:./target/logtrace-0.1.jar=yes -Djava.util.logging.config.file="./src/main/resources/logging.properties" com.starsloader.logtrace.TimeTest
pause
