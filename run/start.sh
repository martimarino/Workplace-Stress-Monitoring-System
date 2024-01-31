
#!/bin/bash

cd project/WSMS/wsms/
mvn clean compile
mvn exec:java -Dexec.mainClass="it.unipi.adii.iot.wsms.Collector"
