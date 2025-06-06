package com.example.java_solana_lp_option.analyzer;

import com.example.java_solana_lp_option.config.SolanaConfig;
import com.example.java_solana_lp_option.service.TokenAnalysisService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * 代幣分析器 - 專門用於分析 SPL Token 和 Token Extensions 的詳細資訊
 */
@Component
public class TokenAnalyzer {
    
    private final TokenAnalysisService tokenAnalysisService;
    private final SolanaConfig solanaConfig;
    
    // 目標代幣地址
    private static final String TARGET_TOKEN = "CYsWY6tmV3WhNHAcSrbC3VPd5KiBhjPiPGMzg6xxn66t";
    
    public TokenAnalyzer(TokenAnalysisService tokenAnalysisService, SolanaConfig solanaConfig) {
        this.tokenAnalysisService = tokenAnalysisService;
        this.solanaConfig = solanaConfig;
    }
    
    /**
     * 分析單一代幣
     */
    public void analyzeToken(String mintAddress) {
        System.out.println("🪙 代幣分析器");
        System.out.println("=".repeat(80));
        System.out.printf("🎯 分析代幣: %s%n", mintAddress);
        
        displayConnectionStatus();
        
        try {
            TokenAnalysisService.TokenAnalysisResult result = 
                tokenAnalysisService.analyzeToken(mintAddress);
            
            if (result.getError() != null) {
                System.out.printf("❌ 分析失敗: %s%n", result.getError());
                return;
            }
            
            displayTokenAnalysis(result);
            
        } catch (Exception e) {
            System.err.printf("❌ 分析過程發生錯誤: %s%n", e.getMessage());
        }
    }
    
    /**
     * 批次分析多個代幣
     */
    public void analyzeBatchTokens(List<String> mintAddresses) {
        System.out.println("🚀 批次代幣分析器");
        System.out.println("=".repeat(80));
        System.out.printf("📊 將分析 %d 個代幣%n", mintAddresses.size());
        
        List<TokenAnalysisService.TokenAnalysisResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        for (int i = 0; i < mintAddresses.size(); i++) {
            String mintAddress = mintAddresses.get(i);
            System.out.printf("\n🔄 [%d/%d] 分析代幣: %s%n", i + 1, mintAddresses.size(), mintAddress);
            
            try {
                TokenAnalysisService.TokenAnalysisResult result = 
                    tokenAnalysisService.analyzeToken(mintAddress);
                
                results.add(result);
                
                if (result.getError() == null) {
                    successCount++;
                    System.out.printf("✅ 成功: %s%n", getTokenDisplayName(result));
                } else {
                    failedCount++;
                    System.out.printf("❌ 失敗: %s%n", result.getError());
                }
                
                // 批次處理間隔
                if (i < mintAddresses.size() - 1) {
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                failedCount++;
                System.err.printf("❌ 分析失敗: %s%n", e.getMessage());
            }
        }
        
        // 顯示批次分析摘要
        displayBatchSummary(results, successCount, failedCount);
    }
    
    /**
     * 顯示詳細的代幣分析結果
     */
    private void displayTokenAnalysis(TokenAnalysisService.TokenAnalysisResult result) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 代幣詳細分析報告");
        System.out.println("=".repeat(80));
        
        // 顯示時間戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date analysisDate = new Date(result.getAnalysisTimestamp());
        System.out.printf("⏰ 分析時間: %s%n", sdf.format(analysisDate));
        
        // 基本資訊
        displayBasicInfo(result.getBasicInfo());
        
        // 元數據資訊
        displayMetadata(result.getMetadata());
        
        // Token Extensions 資訊
        displayTokenExtensions(result.getExtensions());
        
        // 持有者分析
        displayHoldersAnalysis(result.getHoldersAnalysis());
        
        // 安全性分析
        displaySecurityAnalysis(result.getSecurityAnalysis());
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 代幣分析完成！");
    }
    
    /**
     * 顯示基本資訊
     */
    private void displayBasicInfo(TokenAnalysisService.TokenBasicInfo basicInfo) {
        if (basicInfo == null || basicInfo.getError() != null) {
            System.out.println("\n❌ 基本資訊獲取失敗");
            return;
        }
        
        System.out.println("\n💎 基本代幣資訊:");
        System.out.println("-".repeat(50));
        System.out.printf("代幣地址: %s%n", basicInfo.getMintAddress());
        System.out.printf("擁有程式: %s%n", getOwnerProgramName(basicInfo.getOwnerProgram()));
        System.out.printf("小數位數: %d%n", basicInfo.getDecimals());
        System.out.printf("總供應量: %,.0f%n", basicInfo.getActualSupply());
        System.out.printf("原始供應量: %,d (最小單位)%n", basicInfo.getSupply());
        
        if (basicInfo.getMintAuthority() != null) {
            System.out.printf("Mint Authority: %s%n", basicInfo.getMintAuthority());
        } else {
            System.out.println("Mint Authority: ✅ 已銷毀 (無法增發)");
        }
        
        if (basicInfo.getFreezeAuthority() != null) {
            System.out.printf("Freeze Authority: %s%n", basicInfo.getFreezeAuthority());
        } else {
            System.out.println("Freeze Authority: ✅ 已銷毀 (無法凍結)");
        }
        
        System.out.printf("代幣類型: %s%n", basicInfo.isTokenExtensions() ? "Token Extensions (Token 2022)" : "標準 SPL Token");
        System.out.printf("初始化狀態: %s%n", basicInfo.isInitialized() ? "✅ 已初始化" : "❌ 未初始化");
    }
    
