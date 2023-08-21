#!/bin/bash

nohup $JAVA_HOME/bin/java -jar $REINSTEIN_APP_HOME/build/libs/order-0.0.1-SNAPSHOT.jar >> $REINSTEIN_APP_HOME/$REINSTEIN_APP_NAME.log 2>&1 &
echo "Starting $REINSTEIN_APP_NAME"
