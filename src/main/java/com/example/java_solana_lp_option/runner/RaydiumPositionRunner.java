package com.example.java_solana_lp_option.runner;

import com.example.java_solana_lp_option.analyzer.RaydiumPositionAnalyzer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Order(3) // 在其他 Runner 之後執行
public class RaydiumPositionRunner implements CommandLineRunner {

    private final RaydiumPositionAnalyzer analyzer;
    
    // 預設的 CLMM Position NFT Mint (可以透過環境變數或參數覆蓋)
    private static final String DEFAULT_CLMM_POSITION_NFT_MINT = "68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o";
    
    public RaydiumPositionRunner(RaydiumPositionAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 === Raydium Position 分析器啟動 ===");
        System.out.println("📅 執行時機：應用程式啟動時執行一次");
        System.out.println("🔧 功能：分析 Raydium AMM 和 CLMM 倉位資訊");
        System.out.println("💡 注意：目前不會寫入資料庫，僅進行分析和顯示");
        System.out.println("=".repeat(80));
        
        try {
            // 1. 分析 AMM Position
            System.out.println("\n🔵 第一部分：AMM Pool 分析");
            System.out.println("-".repeat(50));
            analyzeAMM();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 2. 分析 CLMM Position
            System.out.println("\n🟢 第二部分：CLMM 倉位分析");
            System.out.println("-".repeat(50));
            analyzeCLMM();
            
        } catch (Exception e) {
            System.err.println("❌ Raydium Position 分析過程中發生錯誤: " + e.getMessage());
            // 移除 e.printStackTrace() 來減少不必要的堆疊追蹤輸出
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ === Raydium Position 分析器執行完成 ===");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 分析 AMM Pool 資訊
     */
    private void analyzeAMM() {
        try {
            System.out.println("🎯 開始執行 Raydium AMM Pool 分析...");
            
            // 可以從環境變數獲取用戶錢包地址（可選）
            String userWallet = System.getenv("USER_WALLET");
            if (userWallet != null && !userWallet.trim().isEmpty()) {
                System.out.printf("👤 將分析用戶錢包: %s%n", userWallet);
            } else {
                System.out.println("💡 未提供用戶錢包地址，僅分析 Pool 基本資訊");
                System.out.println("   如需用戶倉位分析，請設定環境變數 USER_WALLET");
            }
            
            analyzer.analyzeAMMPosition(userWallet);
            
        } catch (Exception e) {
            System.err.println("❌ AMM 分析失敗: " + e.getMessage());
            // 只在除錯模式下顯示完整堆疊追蹤
        }
    }
    
    /**
     * 分析 CLMM Position 資訊
     */
    private void analyzeCLMM() {
        try {
            // 可以從環境變數獲取 CLMM Position ID，否則使用預設值
            String clmmPositionId = System.getenv("CLMM_POSITION_ID");
            if (clmmPositionId == null || clmmPositionId.trim().isEmpty()) {
                clmmPositionId = DEFAULT_CLMM_POSITION_NFT_MINT;
                System.out.printf("💡 使用預設 CLMM Position ID: %s%n", clmmPositionId);
                System.out.println("   如需分析其他倉位，請設定環境變數 CLMM_POSITION_ID");
            } else {
                System.out.printf("🎯 使用環境變數指定的 CLMM Position ID: %s%n", clmmPositionId);
            }
            
            analyzer.analyzeCLMMPosition(clmmPositionId);
            
        } catch (Exception e) {
            System.err.println("❌ CLMM 分析失敗: " + e.getMessage());
            // 只在除錯模式下顯示完整堆疊追蹤
        }
    }
    
    /**
     * 提供手動觸發分析的方法（供其他服務呼叫）
     */
    public void manualAnalyze(String type, String identifier) {
        System.out.printf("%n🔧 手動觸發 %s 分析...%n", type.toUpperCase());
        
        try {
            switch (type.toLowerCase()) {
                case "amm":
                    System.out.println("📊 執行 AMM 分析...");
                    analyzer.analyzeAMMPosition(identifier);
                    break;
                    
                case "clmm":
                    System.out.println("📊 執行 CLMM 分析...");
                    String positionId = (identifier != null && !identifier.trim().isEmpty()) 
                                       ? identifier 
                                       : DEFAULT_CLMM_POSITION_NFT_MINT;
                    analyzer.analyzeCLMMPosition(positionId);
                    break;
                    
                default:
                    System.err.printf("❌ 不支援的分析類型: %s%n", type);
                    System.out.println("💡 支援的類型: amm, clmm");
                    return;
            }
            
            System.out.printf("✅ %s 分析完成%n", type.toUpperCase());
            
        } catch (Exception e) {
            System.err.printf("❌ 手動 %s 分析失敗: %s%n", type.toUpperCase(), e.getMessage());
            // 只在除錯模式下顯示完整堆疊追蹤
        }
    }
    
    /**
     * 提供快速 CLMM 分析方法
     */
    public void quickCLMMAnalysis(String positionId) {
        if (positionId == null || positionId.trim().isEmpty()) {
            positionId = DEFAULT_CLMM_POSITION_NFT_MINT;
        }
        
        System.out.printf("%n⚡ 快速 CLMM 倉位分析: %s%n", positionId);
        try {
            analyzer.analyzeCLMMPosition(positionId);
        } catch (Exception e) {
            System.err.printf("❌ 快速 CLMM 分析失敗: %s%n", e.getMessage());
        }
    }
}