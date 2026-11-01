FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -Dmaven.test.skip=true

RUN mkdir /out \
    && cp identity-service/target/identity-service-*.jar /out/identity-service.jar \
    && cp event-service/target/event-service-*.jar /out/event-service.jar \
    && cp booking-service/target/booking-service-*.jar /out/booking-service.jar \
    && cp api-gateway/target/api-gateway-*.jar /out/api-gateway.jar


FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        postgresql \
        rabbitmq-server \
        supervisor \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home tazkara \
    && rabbitmq-plugins enable --offline rabbitmq_management \
    && echo "loopback_users.guest = false" > /etc/rabbitmq/rabbitmq.conf

WORKDIR /app

COPY --from=build /out/ ./
COPY docker/ /docker/

RUN ln -s "$(find /usr/lib/postgresql -name postgres -type f | head -n 1)" /usr/local/bin/postgres \
    && /usr/local/bin/postgres --version

RUN sed -i 's/\r$//' /docker/entrypoint.sh /docker/supervisord.conf \
    && chmod 755 /docker/entrypoint.sh

EXPOSE 8080

VOLUME /data

USER root

ENTRYPOINT ["/docker/entrypoint.sh"]