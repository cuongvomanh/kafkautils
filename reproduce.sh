#kill -9 `ps -ef | grep 'Mirror.*kafkautils' | grep -v grep | awk '{print $2}'`
lsof -i:8051 | awk '{print $2}' | grep -v PID | xargs kill -9
sleep 10
$KAFKA_HOME/bin/kafka-consumer-groups.sh --bootstrap-server 10.60.155.33:8091 --group kafkautils --reset-offsets --to-earliest --all-topics --execute
nohup java -jar -Dspring.profiles.active=server2local -Dserver.port=8051 target/kafkautils-0.0.1-SNAPSHOT.jar &
sleep 10
timeout 10 curl -X GET --header 'Content-Type: application/json' --header 'Accept: application/json'   --header 'X-XSRF-TOKEN: ' 'http://localhost:8051/api/kafkautils-kafka/consume?sourcetopic=cdr_success_toll_collection-133&targettopic=cdr_success_toll_collection-133'

