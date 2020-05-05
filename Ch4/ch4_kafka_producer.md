### 토픽 생성
토픽 리스트 확인
- ./kafka-topics.sh --list --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/test-kafka

기존에 생성했던 토픽 삭제
- ./kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/test-kafka \--topic test-topic --delete

Relication 수 변경
- kafka/config/server.properties
- default.replication.factor = 3

