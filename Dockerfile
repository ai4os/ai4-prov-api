FROM eclipse-temurin:21-jdk
WORKDIR /app
ADD ./target/ProvAPI-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar" ]