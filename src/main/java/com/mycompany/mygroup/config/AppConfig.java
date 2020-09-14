package com.mycompany.mygroup.config;

import java.util.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
    private Map<String, String> etctransform = new HashMap<>();
    public Map<String, String> getEtctransform() {
        return etctransform;
    }
    public void setEtctransform(Map<String, String> etctransform) {
        this.etctransform = etctransform;
    }
}

