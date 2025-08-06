FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y --no-install-recommends netcat dnsutils && \
    rm -rf /var/lib/apt/lists/*

VOLUME /tmp
COPY target/gateway-file.jar app.jar

CMD ["sh", "-c", \
    "echo 'Waiting for server...'; \
     until nslookup server > /dev/null 2>&1 && nc -z server 9090; do \
        sleep 1; \
        echo 'Still waiting for server:9090...'; \
     done; \
     echo 'Server connection established!'; \
     exec java ${JAVA_OPTS} -jar /app.jar"]