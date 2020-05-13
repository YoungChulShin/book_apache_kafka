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

토픽 정보 확인
- 명령어
    ~~~bash
    /usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181/ycshin-kafka --topic ycshin-topic --describe
    ~~~
- 결과
    ~~~bash
    Topic: ycshin-topic	PartitionCount: 1	ReplicationFactor: 3	Configs:
        Topic: ycshin-topic	Partition: 0	Leader: 3	Replicas: 3,2,1	Isr: 3,2,1
    ~~~
   - 파티션 0에 리더는 3번이라는 것을 확인

## 클라이언트 환경 구성
클라이언트용 Docker Container 생성
- 셋업 과정
   - git 설치
      ~~~
      yum install git
      ~~~


메시지 보내기/받기
- 명령어
   - 메시지 보내기
        ~~~bash
        /usr/local/kafka/bin/kafka-console-producer.sh --broker-list testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic
        ~~~
   - 메시지 받기
        ~~~bash
        /usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic --from-beginning
        ~~~
   - 메시지 받기 명령어가 실행되면 보냈던 메시지들이 차례대로 출력된다
- 기타
   - connection 에러가 날 경우, 브로커의 서비스 상태가 모두 active인지 체크 필요


## 삽질 일기
카프카를 실행할 때 동시에 1개만 접속이 가능한 이슈
- 로그 (sevser.log)를 보니 'Exiting Kafka' 에러가 뜸
- systemctl을 통해서 카프카를 실행할 때 server config 정보(config/server.properties)를 함께 첨부하지 않아서 에러 발생

server.properties 를 통해서 실행할 때 에러 
- 메시지
   ~~~
   The unit files have no installation config (WantedBy, RequiredBy, Also, Alias settings in the [Install] section, and DefaultInstance for template units).
   This means they are not meant to be enabled using systemctl.
   ~~~
- 해결 방법
   - '/etc/systemd/system/kafka-server.service' 문서의 가장 아래에 install 설정을 추가해준다
   ~~~
   [Install]
   WantedBy=multi-user.target
   ~~~