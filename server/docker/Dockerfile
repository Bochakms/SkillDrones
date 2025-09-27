FROM openjdk:21-jdk-slim AS builder
WORKDIR /workspace

COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /opt/app

RUN mkdir -p logs uploads/temp

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]