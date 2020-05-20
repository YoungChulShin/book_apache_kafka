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

## 카프카 모니터링
### 카프카 JMX 설정
JMX(Java Management eXtensions)
- 자바로 만든 애플리케이션 모니터링 등을 위한 도구를 제공하는 자바 API
- MBean(Managed Bean)이라는 객체로 표현된다

JMX 설정 방법
1. 카프카 실행 파일에 JMX 관련 설정을 추가하는 방법
   - 카프카 버전 업그레이드로 실행 파일이 변경되면 다시 추가해줘야하는 단점이 있다
2. systemd를 이용해서 환경변수를 추가하는 방법

systemd를 이용해서 환경변수를 추가하는 방법 실습
1. `/usr/local/kafka/config/jmx` 경로에 파일 생성 및 내용 입력
   ~~~
   JMX_PORT=9999
   ~~~
2. `/etc/systemd/system/kafka-server.service` 파일에 jxm 파일 추가
   ~~~
   // 중략
   ExecStop=/usr/local/kafka/bin/kafka-server-stop.sh
   EnvironmentFile=/usr/local/kafka/config/jmx
   ~~~
3. `systemd` 재시작 및 카프카 재 시작
   ~~~
   systemctl daemon-reload
   systemctl restart kafka-server.service
   ~~~
4. 포트 리스닝 확인
   ~~~
   // 확인
   netstat -ntlp | grep -i 9999

   // 결과
   tcp        0      0 0.0.0.0:9999            0.0.0.0:*               LISTEN      658/java
   ~~~

## 카프카 매니저 -> CMAK (Cluster Manager for Apache Kafka)
CMAK
- https://github.com/yahoo/CMAK
- 운영과 관된된 기능들과 클러스터의 상태를 한눈에 볼수 있는 GUI 툴

### CMAK 설치 
설치 과정
~~~
// opt 폴더에 최신 버전 다운로드 
wget https://github.com/yahoo/CMAK/archive/3.0.0.4.zip

// 압축 풀기
unzip 3.0.0.4.zip

// zip 형태의 배포 파일 만들기 (java version을 1.8 -> 11로 올려줌)
cd CMAK-3.0.0.4/
./sbt clean dist

// 배포 파일 생성 결과
Your package is ready in /opt/CMAK-3.0.0.4/target/universal/cmak-3.0.0.4.zip

// 생성된 파일을 usr/local 경로로 복사
cp /opt/CMAK-3.0.0.4/target/universal/cmak-3.0.0.4.zip /usr/local/

// 복사한 파일 압축 해제
unzip cmak-3.0.0.4.zip

// conf/application.conf 파일에서 주키퍼 설정을 변경
kafka-manager.zkhosts="testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181"

cmak.zkhosts="testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181"

// 카프카 매니저 실행
./cmak -Dconfig.file=/usr/local/cmak-3.0.0.4/conf/application.conf -Dhttp.port=9000

// 접속 대기 활성화
2020-05-20 11:56:38,798 - [INFO] p.c.s.AkkaHttpServer - Listening for HTTP on /0.0.0.0:9000
~~~

접속 성공
![CMAK_run](/Ch6/Images/cmak-1-run.png)

클러스터 생성
![CMAK_run](/Ch6/Images/cmak-2-add-cluster.png)

