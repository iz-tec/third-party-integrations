package io.iztec.tp.integration.tns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan("io.iztec.tp.integration.tns.config")
@SpringBootApplication(scanBasePackages = {
        "io.iztec.tp.commons",
        "io.iztec.tp.integration.tns"
})
@EnableJpaRepositories(basePackages = {
        "io.iztec.tp.commons.database.repository",
        "io.iztec.tp.integration.tns"
})
@EntityScan(basePackages = {
        "io.iztec.tp.commons.database.entity",
        "io.iztec.tp.integration.tns"
})
public class TnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TnsApplication.class, args);
    }
}

