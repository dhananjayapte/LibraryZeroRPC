RESTful Library Service
======================

$ mvn clean package

$ java -jar target/library-0.0.1-SNAPSHOT.jar server config/dev_config.yml 

# How to run this Java process forever
$ nohup ./bin/dev.sh 0<&- &> /tmp/app.log &

Service endpoint: http://localhost:8080/library/v1

Admin: http://localhost:8081/

