FROM eclipse-temurin:21-jre-jammy

# Устанавливаем необходимые утилиты
RUN apt-get update && \
    apt-get install -y --no-install-recommends netcat-openbsd dnsutils && \
    rm -rf /var/lib/apt/lists/*

VOLUME /tmp
COPY target/server-file.jar app.jar

# Команда для проверки доступности порта и DNS resolution
CMD ["sh", "-c", \
    "echo 'Waiting for DNS resolution and database connection...'; \
     timeout=120; \
     start_time=$(date +%s); \
     while ! (nslookup db > /dev/null 2>&1 && nc -z db 5432); do \
        sleep 1; \
        current_time=$(date +%s); \
        elapsed=$((current_time - start_time)); \
        if [ $elapsed -ge $timeout ]; then \
            echo \"Timeout reached after $timeout seconds. Exiting.\"; \
            exit 1; \
        fi; \
        echo \"Still waiting for db:5432... (${elapsed}s elapsed)\"; \
     done; \
     echo 'DNS resolution and database connection successful!'; \
     exec java ${JAVA_OPTS} -jar /app.jar"]