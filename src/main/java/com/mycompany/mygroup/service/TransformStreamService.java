package com.mycompany.mygroup.service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mycompany.mygroup.config.AppConfig;
import com.mycompany.mygroup.utils.StringUtils;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class TransformStreamService {

    public void run(Properties props, List<String> COLUMNS, Map<Integer,String> rules, String sourceTopic, String targetTopic) throws Exception {
        Map<String, Integer> FIELD2INDEX = new HashMap<String,Integer>();
        for (Integer i = 0; i < COLUMNS.size(); i++) {
            FIELD2INDEX.put((COLUMNS.get(i)), i);
        }

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(sourceTopic);

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (String e : FIELD2INDEX.keySet()) {
            context.setVariable(e + "_IDX",FIELD2INDEX.get(e));
        }
        context.registerFunction("reverseString", StringUtils.class.getDeclaredMethod("reverseString", new Class[] { String.class }));
        context.registerFunction("truncDate", StringUtils.class.getDeclaredMethod("truncDate", new Class[] { String.class }));

        textLines
            .map((key, value) -> {
                List<String> row = Arrays.asList(value.split("\\|"));
                context.setVariable("row",row);
                List<String> outputList = new ArrayList<String>();
                for (String expression : rules.values()) {
                    String messages = (String) parser.parseExpression(expression).getValue(context);
                    outputList.add(messages);
                }
                String output = String.join("|", outputList);
                return new KeyValue<>(value, output);
            })
            .to(targetTopic);
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }

}

