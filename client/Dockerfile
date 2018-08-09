FROM openjdk:8-jre-alpine
ADD target/fileservice-client-1.0.jar fileservice-client.jar
ENTRYPOINT ["java", "-jar", "fileservice-client.jar"]