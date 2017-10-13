java -cp "./lib/hessian.jar;./lib/ojdbc14.jar;./target/lib/javassist-3.22.0-GA.jar;./target/lib/hessian-4.0.51.jar;./target/lib/slf4j-api-1.7.25.jar;./target/lib/slf4j-log4j12-1.7.25.jar;./target/lib/log4j-1.2.17.jar;./target/logtrace-0.1.jar;./target/test-classes;./target/classes;"  -Djava.util.logging.config.file="./src/test/resources/logging.properties"  -javaagent:./target/logtrace-0.1.jar=./src/test/resources/logtrace.properties com.starsloader.logtrace.TimeTest

pause
