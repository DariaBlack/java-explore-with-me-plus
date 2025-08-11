FROM amazoncorretto:21-jdk
COPY target/*.jar stats-server.jar
ENTRYPOINT ["java", "-jar", "/stats-server.jar"]