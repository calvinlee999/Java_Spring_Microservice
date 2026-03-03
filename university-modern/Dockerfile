# =============================================================================
# Dockerfile — Two-Stage Build for the University Modern Application
#
# Stage 1 (builder): Uses the full Maven + JDK image to compile and package
#                    the app into a fat JAR.
# Stage 2 (runtime): Uses a tiny JRE-only image to actually run the app.
#                    This keeps the final image small and secure.
# =============================================================================

# ---- Stage 1: Build ----
# Use an official Maven image that already has JDK 21 and Maven installed.
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container (like cd /app).
WORKDIR /app

# Copy the Maven project file first.
# Docker caches each layer, so if only source code changes (not pom.xml),
# Maven won't need to re-download all dependencies — this makes rebuilds fast.
COPY pom.xml .

# Download all dependencies declared in pom.xml into the local Maven cache.
# The -q flag means "quiet" — less terminal noise.
RUN mvn dependency:go-offline -q

# Now copy all source code into the container.
COPY src ./src

# Compile and package the application into a fat JAR.
# -DskipTests means we skip running tests here (tests run in CI separately).
RUN mvn package -DskipTests -q

# ---- Stage 2: Run ----
# Use a lightweight JRE-only image — much smaller than the full JDK.
# alpine = a tiny Linux distro, perfect for container images.
FROM eclipse-temurin:21-jre-alpine

# Set the working directory for the runtime container.
WORKDIR /app

# Copy ONLY the finished JAR from the builder stage.
# The *.jar wildcard picks up the jar regardless of exact version number.
COPY --from=builder /app/target/*.jar app.jar

# Tell Docker (and humans reading this) that the app listens on port 8080.
EXPOSE 8080

# The command that runs when the container starts.
# java -jar app.jar → starts the Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]
