package com.example.java_solana_lp_option;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 啟用排程功能
public class JavaSolanaLpOptionApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaSolanaLpOptionApplication.class, args);
	System.out.println("hello world");
	}

}
