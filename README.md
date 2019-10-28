[![Build Status](https://travis-ci.org/edigonzales/parcel-info-service.svg?branch=master)](https://travis-ci.org/edigonzales/parcel-info-service)
# parcel-info-service

## Developing
```
docker run --rm --name oereb-db-data -p 54321:5432 --hostname primary \
-e PG_DATABASE=oereb -e PG_LOCALE=de_CH.UTF-8 -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary \
-e PG_USER=admin -e PG_PASSWORD=admin \
-e PG_PRIMARY_USER=repl -e PG_PRIMARY_PASSWORD=repl \
-e PG_ROOT_PASSWORD=secret \
-e PG_WRITE_USER=gretl -e PG_WRITE_PASSWORD=gretl \
-e PG_READ_USER=ogc_server -e PG_READ_PASSWORD=ogc_server \
-e PGDATA=/tmp/primary \
sogis/oereb-db-data:2019-10-25_205953
```

Im Image `sogis/oereb-db-data` sind bereits Daten der amtlichen Vermessung in der Datenbank gespeichert.

- http://localhost:8080/getparcel?XY=2600456,1215400
