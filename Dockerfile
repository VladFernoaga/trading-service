# First stage: build the application
FROM openjdk:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Ensure the Maven wrapper has execute permissions and then build the application
RUN chmod +x mvnw && ./mvnw clean package

# Second stage: create the runtime image
FROM openjdk:21-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar /app/trading-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/trading-service.jar"]