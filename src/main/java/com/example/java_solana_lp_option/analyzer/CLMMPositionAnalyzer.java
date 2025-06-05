package com.example.java_solana_lp_option.analyzer;

import com.example.java_solana_lp_option.config.SolanaConfig;
import com.example.java_solana_lp_option.service.SolanaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;

@Component
public class CLMMPositionAnalyzer {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SolanaConfig solanaConfig;
    private final SolanaService solanaService;
    
    // 常數定義
    private static final String DEFAULT_CLMM_POSITION = "BSoUetj6UWvZFYrSnA9KsejAzQZWXUTfFCsB2EWk3LYh";
    private static final String WSOL_MINT = "So11111111111111111111111111111111111111112";
    
    public CLMMPositionAnalyzer(SolanaConfig solanaConfig, SolanaService solanaService) {
        this.solanaConfig = solanaConfig;
        this.solanaService = solanaService;
        this.objectMapper = new ObjectMapper();
        
        // 設定 RestTemplate 超時
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(solanaConfig.getConnectTimeout());
        factory.setReadTimeout(solanaConfig.getReadTimeout());
        this.restTemplate = new RestTemplate(factory);
    }
    
    // 內部類別定義
    public static class TokenInfo {
        private String mint;
        private String symbol;
        private String name;
        private int decimals;
        private String address;
        
        public TokenInfo() {}
        
        public TokenInfo(String mint, String symbol, String name, int decimals, String address) {
            this.mint = mint;
            this.symbol = symbol;
            this.name = name;
            this.decimals = decimals;
            this.address = address;
        }
        
