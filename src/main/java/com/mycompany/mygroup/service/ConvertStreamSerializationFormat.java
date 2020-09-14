package com.mycompany.mygroup.service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mycompany.mygroup.config.AppConfig;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class ConvertStreamSerializationFormat {

    private String changeSplitCharacter(List<String> list, List<String> COLUMNS) {
        try {
            JSONObject obj = new JSONObject();
            for( int i = 0; i < list.size(); i++ ){
                obj.put(COLUMNS.get(i), list.get(i));
            }
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void run(Properties props, List<String> COLUMNS, String sourceTopic, String targetTopic) throws Exception {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(sourceTopic);
        textLines
            .map((key, value) -> new KeyValue<>(value, changeSplitCharacter(Arrays.asList(value.split("\\|")),COLUMNS)))
            .to(targetTopic);
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }

}

