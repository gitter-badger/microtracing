java -cp "./lib/javassist-3.22.0-CR1.jar;lib/log4j-1.2.12.jar;./target/classes;./target/test-classes" -javaagent:./target/logtrace-0.1.jar=yes -Djava.util.logging.config.file="./src/main/resources/logging.properties" com.starsloader.logtrace.TimeTest
pause
