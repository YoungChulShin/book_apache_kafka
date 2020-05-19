## 도커 허브 정보
챕터 5까지 진행한 도커 이미지를 아래 경로의 도커 허브에 업로드
- 주키퍼1: https://hub.docker.com/r/go1323/zookeeper01
- 주키퍼2: https://hub.docker.com/r/go1323/zookeeper02
- 주키퍼3: https://hub.docker.com/r/go1323/zookeeper03
- 카프카1: https://hub.docker.com/r/go1323/kafka01
- 카프카2: https://hub.docker.com/r/go1323/kafka02
- 카프카3: https://hub.docker.com/r/go1323/kafka03

## 필수 카프카 명령어
### 토픽 생성
~~~
// 생성
usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --replication-factor 1 --partitions 1 --topic ycshin6-topic --create

// 결과
Created topic ycshin6-topic.
~~~

### 토픽 리스트 확인
~~~
// 리스트 확인
 /usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --list

 // 결과
 _consumer_offsets
ycshin-topic
ycshin-topic2
ycshin-topic3
ycshin6-topic   <-- 이번에 만든 토픽
~~~

### 토픽 상세보기
~~~
// 상세 보기
/usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --topic ycshin6-topic --describe

// 결과
Topic: ycshin6-topic	PartitionCount: 1	ReplicationFactor: 1	Configs:
	Topic: ycshin6-topic	Partition: 0	Leader: 3	Replicas: 3	Isr: 3
~~~

### 토픽 설정 변경
토픽 보관 주기
- 브로커의 디스크 사용량이 90%에 달하면 먼저 메시지의 보관 주기를 변경해서 용량을 확보해야 한다
- 기본 값은 7일
- kafka-configs.sh로 처리 가능

~~~
// 변경
/usr/local/kafka/bin/kafka-configs.sh -zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --alter --entity-type topics --entity-name ycshin6-topic --add-config retention.ms=3600000

// 실행 결과
Completed Updating config for entity: topic 'ycshin6-topic'.

// 변경 확인 (retention 값이 3600000으로 변경되어 있음)
Topic: ycshin6-topic	PartitionCount: 1	ReplicationFactor: 1	Configs: retention.ms=3600000
	Topic: ycshin6-topic	Partition: 0	Leader: 3	Replicas: 3	Isr: 3

// 삭제 테스트
 /usr/local/kafka/bin/kafka-configs.sh -zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --alter --entity-type topics --entity-name ycshin6-topic --delete-config retention.ms

 // 삭제 실행 결과
 --delete-config is not a recognized option
~~~

### 파티션 수 변경
카프카의 파티션 변경
- 파티션을 늘리는 것만 가능하고 줄이는 것은 불가능하다
- 파티션을 늘렸다고 성능이 좋아지는 것은 아니고, 파티션에 대응되는 컨슈머 수도 늘려줘야 한다
- 파티션을 늘리면 메시지 순서에 영향을 줄 수 있기 때문에 주의해야 한다
   - 컨슈머 입장에서 특정 파티션의 데이터만 가져오도록 설정되어 있을 경우, 파티션이 늘어나면 나누어진 그 과정에서 특정 키의 파티션이 변경될 수 있어서 컨슈머가 잘못된 데이터를 가져오거나 또는 데이터를 가져오지 못할 수 있다

~~~
// 파티션 수 변경 1 -> 2
/usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --alter --topic ycshin6-topic --partitions 2

// 실행 결과
WARNING: If partitions are increased for a topic that has a key, the partition logic or ordering of the messages will be affected
Adding partitions succeeded!

// 변경 결과 확인 (파티션이 늘어난 것 확인 가능)
Topic: ycshin6-topic	PartitionCount: 2	ReplicationFactor: 1	Configs: retention.ms=3600000
	Topic: ycshin6-topic	Partition: 0	Leader: 3	Replicas: 3	Isr: 3
	Topic: ycshin6-topic	Partition: 1	Leader: 1	Replicas: 1	Isr: 1
~~~

### 리플리케이션 팩터 변경
rf.json 파일 생성 
~~~json
{"version":1,
"partitions":[
	{"topic":"ycshin6-topic","partition":0,"replicas":[3,2]},
	{"topic":"ycshin6-topic","partition":1,"replicas":[1,2]}
]}
~~~
- replicas
   - 앞의 값은 리더 정보이며, 뒤에 값은 레플리카 브로커
   - 리더의 경우 변경되면 안되기 때문에 앞에 적어준다

~~~
// 변경 실행
/usr/local/kafka/bin/kafka-reassign-partitions.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --reassignment-json-file /usr/local/kafka/rf.json --execute

// 실행 결과
Current partition replica assignment

{"version":1,"partitions":[{"topic":"ycshin6-topic","partition":1,"replicas":[1],"log_dirs":["any"]},{"topic":"ycshin6-topic","partition":0,"replicas":[3],"log_dirs":["any"]}]}

Save this to use as the --reassignment-json-file option during rollback
Successfully started reassignment of partitions.

// 변경 확인
Topic: ycshin6-topic	PartitionCount: 2	ReplicationFactor: 2	Configs: retention.ms=3600000
	Topic: ycshin6-topic	Partition: 0	Leader: 3	Replicas: 3,2	Isr: 3,2
	Topic: ycshin6-topic	Partition: 1	Leader: 1	Replicas: 1,2	Isr: 1,2
~~~

### 컨슈머 그룹 리스트
컨슈머 그룹 확인(기본 상태)
~~~
// 확인
usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --list

// 결과 (이전 챕터에서 만들었던 그룹이 표시)
ycshin-consumer-group
~~~

### 컨슈머 상태와 오프셋 확인
테스트를 위해서 임시로 ycshin6-topic에 대한 컨슈머 그룹 생성
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin6-topic --group ycshin6-group --from-beginning
~~~

컨슈머 그룹 상태 확인
~~~
// 상태 확인
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --group ycshin6-group --describe

// 결과
Consumer group 'ycshin6-group' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
ycshin6-group   ycshin6-topic   0          0               0               0               -               -               -
ycshin6-group   ycshin6-topic   1          0               0               0               -               -               -
~~~
- LAG: 프로듀서가 보낸 메시지와 컨슈머가 가져간 메시지의 차이

### 주키퍼와 카프카 스케일 아웃
실습에서는 스킵
- 다시 셋업할 엄두가 안남..