    /**
     * 顯示元數據
     */
    private void displayMetadata(TokenAnalysisService.TokenMetadata metadata) {
        if (metadata == null) {
            System.out.println("\n❌ 元數據資訊不可用");
            return;
        }
        
        System.out.println("\n📝 代幣元數據:");
        System.out.println("-".repeat(50));
        
        if (metadata.getError() != null) {
            System.out.printf("❌ 元數據錯誤: %s%n", metadata.getError());
            return;
        }
        
        System.out.printf("名稱: %s%n", metadata.getName());
        System.out.printf("符號: %s%n", metadata.getSymbol());
        System.out.printf("描述: %s%n", metadata.getDescription());
        
        if (metadata.getImageUrl() != null) {
            System.out.printf("圖片 URL: %s%n", metadata.getImageUrl());
        }
        
        System.out.printf("元數據標準: %s%n", metadata.getMetadataStandard());
        System.out.printf("有元數據: %s%n", metadata.isHasMetadata() ? "✅ 是" : "❌ 否");
        
        if (!metadata.getAdditionalAttributes().isEmpty()) {
            System.out.println("\n📋 額外屬性:");
            for (Map.Entry<String, String> entry : metadata.getAdditionalAttributes().entrySet()) {
                System.out.printf("   %s: %s%n", entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * 顯示 Token Extensions 資訊
     */
    private void displayTokenExtensions(TokenAnalysisService.TokenExtensionsInfo extensions) {
        if (extensions == null) {
            System.out.println("\n❌ Token Extensions 資訊不可用");
            return;
        }
        
        System.out.println("\n🔧 Token Extensions 分析:");
        System.out.println("-".repeat(50));
        
        if (extensions.getError() != null) {
            System.out.printf("❌ Extensions 錯誤: %s%n", extensions.getError());
            return;
        }
        
        System.out.printf("是否為 Token Extensions: %s%n", 
            extensions.isTokenExtensions() ? "✅ 是 (Token 2022)" : "❌ 否 (標準 SPL Token)");
        
        if (extensions.isTokenExtensions()) {
            System.out.println("\n🛠️ 已啟用的擴展功能:");
            
            if (extensions.isHasTransferFee()) {
                System.out.println("   🔸 轉帳手續費 (Transfer Fee)");
                displayTransferFeeDetails(extensions.getExtensionsData());
            }
            
            if (extensions.isHasMetadataPointer()) {
                System.out.println("   🔸 元數據指針 (Metadata Pointer)");
            }
            
            if (extensions.isHasTransferHook()) {
                System.out.println("   🔸 轉帳鉤子 (Transfer Hook)");
            }
            
            if (extensions.isHasPermanentDelegate()) {
                System.out.println("   🔸 永久委託人 (Permanent Delegate)");
            }
            
            if (!extensions.isHasTransferFee() && !extensions.isHasMetadataPointer() && 
                !extensions.isHasTransferHook() && !extensions.isHasPermanentDelegate()) {
                System.out.println("   📋 未檢測到標準擴展功能");
            }
            
            // 顯示原始擴展數據
            if (!extensions.getExtensionsData().isEmpty()) {
                System.out.println("\n📊 擴展數據詳情:");
                for (Map.Entry<String, Object> entry : extensions.getExtensionsData().entrySet()) {
                    System.out.printf("   %s: %s%n", entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    /**
     * 顯示轉帳手續費詳情
     */
    @SuppressWarnings("unchecked")
    private void displayTransferFeeDetails(Map<String, Object> extensionsData) {
        Object transferFeeObj = extensionsData.get("transferFee");
        if (transferFeeObj instanceof Map) {
            Map<String, Object> transferFee = (Map<String, Object>) transferFeeObj;
            
            if (transferFee.containsKey("feePercentage")) {
                System.out.printf("      手續費率: %.2f%%%n", transferFee.get("feePercentage"));
            }
            if (transferFee.containsKey("maxFee")) {
                System.out.printf("      最大手續費: %s%n", transferFee.get("maxFee"));
            }
        }
    }
    
    /**
     * 顯示持有者分析
     */
    private void displayHoldersAnalysis(TokenAnalysisService.TokenHoldersAnalysis holdersAnalysis) {
        if (holdersAnalysis == null) {
            System.out.println("\n❌ 持有者分析資訊不可用");
            return;
        }
        
        System.out.println("\n👥 持有者分析:");
        System.out.println("-".repeat(50));
        
        if (holdersAnalysis.getError() != null) {
            System.out.printf("❌ 持有者分析錯誤: %s%n", holdersAnalysis.getError());
            return;
        }
        
        System.out.printf("總持有者數量: %,d%n", holdersAnalysis.getTotalHolders());
        System.out.printf("持有集中度風險: %.1f%%%n", holdersAnalysis.getConcentrationRisk());
        
        if (!holdersAnalysis.getTopHolders().isEmpty()) {
            System.out.println("\n🏆 主要持有者:");
            for (int i = 0; i < Math.min(5, holdersAnalysis.getTopHolders().size()); i++) {
                TokenAnalysisService.TokenHolder holder = holdersAnalysis.getTopHolders().get(i);
                System.out.printf("   %d. %s%n", i + 1, holder.getOwnerAddress());
                System.out.printf("      持有量: %,.0f (%.1f%%)%n", holder.getBalance(), holder.getPercentage());
                System.out.printf("      類型: %s%n", holder.getHolderType());
            }
        }
    }
    
    /**
     * 顯示安全性分析
     */
    private void displaySecurityAnalysis(TokenAnalysisService.TokenSecurityAnalysis securityAnalysis) {
        if (securityAnalysis == null) {
            System.out.println("\n❌ 安全性分析資訊不可用");
            return;
        }
        
        System.out.println("\n🛡️ 安全性分析:");
        System.out.println("-".repeat(50));
        System.out.printf("風險評分: %d/100%n", securityAnalysis.getRiskScore());
        System.out.printf("整體風險等級: %s%n", securityAnalysis.getOverallRisk());
        
        if (!securityAnalysis.getPositiveFactors().isEmpty()) {
            System.out.println("\n✅ 安全因素:");
            for (String factor : securityAnalysis.getPositiveFactors()) {
                System.out.printf("   • %s%n", factor);
            }
        }
        
        if (!securityAnalysis.getRiskFactors().isEmpty()) {
            System.out.println("\n⚠️ 風險因素:");
            for (String risk : securityAnalysis.getRiskFactors()) {
                System.out.printf("   • %s%n", risk);
            }
        }
    }
    
    /**
     * 顯示批次分析摘要
     */
    private void displayBatchSummary(List<TokenAnalysisService.TokenAnalysisResult> results, 
                                   int successCount, int failedCount) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 批次代幣分析摘要報告");
        System.out.println("=".repeat(80));
        
        int totalCount = successCount + failedCount;
        System.out.printf("📈 總計分析: %d 個代幣%n", totalCount);
        System.out.printf("✅ 成功: %d 個 (%.1f%%)%n", 
            successCount, (double) successCount / totalCount * 100);
        System.out.printf("❌ 失敗: %d 個 (%.1f%%)%n", 
            failedCount, (double) failedCount / totalCount * 100);
        
        // 統計代幣類型
        int tokenExtensionsCount = 0;
        int standardTokenCount = 0;
        
        for (TokenAnalysisService.TokenAnalysisResult result : results) {
            if (result.getError() == null && result.getBasicInfo() != null) {
                if (result.getBasicInfo().isTokenExtensions()) {
                    tokenExtensionsCount++;
                } else {
                    standardTokenCount++;
                }
            }
        }
        
        if (tokenExtensionsCount > 0 || standardTokenCount > 0) {
            System.out.println("\n🏷️ 代幣類型分布:");
            if (standardTokenCount > 0) {
                System.out.printf("   標準 SPL Token: %d 個%n", standardTokenCount);
            }
            if (tokenExtensionsCount > 0) {
                System.out.printf("   Token Extensions: %d 個%n", tokenExtensionsCount);
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * 獲取擁有程式名稱
     */
    private String getOwnerProgramName(String programId) {
        if (programId == null) return "Unknown";
        
        switch (programId) {
            case "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA":
                return "標準 SPL Token Program";
            case "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb":
                return "Token Extensions Program (Token 2022)";
            default:
                return programId;
        }
    }
    
    /**
     * 獲取代幣顯示名稱
     */
    private String getTokenDisplayName(TokenAnalysisService.TokenAnalysisResult result) {
        if (result.getMetadata() != null && result.getMetadata().getSymbol() != null) {
            return result.getMetadata().getSymbol();
        }
        if (result.getBasicInfo() != null) {
            return result.getBasicInfo().getSymbol();
        }
        return "Unknown Token";
    }
    
    /**
     * 顯示連接狀態
     */
    private void displayConnectionStatus() {
        System.out.println("\n🔗 Solana 連接狀態:");
        System.out.println("-".repeat(50));
        System.out.printf("RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("網路: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "✅ 啟用" : "⚠️ 停用");
        System.out.println("-".repeat(50));
    }
    
    /**
     * 分析目標代幣
     */
    public void analyzeTargetToken() {
        analyzeToken(TARGET_TOKEN);
    }
    
    /**
     * 測試已知代幣
     */
    public void testKnownTokens() {
        List<String> testTokens = Arrays.asList(
            TARGET_TOKEN,                                                    // 目標代幣
            "So11111111111111111111111111111111111111112",                 // WSOL
            "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",                // USDC
            "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"                 // RAY
        );
        
        analyzeBatchTokens(testTokens);
    }
}