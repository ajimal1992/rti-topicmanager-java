#!/bin/bash

if [ "$1" != "" ]; then
    IDLFILENAME=$1
else
    echo "IDL filename required"
    exit 1
fi
mkdir src
rtiddsgen $IDLFILENAME -ppDisable -d ./src -language Java -create typefiles -create examplefiles
mv ./src/USER_QOS_PROFILES.xml ./
rm ./src/*Publisher.java
rm ./src/*Subscriber.java
mv PublisherManager.java ./src
mv SubscriberManager.java ./src
