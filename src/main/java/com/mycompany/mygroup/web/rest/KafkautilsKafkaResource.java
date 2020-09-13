package com.mycompany.mygroup.web.rest;

import com.mycompany.mygroup.config.SourceKafkaProperties;
import com.mycompany.mygroup.config.TargetKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/kafkautils-kafka")
public class KafkautilsKafkaResource {

    private final Logger log = LoggerFactory.getLogger(KafkautilsKafkaResource.class);

    private final SourceKafkaProperties sourceKafkaProperties;
    private final TargetKafkaProperties targetKafkaProperties;
    private KafkaProducer<String, String> sourceProducer;
    private KafkaProducer<String, String> targetProducer;
    private ExecutorService sseExecutorService = Executors.newCachedThreadPool();

    public KafkautilsKafkaResource(SourceKafkaProperties sourceKafkaProperties, TargetKafkaProperties targetKafkaProperties) {
        this.sourceKafkaProperties = sourceKafkaProperties;
        this.targetKafkaProperties = targetKafkaProperties;
        this.sourceProducer = new KafkaProducer<>(sourceKafkaProperties.getProducerProps());
        this.targetProducer = new KafkaProducer<>(targetKafkaProperties.getProducerProps());
    }

    @PostMapping("/publish/{topic}")
    public PublishResult publish(@PathVariable String topic, @RequestParam String message, @RequestParam(required = false) String key) throws ExecutionException, InterruptedException {
        log.debug("REST request to send to Kafka topic {} with key {} the message : {}", topic, key, message);
        RecordMetadata metadata = sourceProducer.send(new ProducerRecord<>(topic, key, message)).get();
        return new PublishResult(metadata.topic(), metadata.partition(), metadata.offset(), Instant.ofEpochMilli(metadata.timestamp()));
    }

    @GetMapping("/consume")
    public SseEmitter consume(@RequestParam("sourcetopic") List<String> sourceTopics,@RequestParam("targettopic") List<String> targetTopics, @RequestParam Map<String, String> consumerParams) {
        log.debug("REST request to consume records from Kafka topics {}", sourceTopics);
        Map<String, Object> consumerProps = sourceKafkaProperties.getConsumerProps();
        consumerProps.putAll(consumerParams);
        consumerProps.remove("topic");

        SseEmitter emitter = new SseEmitter(0L);
        sseExecutorService.execute(() -> {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
            emitter.onCompletion(consumer::close);
            consumer.subscribe(sourceTopics);
            boolean exitLoop = false;
            while(!exitLoop) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
                    for (ConsumerRecord<String, String> record : records) {
                        emitter.send(record.value());
                        String mirrorTopic = targetTopics.get(0);
                        RecordMetadata metadata = targetProducer.send(new ProducerRecord<>(mirrorTopic, "", record.value())).get();
                        System.out.println("TT TOpic: " + metadata.topic() + metadata.partition() + metadata.offset() + metadata.toString());
                    }
                    emitter.send(SseEmitter.event().comment(""));
                } catch (Exception ex) {
                    log.trace("Complete with error {}", ex.getMessage(), ex);
                    emitter.completeWithError(ex);
                    exitLoop = true;
                }
            }
            consumer.close();
            emitter.complete();
        });
        return emitter;
    }

    private static class PublishResult {
        public final String topic;
        public final int partition;
        public final long offset;
        public final Instant timestamp;

        private PublishResult(String topic, int partition, long offset, Instant timestamp) {
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
            this.timestamp = timestamp;
        }
    }
}
