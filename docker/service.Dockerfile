# syntax=docker/dockerfile:1
# Dockerfile compartilhado pelos três serviços. Contexto de build: raiz do projeto.
# Uso: docker build -f docker/service.Dockerfile --build-arg SERVICE_PATH=services/transit-service .

FROM maven:3.9-eclipse-temurin-25 AS build
ARG SERVICE_PATH
WORKDIR /build
COPY pom.xml ./
COPY common/ common/
COPY services/ services/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${SERVICE_PATH} -am package -DskipTests \
 && mkdir /out && cp ${SERVICE_PATH}/target/*.jar /out/app.jar

FROM eclipse-temurin:25-jre
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 10001 app
USER app
WORKDIR /app
COPY --from=build /out/app.jar /app/app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
