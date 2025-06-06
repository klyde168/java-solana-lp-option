package com.example.java_solana_lp_option.service;

import com.example.java_solana_lp_option.config.SolanaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

/**
 * Anchor 程式數據服務 - 專門處理 Anchor 程式的識別、IDL 解析和帳戶數據解碼
 */
@Service
public class AnchorProgramService {
    
    private final SolanaService solanaService;
    private final SolanaConfig solanaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // 快取已知的程式資訊
    private final Map<String, AnchorProgramInfo> programCache = new ConcurrentHashMap<>();
    
    // 已知的 Anchor 程式清單
    private static final Map<String, String> KNOWN_ANCHOR_PROGRAMS = new HashMap<>();
    
    static {
        // Raydium
        KNOWN_ANCHOR_PROGRAMS.put("CAMMCzo5YL8w4VFF8KVHrK22GGUsp5VTaW7grrKgrWqK", "Raydium CLMM");
        KNOWN_ANCHOR_PROGRAMS.put("675kPX9MHTjS2zt1qfr1NYHuzeLXfQM9H24wFSUt1Mp8", "Raydium AMM V4");
        KNOWN_ANCHOR_PROGRAMS.put("5Q544fKrFoe6tsEbD7S8EmxGTJYAKtTVhAW5Q5pge4j1", "Raydium Pool");
        
        // Orca
        KNOWN_ANCHOR_PROGRAMS.put("whirLbMiicVdio4qvUfM5KAg6Ct8VwpYzGff3uctyCc", "Orca Whirlpool");
        KNOWN_ANCHOR_PROGRAMS.put("9W959DqEETiGZocYWCQPaJ6sBmUzgfxXfqGeTEdp3aQP", "Orca Pool");
        
        // Jupiter
        KNOWN_ANCHOR_PROGRAMS.put("JUP6LkbZbjS1jKKwapdHNy74zcZ3tLUZoi5QNyVTaV4", "Jupiter V6");
        KNOWN_ANCHOR_PROGRAMS.put("JUP4Fb2cqiRUcaTHdrPC8h2gNsA2ETXiPDD33WcGuJB", "Jupiter V4");
        
        // Meteora
        KNOWN_ANCHOR_PROGRAMS.put("Eo7WjKq67rjJQSZxS6z3YkapzY3eMj6Xy8X5EQVn5UaB", "Meteora DLMM");
        KNOWN_ANCHOR_PROGRAMS.put("24Uqj9JCLxUeoC3hGfh5W3s9FM9uCHDS2SG3LYwBpyTi", "Meteora Pool");
        
        // Serum
        KNOWN_ANCHOR_PROGRAMS.put("9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin", "Serum DEX V3");
        KNOWN_ANCHOR_PROGRAMS.put("EhpbDdUQ7peFg1rGTGNmcPG93FzyWnKMvBXbXDjqLKKU", "Serum Pool");
        
        // Mango
        KNOWN_ANCHOR_PROGRAMS.put("mv3ekLzLbnVPNxjSKvqBpU3ZeZXPQdEC3bp5MDEBG68", "Mango V3");
        KNOWN_ANCHOR_PROGRAMS.put("4MangoMjqJ2firMokCjjGgoK8d4MXcrgL7XJaL3w6fVg", "Mango V4");
        
        // Solend
        KNOWN_ANCHOR_PROGRAMS.put("So1endDq2YkqhipRh3WViPa8hdiSpxWy6z3Z6tMCpAo", "Solend");
        
        // Phoenix
        KNOWN_ANCHOR_PROGRAMS.put("PhoeNiXZ8ByJGLkxNfZRnkUfjvmuYqLR89jjFHGqdXY", "Phoenix DEX");
    }
    
    public AnchorProgramService(SolanaService solanaService, SolanaConfig solanaConfig) {
        this.solanaService = solanaService;
        this.solanaConfig = solanaConfig;
        this.objectMapper = new ObjectMapper();
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(solanaConfig.getConnectTimeout());
        factory.setReadTimeout(solanaConfig.getReadTimeout());
        this.restTemplate = new RestTemplate(factory);
    }
    
