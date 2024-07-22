## syntax=docker/dockerfile:1
#
## Create a stage for resolving and downloading dependencies.
#FROM openjdk:8u181-jdk-slim as deps
#
#WORKDIR /build
#
## Copy the mvnw wrapper with executable permissions.
#COPY --chmod=0755 mvnw mvnw
#COPY mvnw.cmd mvnw.cmd
#COPY .mvn/ .mvn/
#COPY pom.xml .
#
## Download dependencies as a separate step to take advantage of Docker's caching.
## Leverage a cache mount to /root/.m2 so that subsequent builds don't have to re-download packages.
#RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests
#
#################################################################################
#
## Create a stage for building the application based on the stage with downloaded dependencies.
#FROM deps as package
#
#WORKDIR /build
#
#COPY . .
#RUN --mount=type=cache,target=/root/.m2 ./mvnw package -DskipTests && \
#    mv target/exchange-core-0.5.4-SNAPSHOT.jar target/app.jar
#
#################################################################################
#
## Create a new stage for running the application that contains the minimal runtime dependencies for the application.
#FROM openjdk:8u181-jre-slim AS final
#
## Create a non-privileged user that the app will run under.
#ARG UID=10001
#RUN adduser --disabled-password --gecos "" --home "/nonexistent" --shell "/sbin/nologin" --no-create-home --uid "${UID}" appuser
#USER appuser
#
## Copy the executable from the "package" stage.
#COPY --from=package /build/target/app.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT [ "java", "-jar", "app.jar" ]

# syntax=docker/dockerfile:1

# syntax=docker/dockerfile:1

# syntax=docker/dockerfile:1

# Stage 1: Build the application
#FROM openjdk:8u181-jdk-slim AS build
#
#WORKDIR /app
#
#COPY .mvn/ .mvn
#COPY mvnw pom.xml ./
#RUN ./mvnw dependency:go-offline
#
#COPY src ./src
#RUN ./mvnw clean package -DskipTests
#
## Stage 2: Create the final image
#FROM openjdk:8u181-jre-slim
#
#WORKDIR /app
#
#COPY --from=build /app/target/exchange-core-0.5.4-SNAPSHOT.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]


# Stage 1: Build the application
FROM openjdk:8u181-jdk-slim AS build

WORKDIR /app

# Copy Maven configuration and install dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY libs ./libs
RUN ./mvnw dependency:go-offline

# Copy source code and build the project
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:8u181-jre-slim

WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/exchange-core-0.5.4-SNAPSHOT.jar app.jar

# Copy the QuickFIX/J jar file
COPY libs/quickfixj/quickfixj-all-2.3.1.jar /app/quickfixj-all-2.3.1.jar

# Copy the QuickFIX/J configuration file
COPY src/main/resources/quickfix.cfg /app/quickfix.cfg

# Entry point for the container
ENTRYPOINT ["java", "-cp", "app.jar:quickfixj-all-2.3.1.jar", "exchange.core2.core.FixEngine"]
