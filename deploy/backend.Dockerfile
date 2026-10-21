# Build context: repository root (see docker-compose.prod.yml).

# --- Stage 1: build the jar with the Maven Wrapper (JDK only needed here) ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn .mvn
# Dependencies first: this layer stays cached as long as pom.xml does not change.
RUN ./mvnw -B -q dependency:go-offline
COPY backend/src src
# Tests already ran in CI (they need Docker for Testcontainers).
RUN ./mvnw -B -q package -DskipTests

# --- Stage 2: small runtime image (JRE only, no build tools, no source code) ---
FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app
COPY --from=build /build/target/zutritt-backend-*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
