package com.example.java_solana_lp_option.runner;

import com.example.java_solana_lp_option.analyzer.SolanaAccountAnalyzer;
import com.example.java_solana_lp_option.config.SolanaConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//@Component
@Order(3)
public class SolanaAccountRunner implements CommandLineRunner {

    private final SolanaAccountAnalyzer accountAnalyzer;
    private final SolanaConfig solanaConfig;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SolanaAccountRunner(SolanaAccountAnalyzer accountAnalyzer, SolanaConfig solanaConfig) {
        this.accountAnalyzer = accountAnalyzer;
        this.solanaConfig = solanaConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 === Solana 帳戶分析器啟動 ===");
        System.out.println("📅 執行時機：應用程式啟動時執行一次");
        System.out.println("🔧 功能：分析 Solana 帳戶的詳細資訊");
        
        // 顯示配置資訊
        System.out.println("\n📋 Solana 節點配置:");
        System.out.printf("   RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("   網路環境: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("   區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "✅ 啟用 (使用實際節點)" : "⚠️ 停用 (使用模擬數據)");
        
        System.out.println("=".repeat(80));
        
        try {
            // 分析目標帳戶：3tgWY4ZcaLYE3jqp2fMaYiinPADmZFRCXLhmLwMpCBGu
            System.out.println("\n🔵 分析目標帳戶");
            System.out.println("-".repeat(50));
            accountAnalyzer.analyzeTargetAccount();
            
        } catch (Exception e) {
            System.err.println("❌ Solana 帳戶分析過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ === Solana 帳戶分析器執行完成 ===");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 手動分析指定帳戶
     */
    public void manualAnalyzeAccount(String accountAddress) {
        System.out.printf("%n🔧 手動分析帳戶: %s%n", accountAddress);
        System.out.printf("時間: %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            accountAnalyzer.analyzeAccount(accountAddress);
            System.out.println("✅ 帳戶分析完成");
        } catch (Exception e) {
            System.err.printf("❌ 帳戶分析失敗: %s%n", e.getMessage());
        }
    }
}