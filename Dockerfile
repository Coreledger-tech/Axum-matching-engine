## Stage 1: Build the application
#FROM openjdk:8u181-jdk-slim AS build
#
#WORKDIR /app
#
## Copy Maven configuration and install dependencies
#COPY .mvn/ .mvn
#COPY mvnw pom.xml ./
#COPY libs ./libs
#RUN ./mvnw dependency:go-offline
#
## Copy source code and build the project
#COPY src ./src
#RUN ./mvnw clean package -DskipTests
#
## Stage 2: Create the final image
#FROM openjdk:8u181-jre-slim
#
#WORKDIR /app
#
## Copy the built JAR file from the build stage
#COPY --from=build /app/target/exchange-core-0.5.4-SNAPSHOT.jar app.jar
#
## Copy the QuickFIX/J jar file
#COPY libs/quickfixj/quickfixj-all-2.3.1.jar /app/quickfixj-all-2.3.1.jar
#
## Copy the QuickFIX/J configuration file
#COPY src/main/resources/quickfix.cfg /app/quickfix.cfg
#
## Entry point for the container
#ENTRYPOINT ["java", "-cp", "app.jar:quickfixj-all-2.3.1.jar", "exchange.core2.core.FixEngine"]
#

# syntax=docker/dockerfile:1

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

# Expose the FIX port (make sure this port matches your FixEngine config)
EXPOSE 8080
EXPOSE 9090

# Entry point for the container
ENTRYPOINT ["java", "-cp", "app.jar:quickfixj-all-2.3.1.jar", "exchange.core2.core.FixEngine", "exchange.core2.core.AxumApi", "exchange.core2.core.Axum"]
