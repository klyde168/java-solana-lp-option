package com.example.java_solana_lp_option.runner;

import com.example.java_solana_lp_option.analyzer.CLMMPositionAnalyzer;
import com.example.java_solana_lp_option.config.SolanaConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//@Component
//@Order(2) // 在其他 Runner 之後執行
public class CLMMPositionRunner implements CommandLineRunner {

    private final CLMMPositionAnalyzer analyzer;
    private final SolanaConfig solanaConfig;
    
    // 更新預設的 CLMM Position NFT Mints (使用有效的 Position)
    private static final List<String> DEFAULT_CLMM_POSITIONS = Arrays.asList(
        "68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o", // 有效的 WSOL/USDC Position
        "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM", // 另一個常見的 Position
        "5Q544fKrFoe6tsEbD7S8EmxGTJYAKtTVhAW5Q5pge4j1"  // 第三個備用 Position
    );
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public CLMMPositionRunner(CLMMPositionAnalyzer analyzer, SolanaConfig solanaConfig) {
        this.analyzer = analyzer;
        this.solanaConfig = solanaConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 === CLMM Position 分析器啟動 ===");
        System.out.println("📅 執行時機：應用程式啟動時執行一次");
        System.out.println("🔧 功能：分析 Raydium CLMM Position NFT 的詳細資訊");
        
        // 顯示配置資訊
        System.out.println("\n📋 Solana 節點配置:");
        System.out.printf("   RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("   網路環境: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("   區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "✅ 啟用 (使用實際節點)" : "⚠️ 停用 (使用模擬數據)");
        System.out.printf("   連接超時: %d ms%n", solanaConfig.getConnectTimeout());
        System.out.printf("   讀取超時: %d ms%n", solanaConfig.getReadTimeout());
        System.out.printf("   最大重試: %d 次%n", solanaConfig.getMaxRetries());
        
        if (solanaConfig.isEnableBlockchainData()) {
            System.out.println("\n🔗 正在檢查 Solana 節點連接...");
            System.out.println("   節點連接將在分析時進行驗證");
        } else {
            System.out.println("\n💡 提示: 如需使用實際區塊鏈數據，請設定 solana.enableBlockchainData=true");
        }
        
        System.out.println("=".repeat(80));
        
        try {
            // 1. 單一 Position 分析示例
            System.out.println("\n🔵 第一部分：單一 CLMM Position 分析");
            System.out.println("-".repeat(50));
            analyzeSinglePosition();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 2. 批次 Position 分析示例
            System.out.println("\n🟢 第二部分：批次 CLMM Position 分析");
            System.out.println("-".repeat(50));
            analyzeBatchPositions();
            
            // 添加分隔線
            System.out.println("\n" + "=".repeat(80));
            
            // 3. 快速狀態檢查示例
            System.out.println("\n🟡 第三部分：快速狀態檢查");
            System.out.println("-".repeat(50));
            performQuickStatusChecks();
            
        } catch (Exception e) {
            System.err.println("❌ CLMM Position 分析過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ === CLMM Position 分析器執行完成 ===");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 分析單一 CLMM Position
     */
    private void analyzeSinglePosition() {
        try {
            String positionId = System.getenv("CLMM_POSITION_ID");
            if (positionId == null || positionId.trim().isEmpty()) {
                positionId = DEFAULT_CLMM_POSITIONS.get(0);
                System.out.printf("💡 使用預設 CLMM Position ID: %s%n", positionId);
                System.out.println("   如需分析其他倉位，請設定環境變數 CLMM_POSITION_ID");
            } else {
                System.out.printf("🎯 使用環境變數指定的 CLMM Position ID: %s%n", positionId);
            }
            
            analyzer.analyzeCLMMPosition(positionId);
            
        } catch (Exception e) {
            System.err.println("❌ 單一 Position 分析失敗: " + e.getMessage());
        }
    }
    
    /**
     * 批次分析多個 Positions
     */
    private void analyzeBatchPositions() {
        try {
            String batchIds = System.getenv("CLMM_BATCH_POSITION_IDS");
            List<String> positionIds;
            
            if (batchIds != null && !batchIds.trim().isEmpty()) {
                positionIds = Arrays.asList(batchIds.split(","));
                System.out.printf("🎯 使用環境變數指定的批次 Position IDs (%d 個)%n", positionIds.size());
                for (int i = 0; i < positionIds.size(); i++) {
                    System.out.printf("   %d. %s%n", i + 1, positionIds.get(i).trim());
                }
            } else {
                positionIds = DEFAULT_CLMM_POSITIONS;
                System.out.printf("💡 使用預設批次 Position IDs (%d 個)%n", positionIds.size());
                System.out.println("   如需分析其他倉位，請設定環境變數 CLMM_BATCH_POSITION_IDS (用逗號分隔)");
            }
            
            analyzer.analyzeBatchCLMMPositions(positionIds);
            
        } catch (Exception e) {
            System.err.println("❌ 批次 Position 分析失敗: " + e.getMessage());
        }
    }
    
    /**
     * 執行快速狀態檢查
     */
    private void performQuickStatusChecks() {
        try {
            System.out.println("🔍 執行所有 Position 的快速狀態檢查...");
            
            // 只檢查有效的 Position
            List<String> validPositions = getValidPositions();
            
            for (String positionId : validPositions) {
                analyzer.quickStatusCheck(positionId);
            }
            
            System.out.println("\n📊 生成 Position 摘要報告...");
            generateSummaryReport();
            
        } catch (Exception e) {
            System.err.println("❌ 快速狀態檢查失敗: " + e.getMessage());
        }
    }
    
    /**
     * 獲取有效的 Position 列表（過濾掉已知無效的）
     */
    private List<String> getValidPositions() {
        List<String> validPositions = new ArrayList<>();
        
        for (String positionId : DEFAULT_CLMM_POSITIONS) {
            // 過濾掉已知無效的 Position
            if (!"BSoUetj6UWvZFYrSnA9KsejAzQZWXUTfFCsB2EWk3LYh".equals(positionId)) {
                validPositions.add(positionId);
            }
        }
        
        // 如果所有預設 Position 都無效，至少保留一個已知有效的
        if (validPositions.isEmpty()) {
            validPositions.add("68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o");
        }
        
        return validPositions;
    }
    
    /**
     * 生成摘要報告
     */
    private void generateSummaryReport() {
        try {
            System.out.println("\n📋 CLMM Position 摘要報告");
            System.out.println("-".repeat(60));
            
            double totalValue = 0;
            double totalUnclaimedFees = 0;
            int successCount = 0;
            int failedCount = 0;
            
            List<String> validPositions = getValidPositions();
            
            for (String positionId : validPositions) {
                Map<String, Object> summary = analyzer.getPositionSummary(positionId);
                
                String status = (String) summary.get("status");
                String shortId = positionId.length() > 12 ? 
                    positionId.substring(0, 6) + "..." + positionId.substring(positionId.length() - 6) : 
                    positionId;
                
                System.out.printf("Position %s: ", shortId);
                
                if ("success".equals(status)) {
                    successCount++;
                    String tokenPair = (String) summary.getOrDefault("tokenPair", "Unknown");
                    Double usdValue = (Double) summary.get("usdValue");
                    Double unclaimedFeesUSD = (Double) summary.get("unclaimedFeesUSD");
                    
                    System.out.printf("✅ %s", tokenPair);
                    
                    if (usdValue != null) {
                        System.out.printf(" | 價值: %s", formatCurrency(usdValue));
                        totalValue += usdValue;
                    }
                    
                    if (unclaimedFeesUSD != null) {
                        System.out.printf(" | 未領收益: %s", formatCurrency(unclaimedFeesUSD));
                        totalUnclaimedFees += unclaimedFeesUSD;
                    }
                    
                    System.out.println();
                } else {
                    failedCount++;
                    String error = (String) summary.getOrDefault("error", "未知錯誤");
                    System.out.printf("❌ 失敗 (%s)%n", error);
                }
            }
            
            // 顯示總計
            System.out.println("-".repeat(60));
            System.out.printf("📊 統計摘要:%n");
            System.out.printf("   成功分析: %d 個 Position%n", successCount);
            System.out.printf("   失敗: %d 個 Position%n", failedCount);
            System.out.printf("   總價值: %s%n", formatCurrency(totalValue));
            System.out.printf("   總未領收益: %s%n", formatCurrency(totalUnclaimedFees));
            
            if (totalValue > 0) {
                double unclaimedPercentage = (totalUnclaimedFees / totalValue) * 100;
                System.out.printf("   未領收益比例: %.2f%%%n", unclaimedPercentage);
                
                // 提供管理建議
                if (unclaimedPercentage > 2.0) {
                    System.out.println("\n💡 管理建議:");
                    System.out.printf("   🚨 未領收益比例 %.2f%% 較高，建議考慮領取手續費%n", unclaimedPercentage);
                } else if (totalUnclaimedFees > 50) {
                    System.out.println("\n💡 管理建議:");
                    System.out.printf("   ⚠️ 未領收益金額 %s 較大，可考慮定期領取%n", formatCurrency(totalUnclaimedFees));
                } else {
                    System.out.println("\n✅ Position 狀態良好，未領收益在合理範圍內");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ 生成摘要報告失敗: " + e.getMessage());
        }
    }
    
    /**
     * 定時任務：每8小時執行一次 Position 檢查
     */
    @Scheduled(cron = "0 0 */8 * * *")
    public void scheduledPositionCheck() {
        System.out.printf("%n⏰ CLMM Position 定時檢查 - %s%n", LocalDateTime.now().format(FORMATTER));
        
        try {
            performQuickStatusChecks();
        } catch (Exception e) {
            System.err.printf("❌ 定時 Position 檢查失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 手動觸發 Position 分析的方法（供其他服務呼叫）
     */
    public void manualAnalyze(String type, String positionId) {
        System.out.printf("%n🔧 手動觸發 %s 分析...%n", type.toUpperCase());
        
        try {
            switch (type.toLowerCase()) {
                case "single":
                    System.out.println("📊 執行單一 Position 分析...");
                    String targetId = (positionId != null && !positionId.trim().isEmpty()) 
                                     ? positionId 
                                     : DEFAULT_CLMM_POSITIONS.get(0);
                    analyzer.analyzeCLMMPosition(targetId);
                    break;
                    
                case "batch":
                    System.out.println("📊 執行批次 Position 分析...");
                    if (positionId != null && !positionId.trim().isEmpty()) {
                        List<String> ids = Arrays.asList(positionId.split(","));
                        analyzer.analyzeBatchCLMMPositions(ids);
                    } else {
                        analyzer.analyzeBatchCLMMPositions(getValidPositions());
                    }
                    break;
                    
                case "quick":
                    System.out.println("📊 執行快速狀態檢查...");
                    if (positionId != null && !positionId.trim().isEmpty()) {
                        analyzer.quickStatusCheck(positionId);
                    } else {
                        performQuickStatusChecks();
                    }
                    break;
                    
                case "summary":
                    System.out.println("📊 生成摘要報告...");
                    generateSummaryReport();
                    break;
                    
                default:
                    System.err.printf("❌ 不支援的分析類型: %s%n", type);
                    System.out.println("💡 支援的類型: single, batch, quick, summary");
                    return;
            }
            
            System.out.printf("✅ %s 分析完成%n", type.toUpperCase());
            
        } catch (Exception e) {
            System.err.printf("❌ 手動 %s 分析失敗: %s%n", type.toUpperCase(), e.getMessage());
        }
    }
    
    /**
     * 提供快速 Position 分析方法
     */
    public void quickPositionAnalysis(String positionId) {
        if (positionId == null || positionId.trim().isEmpty()) {
            positionId = DEFAULT_CLMM_POSITIONS.get(0);
        }
        
        System.out.printf("%n⚡ 快速 CLMM Position 分析: %s%n", positionId);
        try {
            analyzer.quickStatusCheck(positionId);
            
            // 也顯示詳細摘要
            Map<String, Object> summary = analyzer.getPositionSummary(positionId);
            displayDetailedSummary(summary);
            
        } catch (Exception e) {
            System.err.printf("❌ 快速 Position 分析失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 顯示詳細摘要
     */
    private void displayDetailedSummary(Map<String, Object> summary) {
        System.out.println("\n📋 詳細摘要:");
        System.out.println("-".repeat(30));
        
        String status = (String) summary.get("status");
        if ("success".equals(status)) {
            System.out.printf("狀態: ✅ 成功%n");
            System.out.printf("名稱: %s%n", summary.getOrDefault("name", "N/A"));
            System.out.printf("符號: %s%n", summary.getOrDefault("symbol", "N/A"));
            System.out.printf("代幣對: %s%n", summary.getOrDefault("tokenPair", "N/A"));
            System.out.printf("池 ID: %s%n", summary.getOrDefault("poolId", "N/A"));
            
            Double tvl = (Double) summary.get("tvl");
            if (tvl != null) {
                System.out.printf("池 TVL: %s%n", formatCurrency(tvl));
            }
            
            Double feeRate = (Double) summary.get("feeRate");
            if (feeRate != null) {
                System.out.printf("手續費率: %.3f%%%n", feeRate * 100);
            }
            
            Double usdValue = (Double) summary.get("usdValue");
            if (usdValue != null) {
                System.out.printf("位置價值: %s%n", formatCurrency(usdValue));
            }
            
            Double tvlPercentage = (Double) summary.get("tvlPercentage");
            if (tvlPercentage != null) {
                System.out.printf("TVL 佔比: %.4f%%%n", tvlPercentage);
            }
            
            Double unclaimedFeesUSD = (Double) summary.get("unclaimedFeesUSD");
            if (unclaimedFeesUSD != null) {
                System.out.printf("未領收益: %s%n", formatCurrency(unclaimedFeesUSD));
            }
            
        } else {
            System.out.printf("狀態: ❌ 失敗%n");
            System.out.printf("錯誤: %s%n", summary.getOrDefault("error", "未知錯誤"));
        }
    }
    
    /**
     * 格式化貨幣顯示
     */
    private String formatCurrency(double value) {
        if (value >= 1000000) {
            return String.format("$%.2fM", value / 1000000);
        } else if (value >= 1000) {
            return String.format("$%.2fK", value / 1000);
        } else {
            return String.format("$%.2f", value);
        }
    }
    
    /**
     * 獲取所有有效 Position 的狀態
     */
    public List<Map<String, Object>> getAllPositionStatus() {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (String positionId : getValidPositions()) {
            try {
                Map<String, Object> summary = analyzer.getPositionSummary(positionId);
                results.add(summary);
            } catch (Exception e) {
                Map<String, Object> errorSummary = new HashMap<>();
                errorSummary.put("mintAddress", positionId);
                errorSummary.put("status", "error");
                errorSummary.put("error", e.getMessage());
                results.add(errorSummary);
            }
        }
        
        return results;
    }
    
    /**
     * 檢查特定 Position 是否需要管理
     */
    public void checkPositionManagement(String positionId) {
        System.out.printf("🔍 檢查 Position 管理建議: %s%n", positionId);
        
        try {
            Map<String, Object> summary = analyzer.getPositionSummary(positionId);
            
            if ("success".equals(summary.get("status"))) {
                Double unclaimedFeesUSD = (Double) summary.get("unclaimedFeesUSD");
                Double usdValue = (Double) summary.get("usdValue");
                
                if (unclaimedFeesUSD != null && usdValue != null) {
                    double feePercentage = (unclaimedFeesUSD / usdValue) * 100;
                    
                    System.out.printf("💰 未領收益比例: %.2f%%%n", feePercentage);
                    
                    if (feePercentage > 5.0) {
                        System.out.println("🚨 建議: 未領收益比例較高，建議領取手續費");
                    } else if (feePercentage > 2.0) {
                        System.out.println("⚠️ 提醒: 有一定數量的未領收益，可考慮領取");
                    } else {
                        System.out.println("✅ 狀態: 未領收益比例正常");
                    }
                    
                    if (unclaimedFeesUSD > 100) {
                        System.out.printf("💡 提示: 未領收益金額 %s 較大，建議定期領取%n", 
                            formatCurrency(unclaimedFeesUSD));
                    }
                }
            } else {
                System.out.println("❌ 無法獲取 Position 狀態進行管理建議");
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 檢查 Position 管理建議失敗: %s%n", e.getMessage());
        }
    }
}