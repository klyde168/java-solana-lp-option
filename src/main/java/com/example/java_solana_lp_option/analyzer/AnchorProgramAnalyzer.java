package com.example.java_solana_lp_option.analyzer;

import com.example.java_solana_lp_option.config.SolanaConfig;
import com.example.java_solana_lp_option.service.AnchorProgramService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Anchor 程式專用分析器 - 專門分析和展示 Anchor 程式數據
 */
@Component
public class AnchorProgramAnalyzer {
    
    private final AnchorProgramService anchorProgramService;
    private final SolanaConfig solanaConfig;
    
    // 測試用的已知 Anchor 程式帳戶
    private static final List<String> TEST_ANCHOR_ACCOUNTS = Arrays.asList(
        "3tgWY4ZcaLYE3jqp2fMaYiinPADmZFRCXLhmLwMpCBGu", // 目標帳戶
        "68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o", // CLMM Position
        "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM", // 另一個 Position
        "8sLbNZoA1cfnvMJLPfp98ZLAnFSYCFApfJKMbiXNLwxj"  // Pool 帳戶
    );
    
    public AnchorProgramAnalyzer(AnchorProgramService anchorProgramService, SolanaConfig solanaConfig) {
        this.anchorProgramService = anchorProgramService;
        this.solanaConfig = solanaConfig;
    }
    
