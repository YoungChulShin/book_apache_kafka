## 토픽 생성
토픽 리스트 확인
- 코드
    ~~~bash
    ./kafka-topics.sh --list --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/test-kafka
    ~~~

기존에 생성했던 토픽 삭제
- 코드
    ~~~bash
    ./kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/test-kafka \--topic test-topic --delete
    ~~~

Relication 수 변경
- 대상 파일: kafka/config/server.properties
- 코드
    ~~~bash
    default.replication.factor = 3
    ~~~

토픽 생성
- 코드
    ~~~bash
    /usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --topic ycshin-topic --partitions 1 --replication-factor 3 --create
    ~~~
- 결과
   ~~~bash
   Created topic ycshin-topic.
   ~~~
- __삽질__
   - zookeeper 정보를 기록할 때 지노드 정보를 기록하지 않아서 문제가 발생
   - kafka의 log(data1, data2) 폴더에 이전 zookeeper의 cluster 정보가 남아있어서 이 부분에서 계속 오류가 발생.(kafka 로그에서 확인) <br>
   log 폴더를 삭제하고 신규 생성해서 문제 해결

