FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY ./target/ProvenanceAPI-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar" ]