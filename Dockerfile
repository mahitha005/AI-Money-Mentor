# ── Stage 1: Build Spring Boot jar ──
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

# ── Stage 2: Runtime (Java + Python) ──
FROM eclipse-temurin:21-jre

# Install Python + pip + scikit-learn
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install scikit-learn --break-system-packages && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Copy ML model files
COPY ml-model/predict.py ml-model/predict.py
COPY ml-model/finance_model.pkl ml-model/finance_model.pkl

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
