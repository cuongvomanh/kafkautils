kill -9 `ps -ef | grep kafkautils | grep -v grep | awk '{print $2}'`
sleep 10
$KAFKA_HOME/bin/kafka-consumer-groups.sh --bootstrap-server 10.60.155.33:8091 --group kafkautils --reset-offsets --to-earliest --all-topics --execute
nohup java -jar -Dspring.profiles.active=server2local target/kafkautils-0.0.1-SNAPSHOT.jar &
sleep 10
timeout 10 curl -X GET --header 'Content-Type: application/json' --header 'Accept: application/json'   --header 'X-XSRF-TOKEN: ' 'http://localhost:8081/api/kafkautils-kafka/consume?sourcetopic=cdr_success_toll_collection-133&targettopic=cdr_success_toll_collection-133'

