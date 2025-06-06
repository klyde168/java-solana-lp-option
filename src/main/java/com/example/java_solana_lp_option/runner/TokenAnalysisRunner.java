package com.example.java_solana_lp_option.runner;

import com.example.java_solana_lp_option.analyzer.TokenAnalyzer;
import com.example.java_solana_lp_option.config.SolanaConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
@Order(5)
public class TokenAnalysisRunner implements CommandLineRunner {

    private final TokenAnalyzer tokenAnalyzer;
    private final SolanaConfig solanaConfig;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 目標代幣地址
    private static final String TARGET_TOKEN = "CYsWY6tmV3WhNHAcSrbC3VPd5KiBhjPiPGMzg6xxn66t";
    
    public TokenAnalysisRunner(TokenAnalyzer tokenAnalyzer, SolanaConfig solanaConfig) {
        this.tokenAnalyzer = tokenAnalyzer;
        this.solanaConfig = solanaConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🪙 === 代幣分析器啟動 ===");
        System.out.println("📅 執行時機：應用程式啟動時執行一次");
        System.out.println("🔧 功能：深度分析 SPL Token 和 Token Extensions");
        
        // 顯示配置資訊
        System.out.println("\n📋 分析配置:");
        System.out.printf("   目標代幣: %s%n", TARGET_TOKEN);
        System.out.printf("   RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("   網路環境: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("   區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "✅ 啟用" : "⚠️ 停用");
        
        System.out.println("=".repeat(80));
        
        try {
            // 1. 分析目標代幣
            System.out.println("\n🔵 第一部分：目標代幣深度分析");
            System.out.println("-".repeat(50));
            analyzeTargetToken();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 2. 測試已知代幣
            System.out.println("\n🟢 第二部分：已知代幣對比分析");
            System.out.println("-".repeat(50));
            testKnownTokens();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 3. Token Extensions 專項測試
            System.out.println("\n🟡 第三部分：Token Extensions 功能測試");
            System.out.println("-".repeat(50));
            testTokenExtensionsFeatures();
            
        } catch (Exception e) {
            System.err.println("❌ 代幣分析過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ === 代幣分析器執行完成 ===");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 分析目標代幣
     */
    private void analyzeTargetToken() {
        try {
            System.out.printf("🎯 開始分析目標代幣: %s%n", TARGET_TOKEN);
            System.out.println("📋 此代幣來自 Solscan 連結，將進行完整分析...");
            
            tokenAnalyzer.analyzeTargetToken();
            
            System.out.println("\n💡 分析重點:");
            System.out.println("   • 基本代幣資訊 (供應量、小數位、權限)");
            System.out.println("   • 代幣元數據 (名稱、符號、描述)");
            System.out.println("   • Token Extensions 功能檢測");
            System.out.println("   • 持有者分布分析");
            System.out.println("   • 安全性風險評估");
            
        } catch (Exception e) {
            System.err.printf("❌ 目標代幣分析失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 測試已知代幣
     */
    private void testKnownTokens() {
        try {
            System.out.println("🧪 測試主流代幣，與目標代幣進行對比...");
            
            tokenAnalyzer.testKnownTokens();
            
            System.out.println("\n📊 對比分析說明:");
            System.out.println("   • WSOL: Wrapped SOL，標準 SPL Token");
            System.out.println("   • USDC: USD Coin，穩定幣代表");
            System.out.println("   • RAY: Raydium Token，DeFi 治理代幣");
            System.out.println("   • 目標代幣: 待分析的新代幣");
            
        } catch (Exception e) {
            System.err.printf("❌ 已知代幣測試失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 測試 Token Extensions 功能
     */
    private void testTokenExtensionsFeatures() {
        try {
            System.out.println("🔧 專項測試 Token Extensions 功能...");
            System.out.println("📋 檢測以下 Token 2022 擴展功能:");
            System.out.println("   • Transfer Fee - 轉帳手續費");
            System.out.println("   • Metadata Pointer - 鏈上元數據");
            System.out.println("   • Transfer Hook - 轉帳鉤子程式");
            System.out.println("   • Permanent Delegate - 永久委託人");
            
            // 重新分析目標代幣，專注於 Extensions
            System.out.println("\n🔍 重新檢查目標代幣的 Extensions 狀態...");
            tokenAnalyzer.analyzeToken(TARGET_TOKEN);
            
            displayTokenExtensionsGuide();
            
        } catch (Exception e) {
            System.err.printf("❌ Token Extensions 測試失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 顯示 Token Extensions 指南
     */
    private void displayTokenExtensionsGuide() {
        System.out.println("\n📖 Token Extensions (Token 2022) 功能說明:");
        System.out.println("-".repeat(60));
        
        System.out.println("🔸 Transfer Fee (轉帳手續費):");
        System.out.println("   • 每筆轉帳自動收取手續費");
        System.out.println("   • 手續費歸代幣創建者或指定帳戶");
        System.out.println("   • 支援百分比和最大金額限制");
        
        System.out.println("\n🔸 Metadata Pointer (元數據指針):");
        System.out.println("   • 將元數據直接存儲在代幣 Mint 帳戶中");
        System.out.println("   • 減少對外部元數據服務的依賴");
        System.out.println("   • 提供更好的數據完整性");
        
        System.out.println("\n🔸 Transfer Hook (轉帳鉤子):");
        System.out.println("   • 在每筆轉帳前後執行自定義程式");
        System.out.println("   • 可實現複雜的轉帳邏輯和限制");
        System.out.println("   • 支援 KYC、反洗錢等合規功能");
        
        System.out.println("\n🔸 Permanent Delegate (永久委託人):");
        System.out.println("   • 設定永久的代幣管理權限");
        System.out.println("   • 即使 Freeze Authority 被銷毀也保持權限");
        System.out.println("   • 適用於監管和合規場景");
    }
    
    /**
     * 手動分析指定代幣
     */
    public void manualAnalyzeToken(String mintAddress) {
        System.out.printf("%n🔧 手動分析代幣: %s%n", mintAddress);
        System.out.printf("時間: %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            tokenAnalyzer.analyzeToken(mintAddress);
            System.out.println("✅ 代幣分析完成");
        } catch (Exception e) {
            System.err.printf("❌ 代幣分析失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 手動批次分析
     */
    public void manualBatchAnalyze(List<String> mintAddresses) {
        System.out.printf("%n🔧 手動批次分析代幣 (%d 個)%n", mintAddresses.size());
        System.out.printf("時間: %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            tokenAnalyzer.analyzeBatchTokens(mintAddresses);
            System.out.println("✅ 批次代幣分析完成");
        } catch (Exception e) {
            System.err.printf("❌ 批次代幣分析失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 專項分析特定類型的代幣
     */
    public void analyzeTokenCategory(String category) {
        System.out.printf("%n🎯 分析特定類別代幣: %s%n", category);
        
        List<String> tokens;
        switch (category.toLowerCase()) {
            case "stablecoin":
                tokens = Arrays.asList(
                    "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", // USDC
                    "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB", // USDT
                    "A9mUU4qviSctJVPJdBJWkb28deg915LYJKrzQ19ji3FM"  // USDCet
                );
                break;
            case "defi":
                tokens = Arrays.asList(
                    "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R", // RAY
                    "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE", // ORCA
                    "SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt"  // SRM
                );
                break;
            case "target":
                tokens = Arrays.asList(TARGET_TOKEN);
                break;
            default:
                System.out.printf("❌ 不支援的類別: %s%n", category);
                System.out.println("💡 支援的類別: stablecoin, defi, target");
                return;
        }
        
        manualBatchAnalyze(tokens);
    }
    
    /**
     * 比較兩個代幣
     */
    public void compareTokens(String token1, String token2) {
        System.out.printf("%n⚖️ 代幣對比分析%n");
        System.out.printf("代幣 A: %s%n", token1);
        System.out.printf("代幣 B: %s%n", token2);
        System.out.printf("時間: %s%n", LocalDateTime.now().format(FORMATTER));
        
        List<String> tokens = Arrays.asList(token1, token2);
        manualBatchAnalyze(tokens);
        
        System.out.println("\n📊 對比要點:");
        System.out.println("   • 代幣類型 (標準 SPL vs Token Extensions)");
        System.out.println("   • 權限設定 (Mint/Freeze Authority)");
        System.out.println("   • 供應量和小數位設定");
        System.out.println("   • 安全性風險評分");
        System.out.println("   • Token Extensions 功能差異");
    }
}