FROM maven:3.9.6-amazoncorretto-21 AS build

RUN groupadd appgroup && useradd -m -g appgroup appuser
USER appuser

WORKDIR /app

COPY ["src", "./src"]
COPY ["pom.xml", "./"]

ARG APP_MONGODB_HOST_ARG
ARG APP_MONGODB_PORT_ARG
ARG RABBIT_HOST_ARG
ARG RABBIT_PORT_ARG
ARG WEBDRIVER_SCHEME_ARG
ARG WEBDRIVER_HOST_ARG
ARG WEBDRIVER_PORT_ARG

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/justjoinparser-1.0.0-SNAPSHOT.jar /app/justjoinparser-1.0.0-SNAPSHOT.jar

ENV APP_MONGODB_HOST=$APP_MONGODB_HOST_ARG
ENV APP_MONGODB_PORT=$APP_MONGODB_PORT_ARG
ENV RABBIT_HOST=$RABBIT_HOST_ARG
ENV RABBIT_PORT=$RABBIT_PORT_ARG
ENV WEBDRIVER_SCHEME=$WEBDRIVER_SCHEME_ARG
ENV WEBDRIVER_HOST=$WEBDRIVER_HOST_ARG
ENV WEBDRIVER_PORT=$WEBDRIVER_PORT_ARG

EXPOSE 8082

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/justjoinparser-1.0.0-SNAPSHOT.jar"]
