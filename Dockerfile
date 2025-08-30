FROM openjdk:17

WORKDIR /app

COPY target/*.jar app.jar    

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
