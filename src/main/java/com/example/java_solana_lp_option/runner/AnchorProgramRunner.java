package com.example.java_solana_lp_option.runner;

import com.example.java_solana_lp_option.analyzer.AnchorProgramAnalyzer;
import com.example.java_solana_lp_option.config.SolanaConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

//@Component
@Order(4)
public class AnchorProgramRunner implements CommandLineRunner {

    private final AnchorProgramAnalyzer anchorProgramAnalyzer;
    private final SolanaConfig solanaConfig;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public AnchorProgramRunner(AnchorProgramAnalyzer anchorProgramAnalyzer, SolanaConfig solanaConfig) {
        this.anchorProgramAnalyzer = anchorProgramAnalyzer;
        this.solanaConfig = solanaConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔗 === Anchor 程式分析器啟動 ===");
        System.out.println("📅 執行時機：應用程式啟動時執行一次");
        System.out.println("🔧 功能：分析 Anchor 程式帳戶的詳細資訊");
        
        // 顯示配置資訊
        System.out.println("\n📋 Solana 節點配置:");
        System.out.printf("   RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("   網路環境: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("   區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "✅ 啟用 (使用實際節點)" : "⚠️ 停用 (使用模擬數據)");
        
        System.out.println("=".repeat(80));
        
        try {
            // 1. 顯示系統總覽
            System.out.println("\n🔵 第一部分：Anchor 程式系統總覽");
            System.out.println("-".repeat(50));
            anchorProgramAnalyzer.displayProgramOverview();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 2. 分析目標帳戶
            System.out.println("\n🟢 第二部分：目標帳戶 Anchor 程式分析");
            System.out.println("-".repeat(50));
            anchorProgramAnalyzer.analyzeTargetAccount();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 3. 測試已知帳戶
            System.out.println("\n🟡 第三部分：批次測試已知 Anchor 程式帳戶");
            System.out.println("-".repeat(50));
            anchorProgramAnalyzer.testKnownAccounts();
            
        } catch (Exception e) {
            System.err.println("❌ Anchor 程式分析過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ === Anchor 程式分析器執行完成 ===");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 手動分析指定帳戶
     */
    public void manualAnalyzeAnchorAccount(String accountAddress) {
        System.out.printf("%n🔧 手動分析 Anchor 程式帳戶: %s%n", accountAddress);
        System.out.printf("時間: %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            anchorProgramAnalyzer.analyzeAnchorAccount(accountAddress);
            System.out.println("✅ Anchor 程式分析完成");
        } catch (Exception e) {
            System.err.printf("❌ Anchor 程式分析失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 手動批次分析
     */
    public void manualBatchAnalyze(List<String> accountAddresses) {
        System.out.printf("%n🔧 手動批次分析 Anchor 程式帳戶 (%d 個)%n", accountAddresses.size());
        System.out.printf("時間: %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            anchorProgramAnalyzer.analyzeBatchAnchorAccounts(accountAddresses);
            System.out.println("✅ 批次 Anchor 程式分析完成");
        } catch (Exception e) {
            System.err.printf("❌ 批次 Anchor 程式分析失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 顯示程式統計
     */
    public void displayStatistics() {
        System.out.printf("%n📊 Anchor 程式系統統計 - %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            anchorProgramAnalyzer.displayProgramOverview();
        } catch (Exception e) {
            System.err.printf("❌ 顯示統計資訊失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 測試特定 DeFi 協議
     */
    public void testSpecificProtocol(String protocolType) {
        System.out.printf("%n🧪 測試特定協議: %s%n", protocolType);
        
        List<String> testAccounts;
        switch (protocolType.toLowerCase()) {
            case "raydium":
                testAccounts = Arrays.asList(
                    "68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o",
                    "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"
                );
                break;
            case "orca":
                testAccounts = Arrays.asList(
                    "3tgWY4ZcaLYE3jqp2fMaYiinPADmZFRCXLhmLwMpCBGu"
                );
                break;
            case "jupiter":
                testAccounts = Arrays.asList(
                    "8sLbNZoA1cfnvMJLPfp98ZLAnFSYCFApfJKMbiXNLwxj"
                );
                break;
            default:
                System.out.printf("❌ 不支援的協議: %s%n", protocolType);
                return;
        }
        
        manualBatchAnalyze(testAccounts);
    }
}