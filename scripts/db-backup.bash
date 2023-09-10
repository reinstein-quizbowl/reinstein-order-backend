#!/bin/bash

DB_URL_WITHOUT_JDBC=$(echo $REINSTEIN_DB_URL | cut -d ":" -f "2-")

if [[ $# -eq 0 ]]; then
	DESTINATION_DIRECTORY=~
else
	DESTINATION_DIRECTORY=$1
fi
DESTINATION_FILE=$DESTINATION_DIRECTORY/reinstein-order-$(date +%Y-%m-%d).sql.gz

if [[ -e $DESTINATION_FILE ]]; then
	echo "$DESTINATION_FILE already exists; doing nothing rather than overwriting" 1>&2
	exit 1
else
	PGPASSWORD=$REINSTEIN_DB_PASSWORD pg_dump --dbname=$DB_URL_WITHOUT_JDBC --user=$REINSTEIN_DB_USERNAME | gzip > $DESTINATION_FILE
	echo "Wrote database backup to $DESTINATION_FILE"
fi
