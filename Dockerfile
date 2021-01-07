FROM openjdk:11

COPY target/JMHSandbox-1.0.0-SNAPSHOT-shaded.jar jmh.jar

ENTRYPOINT ["java","-jar","jmh.jar","c"]