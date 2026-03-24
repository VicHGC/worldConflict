FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline -B

COPY backend/src backend/src
RUN mvn -f backend/pom.xml package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/backend/target/worldconflict-api-1.0.0.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
