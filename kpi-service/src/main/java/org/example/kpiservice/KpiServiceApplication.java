package org.example.kpiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync

public class KpiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KpiServiceApplication.class, args);
    }

}
