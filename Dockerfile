FROM eclipse-temurin:25-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends adduser ca-certificates curl \
    && rm -rf /var/lib/apt/lists/* \
    && addgroup --system appgroup \
    && adduser --system --group appuser \
    && mkdir -p /app/logs \
    && chown -R appuser:appgroup /app/logs

VOLUME ["/app/logs"]

WORKDIR /app

ARG SERVICE_NAME=app
ARG PROFILE=dev
ARG SERVER_PORT=8080

ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV SERVER_PORT=${SERVER_PORT}

COPY --chown=appuser:appgroup build/libs/${SERVICE_NAME}.jar app.jar

RUN chmod 755 app.jar
USER appuser

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
    CMD curl --fail http://localhost:${SERVER_PORT}/health/${SERVICE_NAME}/status || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=${SPRING_PROFILES_ACTIVE}", "--server.port=${SERVER_PORT}"]