    /**
     * 主要入口：分析帳戶的 Anchor 程式數據
     */
    public AnchorProgramAnalysis analyzeAccountAnchorData(String accountAddress) {
        try {
            System.out.println("🔍 分析帳戶的 Anchor 程式數據...");
            
            // 1. 獲取帳戶基本資訊
            JsonNode accountInfo = solanaService.getAccountInfo(accountAddress);
            if (accountInfo == null || accountInfo.get("value").isNull()) {
                System.out.println("❌ 帳戶不存在");
                return null;
            }
            
            JsonNode value = accountInfo.get("value");
            String owner = value.get("owner").asText();
            
            // 2. 檢查是否為已知的 Anchor 程式
            AnchorProgramInfo programInfo = identifyAnchorProgram(owner);
            if (programInfo == null) {
                System.out.printf("⚠️ 程式 %s 不是已知的 Anchor 程式%n", owner);
                return null;
            }
            
            // 3. 解析帳戶數據
            String data = value.get("data").get(0).asText();
            byte[] accountData = Base64.getDecoder().decode(data);
            
            // 4. 根據程式類型解析數據
            Map<String, Object> parsedData = parseAccountData(programInfo, accountData, accountAddress);
            
            // 5. 創建分析結果
            AnchorProgramAnalysis analysis = new AnchorProgramAnalysis();
            analysis.setAccountAddress(accountAddress);
            analysis.setProgramInfo(programInfo);
            analysis.setParsedData(parsedData);
            analysis.setDataSize(accountData.length);
            analysis.setOwner(owner);
            
            System.out.printf("✅ 成功分析 %s 程式的帳戶數據%n", programInfo.getName());
            return analysis;
            
        } catch (Exception e) {
            System.err.printf("❌ 分析 Anchor 程式數據失敗: %s%n", e.getMessage());
            return null;
        }
    }
    
    /**
     * 識別 Anchor 程式
     */
    private AnchorProgramInfo identifyAnchorProgram(String programId) {
        // 檢查快取
        if (programCache.containsKey(programId)) {
            return programCache.get(programId);
        }
        
        // 檢查已知程式
        if (KNOWN_ANCHOR_PROGRAMS.containsKey(programId)) {
            AnchorProgramInfo info = new AnchorProgramInfo();
            info.setProgramId(programId);
            info.setName(KNOWN_ANCHOR_PROGRAMS.get(programId));
            info.setType(determineProjectType(info.getName()));
            info.setInstructions(getKnownInstructions(programId));
            
            // 嘗試獲取更詳細的資訊
            enrichProgramInfo(info);
            
            programCache.put(programId, info);
            return info;
        }
        
        // 嘗試從鏈上獲取程式資訊
        AnchorProgramInfo info = fetchProgramFromChain(programId);
        if (info != null) {
            programCache.put(programId, info);
        }
        
        return info;
    }
    
