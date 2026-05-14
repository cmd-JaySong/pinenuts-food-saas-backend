package com.pinenuts;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pinenuts.mapper")
public class PinenutsFoodSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(PinenutsFoodSaasApplication.class, args);
    }

}
