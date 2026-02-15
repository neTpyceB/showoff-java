package com.showoff.incidentops.springboot;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.showoff.incidentops.springboot")
public class IncidentApiApplication {
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = launch(args);
    }

    static ConfigurableApplicationContext launch(String[] args) {
        return SpringApplication.run(IncidentApiApplication.class, args);
    }

    static void shutdown() {
        if (context != null) {
            context.close();
            context = null;
        }
    }
}
