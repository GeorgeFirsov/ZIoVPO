#!/bin/sh
set -e

until mc alias set local http://minio:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"; do
  sleep 2
done

mc mb --ignore-existing local/$MINIO_BUCKET
mc anonymous set none local/$MINIO_BUCKET
mc admin user add local "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY" || true
mc admin policy attach local readwrite --user "$MINIO_ACCESS_KEY" || true