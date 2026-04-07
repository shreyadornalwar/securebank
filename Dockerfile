FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY backend/ .

RUN mvn clean dependency:resolve -U package -DskipTests

CMD ["java", "-jar", "target/banking-system-1.0.0.jar"]