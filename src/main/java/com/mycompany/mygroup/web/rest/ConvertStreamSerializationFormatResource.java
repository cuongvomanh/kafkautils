package com.mycompany.mygroup.web.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mycompany.mygroup.config.AppConfig;
import com.mycompany.mygroup.config.KafkaProperties;
import com.mycompany.mygroup.service.ConvertStreamSerializationFormat;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafkautils-kafka")
@EnableConfigurationProperties
public class ConvertStreamSerializationFormatResource {

    private final Logger log = LoggerFactory.getLogger(KafkautilsKafkaResource.class);

    private final KafkaProperties kafkaProperties;

    @Autowired
    private ConvertStreamSerializationFormat convertStreamSerializationFormat;

    private AppConfig appConfig;
    private List<String> COLUMNS;
    private Properties props;

    public ConvertStreamSerializationFormatResource(KafkaProperties kafkaProperties, AppConfig appConfig) {
        this.kafkaProperties = kafkaProperties;
        this.appConfig = appConfig;
        this.props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaProperties.getStreamsProps().get("application.id.config"));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getStreamsProps().get("bootstrap.servers"));
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, kafkaProperties.getStreamsProps().get("cache.max.bytes.buffering.config"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        this.COLUMNS = Arrays.asList(appConfig.getEtctransform().get("columns").split(" ") );
    }

    @PostMapping("/convert")
    public ResponseEntity<String> consume(@RequestParam("sourcetopic") String sourceTopic,@RequestParam("targettopic") String targetTopic, @RequestParam Map<String, String> streamsParams) {
        try {
            String inputTopic = appConfig.getEtctransform().get("inputtopic");
            String outputTopic = appConfig.getEtctransform().get("outputtopic");
            System.out.println("DDEBUG sourcetopic: " + (sourceTopic != null));
            convertStreamSerializationFormat.run(this.props, this.COLUMNS, (sourceTopic != null) ? sourceTopic : inputTopic, (targetTopic != null) ? targetTopic : outputTopic);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
