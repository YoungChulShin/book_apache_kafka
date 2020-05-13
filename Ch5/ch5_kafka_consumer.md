## 메시지 전송 테스트

토픽 하나 생성 (ycshin-topic2)
~~~
usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --topic ycshin-topic2 --partitions 1 --replication-factor 3 --create
~~~

토픽 정보 확인 (ycshin-topic2)
~~~
/usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181/ycshin-kafka --topic ycshin-topic2 --describe
~~~

콘솔 프로듀서로 메시지 보내기
~~~
/usr/local/kafka/bin/kafka-console-producer.sh --broker-list testKafka01:9092,testKafka02:9092,testkafka03:9092 --topic ycshin-topic2

[보낸 메시지]
>hello
>this is message for ycshin-topic2
~~~

콘솔 컨슈머로 메시지 가져오기
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic2 --from-beginning

[결과]
hello
this is message for ycshin-topic2
~~~

컨슈머 그룹 확인
~~~
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --list

[결과]
console-consumer-72924
~~~

컨슈머 그룹 지정(ycshin-consumer-group)
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic2 --group ycshin-consumer-group --from-beginning
~~~

컨슈머 그룹 확인
~~~
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --list

[결과]
ycshin-consumer-group
~~~

## 메시지 순서 테스트
새로운 토픽 생성 (ycshin-topic3)
~~~
usr/local/kafka/bin/kafka-topics.sh --zookeeper testZookeeper01:2181,testZookeeper02:2181,testZookeeper03:2181/ycshin-kafka --topic ycshin-topic3 --partitions 3 --replication-factor 1 --create
~~~

a->b-c->d->e 전송
~~~
/usr/local/kafka/bin/kafka-console-producer.sh --broker-list testKafka01:9092,testKafka02:9092,testkafka03:9092 --topic ycshin-topic3

[전송 메시지]
a
b
c
d
e
~~~

a->b-c->d->e 전송 확인
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic3 --from-beginning

[결과]
a
b
c
d
e
~~~

1->2->3->4->5 전송
~~~
/usr/local/kafka/bin/kafka-console-producer.sh --broker-list testKafka01:9092,testKafka02:9092,testkafka03:9092 --topic ycshin-topic3

[전송 메시지]
1
2
3
4
5
~~~

지금까지의 메시지 전송 확인
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic3 --from-beginning

[결과]
a
b
c
1
2
3
d
e
4
5
~~~

파티션 0 메시지 확인
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic3 --partition 0 --from-beginning

[결과]
메시지 없음
~~~

파티션 1 메시지 확인
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic3 --partition 1 --from-beginning

[결과]
a
b
c
1
2
3
~~~

파티션 2 메시지 확인
~~~
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server testKafka01:9092,testKafka02:9092,testKafka03:9092 --topic ycshin-topic3 --partition 2 --from-beginning

[결과]
d
e
4
5
~~~

파티션 별 데이터 확인

|파티션|0|1|2|3|4|5|
|-|-|-|-|-|-|-|
|파티션0|||||
|파티션1|a|b|c|1|2|3|
|파티션2|d|e|4|5|||

