package com.example.java_solana_lp_option;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.java_solana_lp_option.config.SolanaConfig;

@SpringBootApplication
@EnableScheduling  // 啟用排程功能
@EnableConfigurationProperties(SolanaConfig.class)  // 啟用配置屬性
public class JavaSolanaLpOptionApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaSolanaLpOptionApplication.class, args);
	System.out.println("hello world");
	}

}