    /**
     * 從鏈上獲取程式資訊
     */
    private AnchorProgramInfo fetchProgramFromChain(String programId) {
        try {
            System.out.printf("🔍 從鏈上獲取程式資訊: %s%n", programId);
            
            JsonNode accountInfo = solanaService.getAccountInfo(programId);
            if (accountInfo != null && !accountInfo.get("value").isNull()) {
                JsonNode value = accountInfo.get("value");
                boolean executable = value.get("executable").asBoolean();
                
                if (executable) {
                    AnchorProgramInfo info = new AnchorProgramInfo();
                    info.setProgramId(programId);
                    info.setName("Unknown Anchor Program");
                    info.setType("DeFi");
                    info.setExecutable(true);
                    
                    // 嘗試識別是否為 Anchor 程式
                    if (isLikelyAnchorProgram(value)) {
                        System.out.println("✅ 檢測到疑似 Anchor 程式");
                        return info;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.printf("⚠️ 從鏈上獲取程式資訊失敗: %s%n", e.getMessage());
            return null;
        }
    }
    
    /**
     * 檢測是否為 Anchor 程式
     */
    private boolean isLikelyAnchorProgram(JsonNode programAccount) {
        try {
            // Anchor 程式通常會有特定的數據結構
            String data = programAccount.get("data").get(0).asText();
            byte[] programData = Base64.getDecoder().decode(data);
            
            // 檢查 ELF header 或其他 Anchor 特徵
            if (programData.length > 4) {
                // 檢查 ELF magic number
                if (programData[0] == 0x7F && programData[1] == 'E' && 
                    programData[2] == 'L' && programData[3] == 'F') {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析帳戶數據
     */
    private Map<String, Object> parseAccountData(AnchorProgramInfo programInfo, byte[] data, String accountAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.printf("🔍 解析 %s 程式的帳戶數據 (長度: %d)%n", programInfo.getName(), data.length);
            
            // 根據程式類型使用不同的解析器
            switch (programInfo.getType()) {
                case "CLMM":
                    result = parseCLMMAccountData(data, accountAddress);
                    break;
                case "AMM":
                    result = parseAMMAccountData(data, accountAddress);
                    break;
                case "DEX":
                    result = parseDEXAccountData(data, accountAddress);
                    break;
                case "Lending":
                    result = parseLendingAccountData(data, accountAddress);
                    break;
                default:
                    result = parseGenericAnchorData(data, accountAddress);
                    break;
            }
            
            // 添加基本資訊
            result.put("programId", programInfo.getProgramId());
            result.put("programName", programInfo.getName());
            result.put("accountAddress", accountAddress);
            result.put("dataSize", data.length);
            result.put("timestamp", System.currentTimeMillis());
            
            return result;
            
        } catch (Exception e) {
            System.err.printf("❌ 解析帳戶數據失敗: %s%n", e.getMessage());
            
            // 返回基本資訊
            result.put("error", e.getMessage());
            result.put("programId", programInfo.getProgramId());
            result.put("programName", programInfo.getName());
            return result;
        }
    }
    
    /**
     * 解析 CLMM 帳戶數據
     */
    private Map<String, Object> parseCLMMAccountData(byte[] data, String accountAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            
            // Raydium CLMM Position 帳戶結構解析
            if (data.length >= 264) {
                // 跳過帳戶判別器 (8 bytes)
                buffer.position(8);
                
                // Pool ID (32 bytes)
                byte[] poolId = new byte[32];
                buffer.get(poolId);
                result.put("poolId", Base64.getEncoder().encodeToString(poolId));
                
                // Position Owner (32 bytes)
                byte[] owner = new byte[32];
                buffer.get(owner);
                result.put("positionOwner", Base64.getEncoder().encodeToString(owner));
                
                // Tick Lower (4 bytes)
                int tickLower = buffer.getInt();
                result.put("tickLower", tickLower);
                
                // Tick Upper (4 bytes)
                int tickUpper = buffer.getInt();
                result.put("tickUpper", tickUpper);
                
                // Liquidity (16 bytes as 2 longs)
                long liquidityLow = buffer.getLong();
                long liquidityHigh = buffer.getLong();
                result.put("liquidityLow", liquidityLow);
                result.put("liquidityHigh", liquidityHigh);
                
                // Fee Growth Inside Last (16 bytes each for token A and B)
                long feeGrowthInside0LastX64Low = buffer.getLong();
                long feeGrowthInside0LastX64High = buffer.getLong();
                result.put("feeGrowthInside0LastX64Low", feeGrowthInside0LastX64Low);
                result.put("feeGrowthInside0LastX64High", feeGrowthInside0LastX64High);
                
                long feeGrowthInside1LastX64Low = buffer.getLong();
                long feeGrowthInside1LastX64High = buffer.getLong();
                result.put("feeGrowthInside1LastX64Low", feeGrowthInside1LastX64Low);
                result.put("feeGrowthInside1LastX64High", feeGrowthInside1LastX64High);
                
                // Tokens Owed (8 bytes each)
                long tokensOwed0 = buffer.getLong();
                long tokensOwed1 = buffer.getLong();
                result.put("tokensOwed0", tokensOwed0);
                result.put("tokensOwed1", tokensOwed1);
                
                System.out.println("✅ 成功解析 CLMM Position 數據");
                result.put("type", "CLMM_Position");
                
                // 計算價格範圍
                try {
                    double lowerPrice = Math.pow(1.0001, tickLower);
                    double upperPrice = Math.pow(1.0001, tickUpper);
                    result.put("lowerPrice", lowerPrice);
                    result.put("upperPrice", upperPrice);
                    result.put("priceRange", String.format("%.6f - %.6f", lowerPrice, upperPrice));
                } catch (Exception e) {
                    System.out.printf("⚠️ 計算價格範圍失敗: %s%n", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 解析 CLMM 數據失敗: %s%n", e.getMessage());
            result.put("error", "CLMM data parsing failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 解析 AMM 帳戶數據
     */
    private Map<String, Object> parseAMMAccountData(byte[] data, String accountAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            
            // 基本的 AMM 池結構解析
            if (data.length >= 128) {
                // 帳戶判別器 (8 bytes)
                buffer.position(8);
                
                // 狀態 (1 byte)
                byte status = buffer.get();
                result.put("status", status);
                
                // Nonce (1 byte)
                byte nonce = buffer.get();
                result.put("nonce", nonce);
                
                // Token A 數量 (8 bytes)
                long tokenAAmount = buffer.getLong();
                result.put("tokenAAmount", tokenAAmount);
                
                // Token B 數量 (8 bytes)
                long tokenBAmount = buffer.getLong();
                result.put("tokenBAmount", tokenBAmount);
                
                result.put("type", "AMM_Pool");
                System.out.println("✅ 成功解析 AMM Pool 數據");
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 解析 AMM 數據失敗: %s%n", e.getMessage());
            result.put("error", "AMM data parsing failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 解析 DEX 帳戶數據
     */
    private Map<String, Object> parseDEXAccountData(byte[] data, String accountAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("type", "DEX_Account");
            result.put("dataSize", data.length);
            
            // DEX 帳戶通常包含訂單簿或市場資訊
            if (data.length >= 64) {
                ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                
                // 基本結構解析
                buffer.position(8); // 跳過判別器
                
                // Market 狀態或其他 DEX 特定資訊
                result.put("note", "DEX account detected, specific parsing requires market type identification");
            }
            
            System.out.println("✅ 檢測到 DEX 帳戶");
            
        } catch (Exception e) {
            result.put("error", "DEX data parsing failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 解析 Lending 帳戶數據
     */
    private Map<String, Object> parseLendingAccountData(byte[] data, String accountAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("type", "Lending_Account");
            result.put("dataSize", data.length);
            
            if (data.length >= 64) {
                ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                buffer.position(8); // 跳過判別器
                
                // 基本的借貸帳戶資訊
                result.put("note", "Lending account detected, specific parsing requires protocol identification");
            }
            
            System.out.println("✅ 檢測到 Lending 帳戶");
            
        } catch (Exception e) {
            result.put("error", "Lending data parsing failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 通用 Anchor 數據解析
     */
    private Map<String, Object> parseGenericAnchorData(byte[] data, String accountAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("type", "Generic_Anchor_Account");
            result.put("dataSize", data.length);
            
            // 檢查帳戶判別器
            if (data.length >= 8) {
                ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                long discriminator = buffer.getLong();
                result.put("discriminator", discriminator);
                result.put("discriminatorHex", String.format("0x%016X", discriminator));
            }
            
            // 提供原始數據的一些統計資訊
            result.put("hasNonZeroData", hasNonZeroData(data));
            result.put("dataPreview", getDataPreview(data));
            
            System.out.println("✅ 完成通用 Anchor 數據解析");
            
        } catch (Exception e) {
            result.put("error", "Generic Anchor data parsing failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 檢查數據是否包含非零值
     */
    private boolean hasNonZeroData(byte[] data) {
        for (byte b : data) {
            if (b != 0) return true;
        }
        return false;
    }
    
    /**
     * 獲取數據預覽
     */
    private String getDataPreview(byte[] data) {
        int previewLength = Math.min(32, data.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < previewLength; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        if (data.length > previewLength) {
            sb.append("...");
        }
        return sb.toString();
    }
    
    /**
     * 增強程式資訊
     */
    private void enrichProgramInfo(AnchorProgramInfo info) {
        try {
            // 嘗試從公開的 IDL 倉庫或 API 獲取更多資訊
            // 這裡可以添加外部 API 調用
            
            // 設定版本資訊
            info.setVersion("Unknown");
            
            // 設定描述
            String description = generateProgramDescription(info.getName());
            info.setDescription(description);
            
        } catch (Exception e) {
            System.out.printf("⚠️ 增強程式資訊失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 生成程式描述
     */
    private String generateProgramDescription(String programName) {
        if (programName.contains("Raydium")) {
            return "Raydium DEX - Automated Market Maker and Concentrated Liquidity protocol";
        } else if (programName.contains("Orca")) {
            return "Orca DEX - User-friendly automated market maker";
        } else if (programName.contains("Jupiter")) {
            return "Jupiter - Liquidity aggregator for best swap prices";
        } else if (programName.contains("Serum")) {
            return "Serum DEX - Decentralized exchange with order book";
        } else if (programName.contains("Mango")) {
            return "Mango Markets - Decentralized trading platform";
        } else if (programName.contains("Solend")) {
            return "Solend - Algorithmic, decentralized lending protocol";
        } else {
            return "Anchor-based DeFi protocol";
        }
    }
    
    /**
     * 確定專案類型
     */
    private String determineProjectType(String programName) {
        if (programName.contains("CLMM") || programName.contains("Whirlpool") || programName.contains("DLMM")) {
            return "CLMM";
        } else if (programName.contains("AMM") || programName.contains("Pool")) {
            return "AMM";
        } else if (programName.contains("DEX") || programName.contains("Serum")) {
            return "DEX";
        } else if (programName.contains("Solend") || programName.contains("Lending")) {
            return "Lending";
        } else if (programName.contains("Jupiter")) {
            return "Aggregator";
        } else {
            return "DeFi";
        }
    }
    
    /**
     * 獲取已知指令
     */
    private List<String> getKnownInstructions(String programId) {
        List<String> instructions = new ArrayList<>();
        
        // 根據程式 ID 返回已知的指令
        switch (programId) {
            case "CAMMCzo5YL8w4VFF8KVHrK22GGUsp5VTaW7grrKgrWqK":
                instructions.addAll(Arrays.asList(
                    "initialize", "openPosition", "increaseLiquidity", "decreaseLiquidity",
                    "collectFees", "collectReward", "closePosition", "swap"
                ));
                break;
            case "675kPX9MHTjS2zt1qfr1NYHuzeLXfQM9H24wFSUt1Mp8":
                instructions.addAll(Arrays.asList(
                    "initialize", "deposit", "withdraw", "swap"
                ));
                break;
            case "whirLbMiicVdio4qvUfM5KAg6Ct8VwpYzGff3uctyCc":
                instructions.addAll(Arrays.asList(
                    "initializePool", "openPosition", "increaseLiquidity", "decreaseLiquidity", "collectFees"
                ));
                break;
            default:
                instructions.add("Unknown instructions");
                break;
        }
        
        return instructions;
    }
    
    /**
     * 獲取程式統計資訊
     */
    public Map<String, Object> getProgramStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("knownProgramsCount", KNOWN_ANCHOR_PROGRAMS.size());
        stats.put("cachedProgramsCount", programCache.size());
        stats.put("supportedTypes", Arrays.asList("CLMM", "AMM", "DEX", "Lending", "Aggregator"));
        
        // 按類型統計
        Map<String, Integer> typeCount = new HashMap<>();
        for (AnchorProgramInfo info : programCache.values()) {
            String type = info.getType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        stats.put("typeDistribution", typeCount);
        
        return stats;
    }
    
    // 內部類定義
    public static class AnchorProgramInfo {
        private String programId;
        private String name;
        private String type;
        private String version;
        private String description;
        private boolean executable;
        private List<String> instructions;
        
        public AnchorProgramInfo() {
            this.instructions = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getProgramId() { return programId; }
        public void setProgramId(String programId) { this.programId = programId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isExecutable() { return executable; }
        public void setExecutable(boolean executable) { this.executable = executable; }
        public List<String> getInstructions() { return instructions; }
        public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    }
    
    public static class AnchorProgramAnalysis {
        private String accountAddress;
        private AnchorProgramInfo programInfo;
        private Map<String, Object> parsedData;
        private int dataSize;
        private String owner;
        private long timestamp;
        
        public AnchorProgramAnalysis() {
            this.parsedData = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getAccountAddress() { return accountAddress; }
        public void setAccountAddress(String accountAddress) { this.accountAddress = accountAddress; }
        public AnchorProgramInfo getProgramInfo() { return programInfo; }
        public void setProgramInfo(AnchorProgramInfo programInfo) { this.programInfo = programInfo; }
        public Map<String, Object> getParsedData() { return parsedData; }
        public void setParsedData(Map<String, Object> parsedData) { this.parsedData = parsedData; }
        public int getDataSize() { return dataSize; }
        public void setDataSize(int dataSize) { this.dataSize = dataSize; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}