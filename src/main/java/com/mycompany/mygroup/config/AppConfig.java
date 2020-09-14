package com.mycompany.mygroup.config;

import java.util.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
    private Map<String, Object> etctransform = new HashMap<>();
    public Map<String, Object> getEtctransform() {
        return etctransform;
    }
    public void setEtctransform(Map<String, Object> etctransform) {
        this.etctransform = etctransform;
    }
}