        // Getters and Setters
        public String getMint() { return mint; }
        public void setMint(String mint) { this.mint = mint; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getDecimals() { return decimals; }
        public void setDecimals(int decimals) { this.decimals = decimals; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    public static class PoolInfo {
        private String id;
        private TokenInfo mintA;
        private TokenInfo mintB;
        private double price;
        private double tvl;
        private double feeRate;
        private DayStats day;
        private List<RewardInfo> rewardDefaultInfos;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public TokenInfo getMintA() { return mintA; }
        public void setMintA(TokenInfo mintA) { this.mintA = mintA; }
        public TokenInfo getMintB() { return mintB; }
        public void setMintB(TokenInfo mintB) { this.mintB = mintB; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public double getTvl() { return tvl; }
        public void setTvl(double tvl) { this.tvl = tvl; }
        public double getFeeRate() { return feeRate; }
        public void setFeeRate(double feeRate) { this.feeRate = feeRate; }
        public DayStats getDay() { return day; }
        public void setDay(DayStats day) { this.day = day; }
        public List<RewardInfo> getRewardDefaultInfos() { return rewardDefaultInfos; }
        public void setRewardDefaultInfos(List<RewardInfo> rewardDefaultInfos) { this.rewardDefaultInfos = rewardDefaultInfos; }
    }
    
    public static class DayStats {
        private double volume;
        private double apr;
        private double feeApr;
        private List<Double> rewardApr;
        
        // Getters and Setters
        public double getVolume() { return volume; }
        public void setVolume(double volume) { this.volume = volume; }
        public double getApr() { return apr; }
        public void setApr(double apr) { this.apr = apr; }
        public double getFeeApr() { return feeApr; }
        public void setFeeApr(double feeApr) { this.feeApr = feeApr; }
        public List<Double> getRewardApr() { return rewardApr; }
        public void setRewardApr(List<Double> rewardApr) { this.rewardApr = rewardApr; }
    }
    
    public static class RewardInfo {
        private TokenInfo mint;
        
        public TokenInfo getMint() { return mint; }
        public void setMint(TokenInfo mint) { this.mint = mint; }
    }
    
    public static class PositionInfo {
        private double tvlPercentage;
        private double usdValue;
        private double amountA;
        private double amountB;
        private UnclaimedFee unclaimedFee;
        
        // Getters and Setters
        public double getTvlPercentage() { return tvlPercentage; }
        public void setTvlPercentage(double tvlPercentage) { this.tvlPercentage = tvlPercentage; }
        public double getUsdValue() { return usdValue; }
        public void setUsdValue(double usdValue) { this.usdValue = usdValue; }
        public double getAmountA() { return amountA; }
        public void setAmountA(double amountA) { this.amountA = amountA; }
        public double getAmountB() { return amountB; }
        public void setAmountB(double amountB) { this.amountB = amountB; }
        public UnclaimedFee getUnclaimedFee() { return unclaimedFee; }
        public void setUnclaimedFee(UnclaimedFee unclaimedFee) { this.unclaimedFee = unclaimedFee; }
    }
    
    public static class UnclaimedFee {
        private double amountA;
        private double amountB;
        private double usdFeeValue;
        private List<Double> reward;
        private Double usdRewardValue;
        private double usdValue;
        
        // Getters and Setters
        public double getAmountA() { return amountA; }
        public void setAmountA(double amountA) { this.amountA = amountA; }
        public double getAmountB() { return amountB; }
        public void setAmountB(double amountB) { this.amountB = amountB; }
        public double getUsdFeeValue() { return usdFeeValue; }
        public void setUsdFeeValue(double usdFeeValue) { this.usdFeeValue = usdFeeValue; }
        public List<Double> getReward() { return reward; }
        public void setReward(List<Double> reward) { this.reward = reward; }
        public Double getUsdRewardValue() { return usdRewardValue; }
        public void setUsdRewardValue(Double usdRewardValue) { this.usdRewardValue = usdRewardValue; }
        public double getUsdValue() { return usdValue; }
        public void setUsdValue(double usdValue) { this.usdValue = usdValue; }
    }
    
    public static class CLMMPositionData {
        private String name;
        private String symbol;
        private String description;
        private PoolInfo poolInfo;
        private PositionInfo positionInfo;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public PoolInfo getPoolInfo() { return poolInfo; }
        public void setPoolInfo(PoolInfo poolInfo) { this.poolInfo = poolInfo; }
        public PositionInfo getPositionInfo() { return positionInfo; }
        public void setPositionInfo(PositionInfo positionInfo) { this.positionInfo = positionInfo; }
    }
    
    public static class TokenExtensionsData {
        private String name;
        private String symbol;
        private String uri;
        private List<Map<String, String>> additionalMetadata;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        public List<Map<String, String>> getAdditionalMetadata() { return additionalMetadata; }
        public void setAdditionalMetadata(List<Map<String, String>> additionalMetadata) { this.additionalMetadata = additionalMetadata; }
    }
    
    public static class PositionRange {
        private Double lowerPrice;
        private Double upperPrice;
        private Integer tickLower;
        private Integer tickUpper;
        
        public PositionRange(Double lowerPrice, Double upperPrice, Integer tickLower, Integer tickUpper) {
            this.lowerPrice = lowerPrice;
            this.upperPrice = upperPrice;
            this.tickLower = tickLower;
            this.tickUpper = tickUpper;
        }
        
        // Getters and Setters
        public Double getLowerPrice() { return lowerPrice; }
        public void setLowerPrice(Double lowerPrice) { this.lowerPrice = lowerPrice; }
        public Double getUpperPrice() { return upperPrice; }
        public void setUpperPrice(Double upperPrice) { this.upperPrice = upperPrice; }
        public Integer getTickLower() { return tickLower; }
        public void setTickLower(Integer tickLower) { this.tickLower = tickLower; }
        public Integer getTickUpper() { return tickUpper; }
        public void setTickUpper(Integer tickUpper) { this.tickUpper = tickUpper; }
    }
    
    /**
     * 延遲方法
     */
    private void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 帶重試機制的 API 呼叫
     */
    private <T> T callWithRetry(java.util.function.Supplier<T> apiCall, String operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= solanaConfig.getMaxRetries(); attempt++) {
            try {
                return apiCall.get();
            } catch (Exception e) {
                lastException = e;
                String errorMessage = e.getMessage();
                
                // 對於 404 錯誤，不要重試
                if (e instanceof org.springframework.web.client.HttpClientErrorException.NotFound) {
                    System.out.printf("❌ %s: Position 不存在 (404 Not Found)%n", operation);
                    throw e; // 直接拋出，不重試
                }
                
                if (errorMessage != null && (errorMessage.contains("429") || errorMessage.contains("Too Many Requests"))) {
                    System.out.printf("⚠️  %s 遇到 API 限制 (嘗試 %d/%d)%n", operation, attempt, solanaConfig.getMaxRetries());
                    
                    if (attempt < solanaConfig.getMaxRetries()) {
                        long delayMs = solanaConfig.getRetryDelay() * (long) Math.pow(2, attempt - 1);
                        System.out.printf("⏳ 等待 %dms 後重試...%n", delayMs);
                        delay(delayMs);
                        continue;
                    }
                }
                
                if (attempt == solanaConfig.getMaxRetries()) {
                    System.err.printf("❌ %s 失敗 (已重試 %d 次): %s%n", operation, solanaConfig.getMaxRetries(), errorMessage);
                    throw new RuntimeException(operation + " 超過最大重試次數", e);
                }
                
                delay(1000);
            }
        }
        
        throw new RuntimeException(operation + " 超過最大重試次數", lastException);
    }
    
    /**
     * 從 tick 計算價格
     */
    private double calculatePriceFromTick(int tick, int decimalsA, int decimalsB) {
        double basePrice = Math.pow(1.0001, tick);
        double adjustmentFactor = Math.pow(10, decimalsA - decimalsB);
        return basePrice * adjustmentFactor;
    }
    
    /**
     * 獲取 Token Extensions 元數據 - 使用實際 Solana 節點
     */
    private TokenExtensionsData getTokenExtensions(String mintAddress) {
        try {
            System.out.println("🔍 檢查 Token Extensions 元數據...");
            
            if (solanaConfig.isEnableBlockchainData()) {
                // 使用實際的 Solana 節點
                SolanaService.TokenExtensionsInfo tokenInfo = solanaService.getTokenExtensions(mintAddress);
                
                if (tokenInfo != null) {
                    TokenExtensionsData extensions = new TokenExtensionsData();
                    extensions.setName(tokenInfo.getName());
                    extensions.setSymbol(tokenInfo.getSymbol());
                    extensions.setUri(tokenInfo.getUri());
                    
                    System.out.printf("✅ 從區塊鏈獲取 Token Extensions: %s%n", tokenInfo.getName());
                    return extensions;
                } else {
                    System.out.println("❌ 未從區塊鏈找到 Token Extensions");
                    return null;
                }
            } else {
                // 使用模擬數據
                System.out.println("⚠️ 使用模擬 Token Extensions 數據");
                TokenExtensionsData extensions = new TokenExtensionsData();
                extensions.setName("Raydium CLMM Position");
                extensions.setSymbol("RCP");
                extensions.setUri("https://dynamic-ipfs.raydium.io/clmm/position?id=" + mintAddress);
                return extensions;
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Token Extensions 查詢失敗: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 獲取 CLMM Position 詳細數據
     */
    private CLMMPositionData fetchCLMMPositionData(String uri) {
        return callWithRetry(() -> {
            try {
                System.out.println("🌐 獲取 CLMM Position 詳細資訊...");
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("User-Agent", "CLMMPositionAnalyzer/1.0");
                
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    CLMMPositionData data = parseCLMMPositionData(jsonNode);
                    
                    System.out.println("✅ CLMM Position 資料獲取成功");
                    return data;
                } else {
                    System.out.printf("⚠️ API 回應錯誤: %s%n", response.getStatusCode());
                    return null;
                }
                
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                System.out.println("⚠️ Position 不存在或已過期 (404 Not Found)");
                System.out.println("💡 這可能是因為:");
                System.out.println("   - Position 已被關閉或撤回");
                System.out.println("   - NFT Mint 地址不正確");
                System.out.println("   - Raydium API 暫時無法存取該 Position");
                return null; // 返回 null 而不是拋出異常
            } catch (Exception e) {
                throw new RuntimeException("獲取 CLMM Position 資料失敗: " + e.getMessage(), e);
            }
        }, "CLMM Position 資料獲取");
    }
    
    /**
     * 解析 CLMM Position JSON 資料
     */
    private CLMMPositionData parseCLMMPositionData(JsonNode jsonNode) {
        CLMMPositionData data = new CLMMPositionData();
        
        // 基本資訊
        data.setName(getStringValue(jsonNode, "name", "Unknown"));
        data.setSymbol(getStringValue(jsonNode, "symbol", "Unknown"));
        data.setDescription(getStringValue(jsonNode, "description", ""));
        
        // 解析池資訊
        JsonNode poolInfoNode = jsonNode.get("poolInfo");
        if (poolInfoNode != null) {
            PoolInfo poolInfo = new PoolInfo();
            poolInfo.setId(getStringValue(poolInfoNode, "id", ""));
            poolInfo.setPrice(getDoubleValue(poolInfoNode, "price", 0.0));
            poolInfo.setTvl(getDoubleValue(poolInfoNode, "tvl", 0.0));
            poolInfo.setFeeRate(getDoubleValue(poolInfoNode, "feeRate", 0.0));
            
            // 解析代幣 A
            JsonNode mintANode = poolInfoNode.get("mintA");
            if (mintANode != null) {
                TokenInfo mintA = new TokenInfo();
                mintA.setAddress(getStringValue(mintANode, "address", ""));
                mintA.setSymbol(getStringValue(mintANode, "symbol", ""));
                mintA.setName(getStringValue(mintANode, "name", ""));
                mintA.setDecimals(getIntValue(mintANode, "decimals", 0));
                poolInfo.setMintA(mintA);
            }
            
            // 解析代幣 B
            JsonNode mintBNode = poolInfoNode.get("mintB");
            if (mintBNode != null) {
                TokenInfo mintB = new TokenInfo();
                mintB.setAddress(getStringValue(mintBNode, "address", ""));
                mintB.setSymbol(getStringValue(mintBNode, "symbol", ""));
                mintB.setName(getStringValue(mintBNode, "name", ""));
                mintB.setDecimals(getIntValue(mintBNode, "decimals", 0));
                poolInfo.setMintB(mintB);
            }
            
            // 解析每日統計
            JsonNode dayNode = poolInfoNode.get("day");
            if (dayNode != null) {
                DayStats dayStats = new DayStats();
                dayStats.setVolume(getDoubleValue(dayNode, "volume", 0.0));
                dayStats.setApr(getDoubleValue(dayNode, "apr", 0.0));
                dayStats.setFeeApr(getDoubleValue(dayNode, "feeApr", 0.0));
                
                // 解析獎勵 APR
                JsonNode rewardAprNode = dayNode.get("rewardApr");
                if (rewardAprNode != null && rewardAprNode.isArray()) {
                    List<Double> rewardAprList = new ArrayList<>();
                    for (JsonNode aprNode : rewardAprNode) {
                        rewardAprList.add(aprNode.asDouble());
                    }
                    dayStats.setRewardApr(rewardAprList);
                }
                
                poolInfo.setDay(dayStats);
            }
            
            data.setPoolInfo(poolInfo);
        }
        
        // 解析位置資訊
        JsonNode positionInfoNode = jsonNode.get("positionInfo");
        if (positionInfoNode != null) {
            PositionInfo positionInfo = new PositionInfo();
            positionInfo.setTvlPercentage(getDoubleValue(positionInfoNode, "tvlPercentage", 0.0));
            positionInfo.setUsdValue(getDoubleValue(positionInfoNode, "usdValue", 0.0));
            positionInfo.setAmountA(getDoubleValue(positionInfoNode, "amountA", 0.0));
            positionInfo.setAmountB(getDoubleValue(positionInfoNode, "amountB", 0.0));
            
            // 解析未領取手續費
            JsonNode unclaimedFeeNode = positionInfoNode.get("unclaimedFee");
            if (unclaimedFeeNode != null) {
                UnclaimedFee unclaimedFee = new UnclaimedFee();
                unclaimedFee.setAmountA(getDoubleValue(unclaimedFeeNode, "amountA", 0.0));
                unclaimedFee.setAmountB(getDoubleValue(unclaimedFeeNode, "amountB", 0.0));
                unclaimedFee.setUsdFeeValue(getDoubleValue(unclaimedFeeNode, "usdFeeValue", 0.0));
                unclaimedFee.setUsdValue(getDoubleValue(unclaimedFeeNode, "usdValue", 0.0));
                
                Double usdRewardValue = getDoubleValue(unclaimedFeeNode, "usdRewardValue", null);
                unclaimedFee.setUsdRewardValue(usdRewardValue);
                
                // 解析獎勵
                JsonNode rewardNode = unclaimedFeeNode.get("reward");
                if (rewardNode != null && rewardNode.isArray()) {
                    List<Double> rewardList = new ArrayList<>();
                    for (JsonNode reward : rewardNode) {
                        rewardList.add(reward.asDouble());
                    }
                    unclaimedFee.setReward(rewardList);
                }
                
                positionInfo.setUnclaimedFee(unclaimedFee);
            }
            
            data.setPositionInfo(positionInfo);
        }
        
        return data;
    }
    
    /**
     * 從區塊鏈讀取 CLMM Position 帳戶數據 - 使用實際 Solana 節點
     */
    private PositionRange getPositionAccountFromChain(String positionNftMint) {
        try {
            System.out.println("🔍 從區塊鏈讀取 Position 帳戶數據...");
            
            if (solanaConfig.isEnableBlockchainData()) {
                // 檢查 Solana 節點連接狀態
                if (!solanaService.checkConnectionHealth()) {
                    System.out.println("❌ Solana 節點連接失敗，使用模擬數據");
                    return createMockPositionRange();
                }
                
                System.out.printf("🔗 使用 Solana 節點: %s (%s)%n", 
                    solanaConfig.getRpcUrl(), 
                    solanaConfig.getNetworkDisplayName());
                System.out.printf("📡 節點版本: %s%n", solanaService.getNodeVersion());
                
                // 使用實際的區塊鏈服務查找 Position 帳戶
                SolanaService.PositionAccountInfo positionInfo = solanaService.findCLMMPositionAccount(positionNftMint);
                
                if (positionInfo != null) {
                    int decimalsA = 9; // WSOL 預設
                    int decimalsB = 6; // USDC 預設
                    
                    double lowerPrice = calculatePriceFromTick(positionInfo.getTickLower(), decimalsA, decimalsB);
                    double upperPrice = calculatePriceFromTick(positionInfo.getTickUpper(), decimalsA, decimalsB);
                    
                    System.out.printf("✅ 從區塊鏈成功獲取 tick 數據: %d 到 %d%n", 
                        positionInfo.getTickLower(), positionInfo.getTickUpper());
                    
                    return new PositionRange(lowerPrice, upperPrice, 
                        positionInfo.getTickLower(), positionInfo.getTickUpper());
                } else {
                    System.out.println("❌ 未從區塊鏈找到 Position 帳戶，使用模擬數據");
                    return createMockPositionRange();
                }
            } else {
                System.out.println("⚠️ 區塊鏈數據讀取已停用，使用模擬數據");
                return createMockPositionRange();
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 從區塊鏈讀取 Position 數據失敗: %s%n", e.getMessage());
            System.out.println("🔄 回退到模擬數據");
            return createMockPositionRange();
        }
    }
    
    /**
     * 創建模擬的 Position 範圍數據
     */
    private PositionRange createMockPositionRange() {
        // 模擬找到的 tick 數據
        int tickLower = -18973;  // 對應約 149.99 USDC per WSOL
        int tickUpper = -12041;  // 對應約 299.98 USDC per WSOL
        
        int decimalsA = 9; // WSOL
        int decimalsB = 6; // USDC
        
        double lowerPrice = calculatePriceFromTick(tickLower, decimalsA, decimalsB);
        double upperPrice = calculatePriceFromTick(tickUpper, decimalsA, decimalsB);
        
        System.out.printf("📊 模擬 tick 範圍: %d 到 %d%n", tickLower, tickUpper);
        System.out.println("💡 在實際部署時將使用真實的區塊鏈數據");
        
        return new PositionRange(lowerPrice, upperPrice, tickLower, tickUpper);
    }
    
    /**
     * 在分析開始時顯示連接狀態
     */
    private void displayConnectionStatus() {
        System.out.println("\n🔗 Solana 連接狀態:");
        System.out.println("-".repeat(50));
        System.out.printf("RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("網路: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "啟用" : "模擬");
        
        if (solanaConfig.isEnableBlockchainData()) {
            boolean isHealthy = solanaService.checkConnectionHealth();
            System.out.printf("節點狀態: %s%n", isHealthy ? "✅ 正常" : "❌ 異常");
            
            if (isHealthy) {
                String version = solanaService.getNodeVersion();
                System.out.printf("節點版本: %s%n", version);
            }
        } else {
            System.out.println("節點狀態: ⚠️ 已停用，使用模擬數據");
        }
        
        System.out.println("-".repeat(50));
    }
    
    /**
     * 分析並顯示 CLMM Position
     */
    public void analyzeCLMMPosition(String mintAddress) {
        System.out.println("🚀 CLMM Position 分析器");
        System.out.println("=".repeat(80));
        System.out.printf("🎯 目標代幣: %s%n", mintAddress);
        
        // 顯示連接狀態
        displayConnectionStatus();
        
        try {
            // 1. 獲取 Token Extensions 元數據
            TokenExtensionsData tokenExtensions = getTokenExtensions(mintAddress);
            
            if (tokenExtensions == null) {
                System.out.println("❌ 未找到 Token Extensions 元數據");
                System.out.println("💡 這個地址可能不是有效的 CLMM Position NFT");
                return;
            }
            
            System.out.println("✅ Token Extensions 元數據獲取成功");
            System.out.printf("   名稱: %s%n", tokenExtensions.getName());
            System.out.printf("   符號: %s%n", tokenExtensions.getSymbol());
            
            // 2. 獲取 CLMM Position 詳細數據
            if (tokenExtensions.getUri() == null || tokenExtensions.getUri().isEmpty()) {
                System.out.println("❌ 未找到元數據 URI");
                return;
            }
            
            CLMMPositionData clmmData = fetchCLMMPositionData(tokenExtensions.getUri());
            
            if (clmmData == null) {
                System.out.println("❌ 無法獲取 CLMM Position 數據");
                System.out.println("📋 Position 摘要:");
                System.out.printf("   地址: %s%n", mintAddress);
                System.out.printf("   狀態: 無法存取或已關閉%n");
                System.out.printf("   建議: 檢查 Position 是否仍然活躍%n");
                return;
            }
            
            // 3. 顯示詳細分析
            displayCLMMAnalysis(clmmData, mintAddress);
            
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            System.out.println("❌ Position 不存在或已過期");
            System.out.println("📋 Position 摘要:");
            System.out.printf("   地址: %s%n", mintAddress);
            System.out.printf("   狀態: 404 Not Found%n");
            System.out.printf("   原因: Position 可能已被關閉、撤回或地址不正確%n");
        } catch (Exception e) {
            System.err.printf("❌ 分析過程發生錯誤: %s%n", e.getMessage());
            System.out.println("📋 Position 摘要:");
            System.out.printf("   地址: %s%n", mintAddress);
            System.out.printf("   狀態: 分析失敗%n");
            System.out.printf("   錯誤: %s%n", e.getMessage());
            
            // 如果是 Solana 節點相關錯誤，提供額外建議
            if (e.getMessage().contains("Solana") || e.getMessage().contains("RPC")) {
                System.out.println("\n💡 Solana 節點連接問題排除建議:");
                System.out.println("   1. 檢查網路連接");
                System.out.println("   2. 確認 RPC URL 正確");
                System.out.println("   3. 嘗試使用其他 RPC 提供商");
                System.out.println("   4. 暫時停用區塊鏈數據讀取 (設定 solana.enableBlockchainData=false)");
            }
        }
    }
    
    /**
     * 顯示詳細的 CLMM Position 分析
     */
    private void displayCLMMAnalysis(CLMMPositionData data, String mintAddress) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 CLMM Position 詳細分析報告");
        System.out.println("=".repeat(80));
        
        // 基本資訊
        System.out.printf("🎯 Position NFT: %s%n", data.getName());
        System.out.printf("🔍 符號: %s%n", data.getSymbol());
        System.out.printf("📝 描述: %s%n", data.getDescription());
        
        // 池資訊
        if (data.getPoolInfo() != null) {
            PoolInfo pool = data.getPoolInfo();
            System.out.println("\n🏊 池資訊:");
            System.out.println("-".repeat(50));
            System.out.printf("池 ID: %s%n", pool.getId());
            
            if (pool.getMintA() != null && pool.getMintB() != null) {
                System.out.printf("代幣對: %s / %s%n", pool.getMintA().getSymbol(), pool.getMintB().getSymbol());
                System.out.printf("代幣 A: %s (%s)%n", pool.getMintA().getName(), pool.getMintA().getAddress());
                System.out.printf("代幣 B: %s (%s)%n", pool.getMintB().getName(), pool.getMintB().getAddress());
                System.out.printf("當前價格: %.3f %s per %s%n", 
                    pool.getPrice(), pool.getMintB().getSymbol(), pool.getMintA().getSymbol());
            }
            
            System.out.printf("池 TVL: %s%n", formatCurrency(pool.getTvl()));
            System.out.printf("手續費率: %.3f%%%n", pool.getFeeRate() * 100);
            
            if (pool.getDay() != null) {
                DayStats day = pool.getDay();
                System.out.printf("24H 交易量: %s%n", formatCurrency(day.getVolume()));
                System.out.printf("24H APR: %.2f%%%n", day.getApr());
                System.out.printf("  手續費 APR: %.2f%%%n", day.getFeeApr());
                
                if (day.getRewardApr() != null && !day.getRewardApr().isEmpty()) {
                    System.out.printf("  獎勵 APR: %.2f%%%n", day.getRewardApr().get(0));
                }
            }
        }
        
        // Position 詳細資訊
        if (data.getPositionInfo() != null) {
            PositionInfo pos = data.getPositionInfo();
            System.out.println("\n💧 流動性位置詳情:");
            System.out.println("-".repeat(50));
            System.out.printf("位置價值: %s%n", formatCurrency(pos.getUsdValue()));
            System.out.printf("TVL 佔比: %.4f%%%n", pos.getTvlPercentage());
            
            // 分析代幣數量
            analyzeTokenAmounts(data, pos);
            
            // 未領取收益分析
            if (pos.getUnclaimedFee() != null) {
                analyzeUnclaimedFees(data, pos.getUnclaimedFee());
            }
        }
        
        // 從鏈上讀取 Position 價格範圍
        System.out.println("\n🔍 從區塊鏈讀取 Position 價格範圍...");
        
        PositionRange chainPositionData = getPositionAccountFromChain(mintAddress);
        
        if (chainPositionData.getTickLower() != null && chainPositionData.getTickUpper() != null) {
            analyzePriceRange(data, chainPositionData);
        } else {
            System.out.println("\n❌ 無法從區塊鏈獲取 Position 價格範圍資訊");
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 分析完成！");
        
        // 顯示使用說明
        displayUsageInstructions();
    }
    
    /**
     * 分析代幣數量
     */
    private void analyzeTokenAmounts(CLMMPositionData data, PositionInfo pos) {
        if (data.getPoolInfo() == null) return;
        
        PoolInfo pool = data.getPoolInfo();
        double wsolAmount = 0;
        double usdcAmount = 0;
        
        if (pool.getMintA() != null && pool.getMintB() != null) {
            if (WSOL_MINT.equals(pool.getMintA().getAddress())) {
                // Token A 是 WSOL
                wsolAmount = pos.getAmountA();
                usdcAmount = pos.getAmountB();
            } else if (WSOL_MINT.equals(pool.getMintB().getAddress())) {
                // Token B 是 WSOL
                wsolAmount = pos.getAmountB();
                usdcAmount = pos.getAmountA();
            }
            
            System.out.printf("代幣數量分析:%n");
            System.out.printf("  %s 數量: %.6f%n", pool.getMintA().getSymbol(), pos.getAmountA());
            System.out.printf("  %s 數量: %.6f%n", pool.getMintB().getSymbol(), pos.getAmountB());
            
            if (wsolAmount > 0 || usdcAmount > 0) {
                System.out.printf("  WSOL: %.6f SOL%n", wsolAmount);
                System.out.printf("  USDC: %.2f USDC%n", usdcAmount);
            }
        }
    }
    
    /**
     * 分析未領取手續費
     */
    private void analyzeUnclaimedFees(CLMMPositionData data, UnclaimedFee fee) {
        System.out.println("\n💸 未領取收益:");
        System.out.println("-".repeat(30));
        System.out.printf("總美元價值: %s%n", formatCurrency(fee.getUsdFeeValue()));
        
        if (data.getPoolInfo() != null) {
            PoolInfo pool = data.getPoolInfo();
            double wsolFee = 0;
            double usdcFee = 0;
            
            if (pool.getMintA() != null && pool.getMintB() != null) {
                if (WSOL_MINT.equals(pool.getMintA().getAddress())) {
                    wsolFee = fee.getAmountA();
                    usdcFee = fee.getAmountB();
                } else if (WSOL_MINT.equals(pool.getMintB().getAddress())) {
                    wsolFee = fee.getAmountB();
                    usdcFee = fee.getAmountA();
                }
                
                if (wsolFee > 0 || usdcFee > 0) {
                    System.out.printf("WSOL 手續費: %.6f SOL%n", wsolFee);
                    System.out.printf("USDC 手續費: %.2f USDC%n", usdcFee);
                }
            }
        }
        
        // 獎勵代幣
        if (fee.getReward() != null && !fee.getReward().isEmpty() && 
            data.getPoolInfo() != null && data.getPoolInfo().getRewardDefaultInfos() != null) {
            
            System.out.println("\n🏆 獎勵代幣:");
            List<Double> rewards = fee.getReward();
            List<RewardInfo> rewardInfos = data.getPoolInfo().getRewardDefaultInfos();
            
            for (int i = 0; i < rewards.size() && i < rewardInfos.size(); i++) {
                RewardInfo rewardInfo = rewardInfos.get(i);
                if (rewardInfo.getMint() != null) {
                    System.out.printf("%s 獎勵: %.6f%n", 
                        rewardInfo.getMint().getSymbol(), rewards.get(i));
                }
            }
            
            if (fee.getUsdRewardValue() != null) {
                System.out.printf("獎勵總價值: %s%n", formatCurrency(fee.getUsdRewardValue()));
            }
        }
        
        System.out.printf("未領取總價值: %s%n", formatCurrency(fee.getUsdValue()));
    }
    
    /**
     * 分析價格範圍
     */
    private void analyzePriceRange(CLMMPositionData data, PositionRange chainPositionData) {
        // 獲取代幣小數位數
        int decimalsA = 9; // WSOL 預設
        int decimalsB = 6; // USDC 預設
        
        if (data.getPoolInfo() != null) {
            PoolInfo pool = data.getPoolInfo();
            if (pool.getMintA() != null) {
                decimalsA = pool.getMintA().getDecimals();
            }
            if (pool.getMintB() != null) {
                decimalsB = pool.getMintB().getDecimals();
            }
            System.out.printf("🔍 代幣小數位數: %s=%d, %s=%d%n", 
                pool.getMintA() != null ? pool.getMintA().getSymbol() : "TokenA", decimalsA,
                pool.getMintB() != null ? pool.getMintB().getSymbol() : "TokenB", decimalsB);
        }
        
        double lowerPrice = calculatePriceFromTick(chainPositionData.getTickLower(), decimalsA, decimalsB);
        double upperPrice = calculatePriceFromTick(chainPositionData.getTickUpper(), decimalsA, decimalsB);
        
        System.out.println("\n📏 Position 價格範圍:");
        System.out.println("-".repeat(30));
        
        if (data.getPoolInfo() != null && data.getPoolInfo().getMintA() != null && data.getPoolInfo().getMintB() != null) {
            String symbolA = data.getPoolInfo().getMintA().getSymbol();
            String symbolB = data.getPoolInfo().getMintB().getSymbol();
            System.out.printf("Lower Price: %.6f %s per %s%n", lowerPrice, symbolB, symbolA);
            System.out.printf("Upper Price: %.6f %s per %s%n", upperPrice, symbolB, symbolA);
        } else {
            System.out.printf("Lower Price: %.6f%n", lowerPrice);
            System.out.printf("Upper Price: %.6f%n", upperPrice);
        }
        
        System.out.printf("Tick 範圍: %d 到 %d%n", chainPositionData.getTickLower(), chainPositionData.getTickUpper());
        
        // 計算價格範圍寬度
        if (lowerPrice > 0) {
            double rangeWidth = ((upperPrice - lowerPrice) / lowerPrice * 100);
            System.out.printf("價格範圍寬度: %.2f%%%n", rangeWidth);
        }
        
        // 判斷當前價格位置
        if (data.getPoolInfo() != null) {
            double currentPrice = data.getPoolInfo().getPrice();
            
            if (data.getPoolInfo().getMintA() != null && data.getPoolInfo().getMintB() != null) {
                System.out.printf("池當前價格: %.6f %s per %s%n", 
                    currentPrice, 
                    data.getPoolInfo().getMintB().getSymbol(), 
                    data.getPoolInfo().getMintA().getSymbol());
            } else {
                System.out.printf("池當前價格: %.6f%n", currentPrice);
            }
            
            analyzePricePosition(data, currentPrice, lowerPrice, upperPrice);
        }
    }
    
    /**
     * 分析價格位置
     */
    private void analyzePricePosition(CLMMPositionData data, double currentPrice, double lowerPrice, double upperPrice) {
        if (currentPrice < lowerPrice) {
            if (data.getPoolInfo() != null && data.getPoolInfo().getMintB() != null) {
                System.out.printf("💡 狀態: 當前價格低於範圍下限 (全部為 %s)%n", 
                    data.getPoolInfo().getMintB().getSymbol());
            } else {
                System.out.println("💡 狀態: 當前價格低於範圍下限");
            }
        } else if (currentPrice > upperPrice) {
            if (data.getPoolInfo() != null && data.getPoolInfo().getMintA() != null) {
                System.out.printf("💡 狀態: 當前價格高於範圍上限 (全部為 %s)%n", 
                    data.getPoolInfo().getMintA().getSymbol());
            } else {
                System.out.println("💡 狀態: 當前價格高於範圍上限");
            }
        } else {
            System.out.println("✅ 狀態: 當前價格在有效範圍內 (活躍流動性)");
            
            // 計算在範圍內的位置百分比
            double positionInRange = ((currentPrice - lowerPrice) / (upperPrice - lowerPrice) * 100);
            System.out.printf("📊 價格在範圍內位置: %.1f%%%n", positionInRange);
        }
    }
    
    /**
     * 顯示使用說明
     */
    private void displayUsageInstructions() {
        System.out.println("\n💡 使用說明:");
        System.out.println("• 這是一個 Raydium 集中流動性位置 NFT");
        System.out.println("• 代表您在特定價格範圍內提供的流動性");
        System.out.println("• 可通過 Raydium 介面管理流動性和領取收益");
        System.out.println("• 當前價格在您的範圍內時，位置會賺取手續費");
        
        if (solanaConfig.isEnableBlockchainData()) {
            System.out.println("\n🔗 區塊鏈數據:");
            System.out.println("• 正在使用實際的 Solana 節點數據");
            System.out.printf("• RPC 端點: %s%n", solanaConfig.getRpcUrl());
            System.out.printf("• 網路環境: %s%n", solanaConfig.getNetworkDisplayName());
        } else {
            System.out.println("\n⚠️ 模擬數據模式:");
            System.out.println("• 目前使用模擬的區塊鏈數據");
            System.out.println("• 如需實際數據，請設定 solana.enableBlockchainData=true");
            System.out.println("• 並配置有效的 Solana RPC URL");
        }
    }
    
    /**
     * 批次分析多個 Position
     */
    public void analyzeBatchCLMMPositions(List<String> mintAddresses) {
        System.out.println("🚀 批次 CLMM Position 分析器");
        System.out.println("=".repeat(80));
        System.out.printf("📊 將分析 %d 個 Position%n", mintAddresses.size());
        
        int successCount = 0;
        int failedCount = 0;
        List<String> successList = new ArrayList<>();
        List<String> failedList = new ArrayList<>();
        
        for (int i = 0; i < mintAddresses.size(); i++) {
            String mintAddress = mintAddresses.get(i);
            System.out.printf("\n🔄 [%d/%d] 分析 Position: %s%n", i + 1, mintAddresses.size(), mintAddress);
            
            try {
                // 使用修改過的分析方法，不會拋出異常
                boolean success = analyzeCLMMPositionSafely(mintAddress);
                
                if (success) {
                    successCount++;
                    successList.add(mintAddress);
                } else {
                    failedCount++;
                    failedList.add(mintAddress);
                }
                
                // 批次處理間隔
                if (i < mintAddresses.size() - 1) {
                    System.out.println("\n⏱️ 等待 2 秒後處理下一個...");
                    delay(2000);
                }
                
            } catch (Exception e) {
                failedCount++;
                failedList.add(mintAddress);
                System.err.printf("❌ Position %s 分析失敗: %s%n", mintAddress, e.getMessage());
            }
        }
        
        // 顯示批次分析結果
        System.out.println("\n" + "=".repeat(80));
        System.out.printf("✅ 批次分析完成！共處理 %d 個 Position%n", mintAddresses.size());
        System.out.printf("   成功: %d 個%n", successCount);
        System.out.printf("   失敗: %d 個%n", failedCount);
        
        if (!successList.isEmpty()) {
            System.out.println("\n✅ 成功分析的 Position:");
            for (String address : successList) {
                String shortAddress = address.length() > 12 ? 
                    address.substring(0, 6) + "..." + address.substring(address.length() - 6) : address;
                System.out.printf("   - %s%n", shortAddress);
            }
        }
        
        if (!failedList.isEmpty()) {
            System.out.println("\n❌ 分析失敗的 Position:");
            for (String address : failedList) {
                String shortAddress = address.length() > 12 ? 
                    address.substring(0, 6) + "..." + address.substring(address.length() - 6) : address;
                System.out.printf("   - %s%n", shortAddress);
            }
        }
    }
    
    /**
     * 安全的 Position 分析，不會拋出異常
     */
    private boolean analyzeCLMMPositionSafely(String mintAddress) {
        try {
            System.out.println("🚀 CLMM Position 分析器");
            System.out.println("=".repeat(80));
            System.out.printf("🎯 目標代幣: %s%n", mintAddress);
            
            TokenExtensionsData tokenExtensions = getTokenExtensions(mintAddress);
            
            if (tokenExtensions == null) {
                System.out.println("❌ 未找到 Token Extensions 元數據");
                return false;
            }
            
            System.out.println("✅ Token Extensions 元數據獲取成功");
            System.out.printf("   名稱: %s%n", tokenExtensions.getName());
            System.out.printf("   符號: %s%n", tokenExtensions.getSymbol());
            
            if (tokenExtensions.getUri() == null || tokenExtensions.getUri().isEmpty()) {
                System.out.println("❌ 未找到元數據 URI");
                return false;
            }
            
            CLMMPositionData clmmData = fetchCLMMPositionData(tokenExtensions.getUri());
            
            if (clmmData == null) {
                System.out.println("❌ 無法獲取 CLMM Position 數據 (Position 可能已關閉)");
                return false;
            }
            
            displayCLMMAnalysis(clmmData, mintAddress);
            return true;
            
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            System.out.println("❌ Position 不存在或已過期 (404 Not Found)");
            return false;
        } catch (Exception e) {
            System.err.printf("❌ 分析過程發生錯誤: %s%n", e.getMessage());
            return false;
        }
    }
    
    /**
     * 快速狀態檢查 - 僅檢查基本資訊
     */
    public void quickStatusCheck(String mintAddress) {
        System.out.printf("⚡ 快速檢查 Position: %s%n", mintAddress);
        
        try {
            TokenExtensionsData tokenExtensions = getTokenExtensions(mintAddress);
            
            if (tokenExtensions != null && tokenExtensions.getUri() != null) {
                CLMMPositionData clmmData = fetchCLMMPositionData(tokenExtensions.getUri());
                
                if (clmmData != null && clmmData.getPositionInfo() != null) {
                    PositionInfo pos = clmmData.getPositionInfo();
                    System.out.printf("   💰 位置價值: %s%n", formatCurrency(pos.getUsdValue()));
                    System.out.printf("   📊 TVL 佔比: %.4f%%%n", pos.getTvlPercentage());
                    
                    if (pos.getUnclaimedFee() != null) {
                        System.out.printf("   💸 未領收益: %s%n", formatCurrency(pos.getUnclaimedFee().getUsdValue()));
                    }
                    
                    System.out.println("   ✅ 狀態正常");
                } else {
                    System.out.println("   ❌ 無法獲取位置資訊");
                }
            } else {
                System.out.println("   ❌ 無法獲取元數據");
            }
            
        } catch (Exception e) {
            System.out.printf("   ❌ 檢查失敗: %s%n", e.getMessage());
        }
    }
    
    /**
     * 獲取 Position 摘要資訊
     */
    public Map<String, Object> getPositionSummary(String mintAddress) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("mintAddress", mintAddress);
        summary.put("status", "unknown");
        
        try {
            TokenExtensionsData tokenExtensions = getTokenExtensions(mintAddress);
            
            if (tokenExtensions != null && tokenExtensions.getUri() != null) {
                CLMMPositionData clmmData = fetchCLMMPositionData(tokenExtensions.getUri());
                
                if (clmmData != null) {
                    summary.put("status", "success");
                    summary.put("name", clmmData.getName());
                    summary.put("symbol", clmmData.getSymbol());
                    
                    if (clmmData.getPoolInfo() != null) {
                        PoolInfo pool = clmmData.getPoolInfo();
                        summary.put("poolId", pool.getId());
                        summary.put("tvl", pool.getTvl());
                        summary.put("feeRate", pool.getFeeRate());
                        
                        if (pool.getMintA() != null && pool.getMintB() != null) {
                            summary.put("tokenPair", pool.getMintA().getSymbol() + "/" + pool.getMintB().getSymbol());
                        }
                    }
                    
                    if (clmmData.getPositionInfo() != null) {
                        PositionInfo pos = clmmData.getPositionInfo();
                        summary.put("usdValue", pos.getUsdValue());
                        summary.put("tvlPercentage", pos.getTvlPercentage());
                        
                        if (pos.getUnclaimedFee() != null) {
                            summary.put("unclaimedFeesUSD", pos.getUnclaimedFee().getUsdValue());
                        }
                    }
                } else {
                    summary.put("status", "failed");
                    summary.put("error", "無法獲取 CLMM 數據");
                }
            } else {
                summary.put("status", "failed");
                summary.put("error", "無法獲取 Token Extensions");
            }
            
        } catch (Exception e) {
            summary.put("status", "error");
            summary.put("error", e.getMessage());
        }
        
        return summary;
    }
    
    /**
     * 工具方法：安全取得字串值
     */
    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull() && field.isTextual()) ? field.asText() : defaultValue;
    }
    
    /**
     * 工具方法：安全取得雙精度值
     */
    private double getDoubleValue(JsonNode node, String fieldName, double defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull() && field.isNumber()) {
            return field.asDouble();
        }
        return defaultValue;
    }
    
    /**
     * 工具方法：安全取得雙精度值（可為 null）
     */
    private Double getDoubleValue(JsonNode node, String fieldName, Double defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull() && field.isNumber()) {
            return field.asDouble();
        }
        return defaultValue;
    }
    
    /**
     * 工具方法：安全取得整數值
     */
    private int getIntValue(JsonNode node, String fieldName, int defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull() && field.isNumber()) {
            return field.asInt();
        }
        return defaultValue;
    }
    
    /**
     * 工具方法：格式化貨幣
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
     * 主要的公開方法 - 使用預設 Position
     */
    public void analyzeDefaultCLMMPosition() {
        analyzeCLMMPosition(DEFAULT_CLMM_POSITION);
    }
}