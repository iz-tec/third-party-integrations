package io.iztec.tp.integration.tns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan("io.iztec.tp.integration.tns.config")
@SpringBootApplication(scanBasePackages = {
        "io.iztec.tp.commons",
        "io.iztec.tp.integration.tns"
})
public class TnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TnsApplication.class, args);
    }
}

