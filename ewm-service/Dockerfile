FROM amazoncorretto:21-jdk
COPY target/*.jar ewm-service.jar
ENTRYPOINT ["java", "-jar", "/ewm-service.jar"]