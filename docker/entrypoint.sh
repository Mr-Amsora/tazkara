#!/bin/sh
set -e

mkdir -p /data/postgres /data/rabbitmq
mkdir -p /var/run/postgresql

chown -R postgres:postgres /data/postgres /var/run/postgresql
chown -R rabbitmq:rabbitmq /data/rabbitmq

if [ ! -f /data/postgres/PG_VERSION ]; then
    echo "Initializing PostgreSQL..."

    INITDB=$(find /usr/lib/postgresql -name initdb -type f | head -n 1)

    if [ -z "$INITDB" ]; then
        echo "PostgreSQL initdb not found!"
        exit 1
    fi

    printf '%s\n' "$DB_PASSWORD" > /tmp/db-password
    chown postgres:postgres /tmp/db-password
    chmod 600 /tmp/db-password

    su postgres -c "$INITDB \
        -D /data/postgres \
        --auth-host=scram-sha-256 \
        --auth-local=peer \
        --pwfile=/tmp/db-password"

    rm /tmp/db-password
fi

exec supervisord -n -c /docker/supervisord.conf