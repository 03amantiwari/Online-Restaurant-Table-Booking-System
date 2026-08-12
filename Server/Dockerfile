
#STAGE 1 - BUILD
#use a full maven + JDK 21 image to compile the spring Boot app into a jar

FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

#copy the pom.xml first and pre downlaod the all dependencies

COPY pom.xml .
RUN mvn dependency:go-offline -B

#copy the source and build the jar 

COPY src ./src 
RUN mvn clean package -DskipTests -B 


#===================================================

#STAGE 2 - RUNTIME 
#use a light weight JRE-only image (no Maven ,no JDK) to run the jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app


#copy the build jar from the build stage 
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV MANAGEMENT_PORT=8080
ENV CONTEXT_PATH="/api/v1"

# Spring Boot Actuator — this healthcheck lets
# Docker / Swarm know when the container is actually ready, not just started.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${MANAGEMENT_PORT}${CONTEXT_PATH}/actuator/health || exit 1
 

ENTRYPOINT [ "java" ,"-jar", "app.jar" ]




