    /**
     * 分析單一帳戶的 Anchor 程式數據
     */
    public void analyzeAnchorAccount(String accountAddress) {
        System.out.println("🔗 Anchor 程式帳戶分析器");
        System.out.println("=".repeat(80));
        System.out.printf("🎯 分析帳戶: %s%n", accountAddress);
        
        displayConnectionStatus();
        
        try {
            AnchorProgramService.AnchorProgramAnalysis analysis = 
                anchorProgramService.analyzeAccountAnchorData(accountAddress);
            
            if (analysis != null) {
                displayAnchorAnalysis(analysis);
            } else {
                System.out.println("❌ 該帳戶不是已知的 Anchor 程式帳戶");
                displayNonAnchorInfo(accountAddress);
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 分析過程發生錯誤: %s%n", e.getMessage());
        }
    }
    
    /**
     * 批次分析多個帳戶
     */
    public void analyzeBatchAnchorAccounts(List<String> accountAddresses) {
        System.out.println("🚀 批次 Anchor 程式分析器");
        System.out.println("=".repeat(80));
        System.out.printf("📊 將分析 %d 個帳戶%n", accountAddresses.size());
        
        int anchorCount = 0;
        int nonAnchorCount = 0;
        Map<String, Integer> programTypeCount = new HashMap<>();
        
        for (int i = 0; i < accountAddresses.size(); i++) {
            String accountAddress = accountAddresses.get(i);
            System.out.printf("\n🔄 [%d/%d] 分析帳戶: %s%n", i + 1, accountAddresses.size(), accountAddress);
            
            try {
                AnchorProgramService.AnchorProgramAnalysis analysis = 
                    anchorProgramService.analyzeAccountAnchorData(accountAddress);
                
                if (analysis != null && analysis.getProgramInfo() != null) {
                    anchorCount++;
                    String programType = analysis.getProgramInfo().getType();
                    programTypeCount.put(programType, programTypeCount.getOrDefault(programType, 0) + 1);
                    
                    System.out.printf("✅ Anchor 程式: %s (%s)%n", 
                        analysis.getProgramInfo().getName(), programType);
                } else {
                    nonAnchorCount++;
                    System.out.println("⚪ 非 Anchor 程式帳戶");
                }
                
                // 批次處理間隔
                if (i < accountAddresses.size() - 1) {
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                nonAnchorCount++;
                System.err.printf("❌ 分析失敗: %s%n", e.getMessage());
            }
        }
        
        // 顯示批次分析摘要
        displayBatchSummary(anchorCount, nonAnchorCount, programTypeCount);
    }
    
    /**
     * 程式統計和總覽
     */
    public void displayProgramOverview() {
        System.out.println("📊 Anchor 程式系統總覽");
        System.out.println("=".repeat(80));
        
        try {
            Map<String, Object> stats = anchorProgramService.getProgramStatistics();
            
            System.out.printf("🏗️ 已知程式數量: %d%n", stats.get("knownProgramsCount"));
            System.out.printf("💾 快取程式數量: %d%n", stats.get("cachedProgramsCount"));
            
            @SuppressWarnings("unchecked")
            List<String> supportedTypes = (List<String>) stats.get("supportedTypes");
            System.out.printf("🔧 支援類型: %s%n", String.join(", ", supportedTypes));
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> typeDistribution = (Map<String, Integer>) stats.get("typeDistribution");
            if (!typeDistribution.isEmpty()) {
                System.out.println("\n📈 程式類型分布:");
                for (Map.Entry<String, Integer> entry : typeDistribution.entrySet()) {
                    System.out.printf("   %s: %d 個程式%n", entry.getKey(), entry.getValue());
                }
            }
            
            displaySupportedPrograms();
            
        } catch (Exception e) {
            System.err.printf("❌ 獲取統計資訊失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 測試已知帳戶
     */
    public void testKnownAccounts() {
        System.out.println("🧪 測試已知 Anchor 程式帳戶");
        System.out.println("=".repeat(80));
        
        analyzeBatchAnchorAccounts(TEST_ANCHOR_ACCOUNTS);
    }
    
    /**
     * 顯示詳細的 Anchor 分析結果
     */
    private void displayAnchorAnalysis(AnchorProgramService.AnchorProgramAnalysis analysis) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 Anchor 程式詳細分析報告");
        System.out.println("=".repeat(80));
        
        AnchorProgramService.AnchorProgramInfo programInfo = analysis.getProgramInfo();
        
        // 程式基本資訊
        System.out.printf("🏗️ 程式名稱: %s%n", programInfo.getName());
        System.out.printf("🆔 程式 ID: %s%n", programInfo.getProgramId());
        System.out.printf("🏷️ 程式類型: %s%n", programInfo.getType());
        
        if (programInfo.getVersion() != null) {
            System.out.printf("📦 版本: %s%n", programInfo.getVersion());
        }
        
        if (programInfo.getDescription() != null) {
            System.out.printf("📝 描述: %s%n", programInfo.getDescription());
        }
        
        // 支援指令
        if (!programInfo.getInstructions().isEmpty()) {
            System.out.println("\n⚙️ 支援指令:");
            for (String instruction : programInfo.getInstructions()) {
                System.out.printf("   • %s%n", instruction);
            }
        }
        
        // 帳戶資訊
        System.out.println("\n💾 帳戶資訊:");
        System.out.printf("   地址: %s%n", analysis.getAccountAddress());
        System.out.printf("   擁有者: %s%n", analysis.getOwner());
        System.out.printf("   數據大小: %d bytes%n", analysis.getDataSize());
        
        // 解析的數據
        Map<String, Object> parsedData = analysis.getParsedData();
        if (!parsedData.isEmpty()) {
            System.out.println("\n🔍 解析的程式數據:");
            displayParsedData(parsedData, "   ");
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 分析完成！");
    }
    
    /**
     * 顯示解析的數據
     */
    private void displayParsedData(Map<String, Object> data, String indent) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                System.out.printf("%s%s:%n", indent, key);
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                displayParsedData(nestedMap, indent + "  ");
            } else if (value instanceof List) {
                System.out.printf("%s%s: %s%n", indent, key, value.toString());
            } else {
                System.out.printf("%s%s: %s%n", indent, key, value);
            }
        }
    }
    
    /**
     * 顯示非 Anchor 程式帳戶資訊
     */
    private void displayNonAnchorInfo(String accountAddress) {
        System.out.println("\n📋 帳戶基本資訊:");
        System.out.printf("   地址: %s%n", accountAddress);
        System.out.println("   類型: 非 Anchor 程式帳戶");
        System.out.println("\n💡 可能的情況:");
        System.out.println("   • 這是一個標準的 Solana 帳戶");
        System.out.println("   • 這是一個代幣帳戶");
        System.out.println("   • 這是一個非 Anchor 框架開發的程式帳戶");
        System.out.println("   • 程式尚未被我們的系統識別");
    }
    
    /**
     * 顯示批次分析摘要
     */
    private void displayBatchSummary(int anchorCount, int nonAnchorCount, Map<String, Integer> programTypeCount) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 批次分析摘要報告");
        System.out.println("=".repeat(80));
        
        int totalCount = anchorCount + nonAnchorCount;
        System.out.printf("📈 總計分析: %d 個帳戶%n", totalCount);
        System.out.printf("✅ Anchor 程式: %d 個 (%.1f%%)%n", 
            anchorCount, (double) anchorCount / totalCount * 100);
        System.out.printf("⚪ 非 Anchor: %d 個 (%.1f%%)%n", 
            nonAnchorCount, (double) nonAnchorCount / totalCount * 100);
        
        if (!programTypeCount.isEmpty()) {
            System.out.println("\n🏷️ Anchor 程式類型分布:");
            for (Map.Entry<String, Integer> entry : programTypeCount.entrySet()) {
                System.out.printf("   %s: %d 個%n", entry.getKey(), entry.getValue());
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * 顯示支援的程式清單
     */
    private void displaySupportedPrograms() {
        System.out.println("\n🏗️ 支援的 Anchor 程式:");
        System.out.println("-".repeat(50));
        
        System.out.println("🔸 Raydium 生態:");
        System.out.println("   • Raydium CLMM - 集中流動性市場製造商");
        System.out.println("   • Raydium AMM V4 - 自動化市場製造商");
        System.out.println("   • Raydium Pool - 流動性池");
        
        System.out.println("\n🔸 Orca 生態:");
        System.out.println("   • Orca Whirlpool - 集中流動性");
        System.out.println("   • Orca Pool - 標準流動性池");
        
        System.out.println("\n🔸 其他 DeFi 協議:");
        System.out.println("   • Jupiter - 流動性聚合器");
        System.out.println("   • Meteora - 動態流動性市場製造商");
        System.out.println("   • Serum DEX - 去中心化交易所");
        System.out.println("   • Mango Markets - 去中心化交易平台");
        System.out.println("   • Solend - 借貸協議");
        System.out.println("   • Phoenix DEX - 新一代 DEX");
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
     * 分析目標帳戶
     */
    public void analyzeTargetAccount() {
        analyzeAnchorAccount("3tgWY4ZcaLYE3jqp2fMaYiinPADmZFRCXLhmLwMpCBGu");
    }
}