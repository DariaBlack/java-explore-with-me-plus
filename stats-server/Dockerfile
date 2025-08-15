FROM amazoncorretto:21

COPY ./stats-server/service/target/service-*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]