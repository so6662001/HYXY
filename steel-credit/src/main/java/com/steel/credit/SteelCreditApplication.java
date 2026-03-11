package com.steel.credit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.steel.credit.mapper")
@EnableScheduling
public class SteelCreditApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteelCreditApplication.class, args);
    }
}
