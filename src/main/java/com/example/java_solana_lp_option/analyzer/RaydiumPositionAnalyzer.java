package com.example.java_solana_lp_option.analyzer; // Correct package declaration

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component
public class RaydiumPositionAnalyzer {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // 常數定義
    private static final String RAYDIUM_AMM_POOL_ID = "8sLbNZoA1cfnvMJLPfp98ZLAnFSYCFApfJKMbiXNLwxj";
    private static final String AMM_LP_TOKEN_MINT = "BSoUetj6UWvZFYrSnA9KsejAzQZWXUTfFCsB2EWk3LYh";
    private static final String AMM_WSOL_VAULT = "6P4tvbzRY6Bh3MiWDHuLqyHywovsRwRpfskPvyeSoHsz";
    private static final String AMM_USDC_VAULT = "6mK4Pxs6GhwnessH7CvPivqDYauiHZmAdbEFDpXFk9zt";
    
    private static final String WSOL_MINT = "So11111111111111111111111111111111111111112";
    private static final String USDC_MINT = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v";
    private static final String RAY_MINT = "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R";
    
    private static final String CLMM_POSITION_API_URL_BASE = "https://dynamic-ipfs.raydium.io/clmm/position?id=";
    private static final String RAYDIUM_V2_MAIN_PAIRS_API = "https://api.raydium.io/v2/main/pairs";

    private static final int API_TIMEOUT_MS = 15000; // 15 秒超時
    
    // 已知代幣資訊
    private static final Map<String, TokenInfo> KNOWN_TOKENS_INFO = new HashMap<>();
    
    static {
        KNOWN_TOKENS_INFO.put(WSOL_MINT, new TokenInfo("WSOL", 9, "Wrapped SOL"));
        KNOWN_TOKENS_INFO.put(USDC_MINT, new TokenInfo("USDC", 6, "USD Coin"));
        KNOWN_TOKENS_INFO.put(RAY_MINT, new TokenInfo("RAY", 6, "Raydium"));
        KNOWN_TOKENS_INFO.put(AMM_LP_TOKEN_MINT, new TokenInfo("RAY-USDC-LP", 6, "Raydium LP Token (WSOL-USDC AMM V4)"));
    }
    
    public RaydiumPositionAnalyzer() {
        this.objectMapper = new ObjectMapper();
        // 設定 RestTemplate 的超時時間
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(API_TIMEOUT_MS);
        factory.setReadTimeout(API_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }
    
    // 靜態內部類別定義
    public static class TokenInfo {
        private final String symbol;
        private final int decimals;
        private final String name;
        
        public TokenInfo(String symbol, int decimals, String name) {
            this.symbol = symbol;
            this.decimals = decimals;
            this.name = name;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public int getDecimals() { return decimals; }
        public String getName() { return name; }
    }
    
    public static class PoolInfo {
        private String poolId;
        private String poolType;
        private TokenData baseToken;
        private TokenData quoteToken;
        private LpTokenData lpToken;
        private PoolStats poolStats;
        
        // Constructors, getters and setters
        public PoolInfo() {}
        
        public static class TokenData {
            private String mint;
            private String symbol;
            private int decimals;
            private String vault;
            private double reserve;
            
            // Constructors, getters and setters
            public TokenData() {}
            public TokenData(String mint, String symbol, int decimals, String vault, double reserve) {
                this.mint = mint;
                this.symbol = symbol;
                this.decimals = decimals;
                this.vault = vault;
                this.reserve = reserve;
            }
            
            // Getters and Setters
            public String getMint() { return mint; }
            public void setMint(String mint) { this.mint = mint; }
            public String getSymbol() { return symbol; }
            public void setSymbol(String symbol) { this.symbol = symbol; }
            public int getDecimals() { return decimals; }
            public void setDecimals(int decimals) { this.decimals = decimals; }
            public String getVault() { return vault; }
            public void setVault(String vault) { this.vault = vault; }
            public double getReserve() { return reserve; }
            public void setReserve(double reserve) { this.reserve = reserve; }
        }
        
        public static class LpTokenData {
            private String mint;
            private double totalSupply;
            private int decimals;
            
            // Constructors, getters and setters
            public LpTokenData() {}
            public LpTokenData(String mint, double totalSupply, int decimals) {
                this.mint = mint;
                this.totalSupply = totalSupply;
                this.decimals = decimals;
            }
            
            // Getters and Setters
            public String getMint() { return mint; }
            public void setMint(String mint) { this.mint = mint; }
            public double getTotalSupply() { return totalSupply; }
            public void setTotalSupply(double totalSupply) { this.totalSupply = totalSupply; }
            public int getDecimals() { return decimals; }
            public void setDecimals(int decimals) { this.decimals = decimals; }
        }
        
        public static class PoolStats {
            private double tvl;
            private Double volume24h;
            private Double fee24h;
            private Double apr;
            private double price;
            
            // Constructors, getters and setters
            public PoolStats() {}
            public PoolStats(double tvl, Double volume24h, Double fee24h, Double apr, double price) {
                this.tvl = tvl;
                this.volume24h = volume24h;
                this.fee24h = fee24h;
                this.apr = apr;
                this.price = price;
            }
            
            // Getters and Setters
            public double getTvl() { return tvl; }
            public void setTvl(double tvl) { this.tvl = tvl; }
            public Double getVolume24h() { return volume24h; }
            public void setVolume24h(Double volume24h) { this.volume24h = volume24h; }
            public Double getFee24h() { return fee24h; }
            public void setFee24h(Double fee24h) { this.fee24h = fee24h; }
            public Double getApr() { return apr; }
            public void setApr(Double apr) { this.apr = apr; }
            public double getPrice() { return price; }
            public void setPrice(double price) { this.price = price; }
        }
        
        // Getters and Setters for PoolInfo
        public String getPoolId() { return poolId; }
        public void setPoolId(String poolId) { this.poolId = poolId; }
        public String getPoolType() { return poolType; }
        public void setPoolType(String poolType) { this.poolType = poolType; }
        public TokenData getBaseToken() { return baseToken; }
        public void setBaseToken(TokenData baseToken) { this.baseToken = baseToken; }
        public TokenData getQuoteToken() { return quoteToken; }
        public void setQuoteToken(TokenData quoteToken) { this.quoteToken = quoteToken; }
        public LpTokenData getLpToken() { return lpToken; }
        public void setLpToken(LpTokenData lpToken) { this.lpToken = lpToken; }
        public PoolStats getPoolStats() { return poolStats; }
        public void setPoolStats(PoolStats poolStats) { this.poolStats = poolStats; }
    }
    
    public static class ParsedCLMMTokenInfo {
        private String mint;
        private String symbol;
        private String name;
        private int decimals;
        private String amountFormatted;
        private String amountRaw;
        private String feeOwedFormatted;
        private String feeOwedRaw;
        
        // Constructors, getters and setters
        public ParsedCLMMTokenInfo() {}
        
        public ParsedCLMMTokenInfo(String mint, String symbol, String name, int decimals, 
                                   String amountFormatted, String amountRaw,
                                   String feeOwedFormatted, String feeOwedRaw) {
            this.mint = mint;
            this.symbol = symbol;
            this.name = name;
            this.decimals = decimals;
            this.amountFormatted = amountFormatted;
            this.amountRaw = amountRaw;
            this.feeOwedFormatted = feeOwedFormatted;
            this.feeOwedRaw = feeOwedRaw;
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
        public String getAmountFormatted() { return amountFormatted; }
        public void setAmountFormatted(String amountFormatted) { this.amountFormatted = amountFormatted; }
        public String getAmountRaw() { return amountRaw; }
        public void setAmountRaw(String amountRaw) { this.amountRaw = amountRaw; }
        public String getFeeOwedFormatted() { return feeOwedFormatted; }
        public void setFeeOwedFormatted(String feeOwedFormatted) { this.feeOwedFormatted = feeOwedFormatted; }
        public String getFeeOwedRaw() { return feeOwedRaw; }
        public void setFeeOwedRaw(String feeOwedRaw) { this.feeOwedRaw = feeOwedRaw; }
    }
    
    public static class ParsedCLMMRewardInfo {
        private String mint;
        private String symbol;
        private String name;
        private int decimals;
        private String pendingRewardFormatted;
        private String pendingRewardRaw;
        private Double price;
        private String valueUSD;
        
        // Constructors, getters and setters
        public ParsedCLMMRewardInfo() {}
        
        // Getters and Setters
        public String getMint() { return mint; }
        public void setMint(String mint) { this.mint = mint; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getDecimals() { return decimals; }
        public void setDecimals(int decimals) { this.decimals = decimals; }
        public String getPendingRewardFormatted() { return pendingRewardFormatted; }
        public void setPendingRewardFormatted(String pendingRewardFormatted) { this.pendingRewardFormatted = pendingRewardFormatted; }
        public String getPendingRewardRaw() { return pendingRewardRaw; }
        public void setPendingRewardRaw(String pendingRewardRaw) { this.pendingRewardRaw = pendingRewardRaw; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getValueUSD() { return valueUSD; }
        public void setValueUSD(String valueUSD) { this.valueUSD = valueUSD; }
    }
    
    public static class ParsedCLMMPositionInfo {
        private String id;
        private String poolId;
        private String poolName;
        private String owner;
        private Integer tickLower;
        private Integer tickUpper;
        private String priceLowerFormatted;
        private String priceUpperFormatted;
        private String liquidityRaw;
        private ParsedCLMMTokenInfo token0;
        private ParsedCLMMTokenInfo token1;
        private List<ParsedCLMMRewardInfo> rewards;
        private Object status; // 可以是 String 或 Integer
        private Double usdValue;
        private Double unclaimedFeesUSD;
        private Double unclaimedRewardsUSD;
        private Double totalUnclaimedUSD;
        private Double tvlPercentage;
        
        // Constructors, getters and setters
        public ParsedCLMMPositionInfo() {
            this.rewards = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPoolId() { return poolId; }
        public void setPoolId(String poolId) { this.poolId = poolId; }
        public String getPoolName() { return poolName; }
        public void setPoolName(String poolName) { this.poolName = poolName; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public Integer getTickLower() { return tickLower; }
        public void setTickLower(Integer tickLower) { this.tickLower = tickLower; }
        public Integer getTickUpper() { return tickUpper; }
        public void setTickUpper(Integer tickUpper) { this.tickUpper = tickUpper; }
        public String getPriceLowerFormatted() { return priceLowerFormatted; }
        public void setPriceLowerFormatted(String priceLowerFormatted) { this.priceLowerFormatted = priceLowerFormatted; }
        public String getPriceUpperFormatted() { return priceUpperFormatted; }
        public void setPriceUpperFormatted(String priceUpperFormatted) { this.priceUpperFormatted = priceUpperFormatted; }
        public String getLiquidityRaw() { return liquidityRaw; }
        public void setLiquidityRaw(String liquidityRaw) { this.liquidityRaw = liquidityRaw; }
        public ParsedCLMMTokenInfo getToken0() { return token0; }
        public void setToken0(ParsedCLMMTokenInfo token0) { this.token0 = token0; }
        public ParsedCLMMTokenInfo getToken1() { return token1; }
        public void setToken1(ParsedCLMMTokenInfo token1) { this.token1 = token1; }
        public List<ParsedCLMMRewardInfo> getRewards() { return rewards; }
        public void setRewards(List<ParsedCLMMRewardInfo> rewards) { this.rewards = rewards; }
        public Object getStatus() { return status; }
        public void setStatus(Object status) { this.status = status; }
        public Double getUsdValue() { return usdValue; }
        public void setUsdValue(Double usdValue) { this.usdValue = usdValue; }
        public Double getUnclaimedFeesUSD() { return unclaimedFeesUSD; }
        public void setUnclaimedFeesUSD(Double unclaimedFeesUSD) { this.unclaimedFeesUSD = unclaimedFeesUSD; }
        public Double getUnclaimedRewardsUSD() { return unclaimedRewardsUSD; }
        public void setUnclaimedRewardsUSD(Double unclaimedRewardsUSD) { this.unclaimedRewardsUSD = unclaimedRewardsUSD; }
        public Double getTotalUnclaimedUSD() { return totalUnclaimedUSD; }
        public void setTotalUnclaimedUSD(Double totalUnclaimedUSD) { this.totalUnclaimedUSD = totalUnclaimedUSD; }
        public Double getTvlPercentage() { return tvlPercentage; }
        public void setTvlPercentage(Double tvlPercentage) { this.tvlPercentage = tvlPercentage; }
    }
    
    // 工具方法
    public static TokenInfo getKnownTokenInfo(String mint) {
        if (mint == null) return new TokenInfo("未知", 0, "未知代幣");
        return KNOWN_TOKENS_INFO.getOrDefault(mint, 
            new TokenInfo(mint.substring(0, Math.min(6, mint.length())) + "...", 0, "未知代幣"));
    }
    
    public static String formatBigNumber(Object bigNumObj, int decimals, int displayPrecision) {
        if (bigNumObj == null) return "0";
        
        String bigNumStr = String.valueOf(bigNumObj).trim();
        if (bigNumStr.isEmpty() || bigNumStr.equals("null")) return "0";
        
        try {
            if (decimals == 0 && !bigNumStr.contains(".")) {
                return new BigDecimal(bigNumStr).toString();
            }
            
            if (bigNumStr.contains(".")) {
                double parsedDouble = Double.parseDouble(bigNumStr);
                if (!Double.isNaN(parsedDouble)) {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(displayPrecision);
                    df.setGroupingUsed(false);
                    return df.format(parsedDouble);
                }
                return bigNumStr;
            }
            
            boolean isNegative = bigNumStr.startsWith("-");
            String absNumStr = isNegative ? bigNumStr.substring(1) : bigNumStr;
            
            String integerPartStr;
            String fractionalPartStr;
            
            if (absNumStr.length() > decimals) {
                integerPartStr = absNumStr.substring(0, absNumStr.length() - decimals);
                fractionalPartStr = absNumStr.substring(absNumStr.length() - decimals);
            } else {
                integerPartStr = "0";
                fractionalPartStr = String.format("%" + decimals + "s", absNumStr).replace(' ', '0');
            }
            
            String formattedFractional = fractionalPartStr.substring(0, Math.min(displayPrecision, fractionalPartStr.length()));
            formattedFractional = formattedFractional.replaceAll("0+$", "");
            
            if (formattedFractional.isEmpty()) {
                return (isNegative ? "-" : "") + integerPartStr;
            }
            return (isNegative ? "-" : "") + integerPartStr + "." + formattedFractional;
            
        } catch (Exception e) {
            try {
                double parsedDouble = Double.parseDouble(bigNumStr);
                if (!Double.isNaN(parsedDouble)) {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(displayPrecision);
                    df.setGroupingUsed(false);
                    return df.format(parsedDouble);
                }
            } catch (NumberFormatException ex) {
                // 忽略
            }
            return bigNumStr;
        }
    }
    
    public static String formatBigNumber(Object bigNumObj, int decimals) {
        return formatBigNumber(bigNumObj, decimals, 6);
    }
    
    // 安全取得字串值
    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull() && field.isTextual()) ? field.asText() : defaultValue;
    }
     private String getStringValue(JsonNode node, String fieldName) {
        return getStringValue(node, fieldName, "N/A");
    }
    
    // 安全取得數值
    private double getDoubleValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull() && field.isNumber()) {
            return field.asDouble();
        }
        return 0.0;
    }
    
    // 安全取得整數值
    private Integer getIntegerValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull() && field.isInt()) {
            return field.asInt();
        }
        return null;
    }
    
    // HTTP GET 請求
    public JsonNode httpGet(String url, String operationName) throws Exception {
        // System.out.printf("🌐 [%s] 正在從 %s 獲取數據...%n", operationName, url); 
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                // System.out.printf("✅ [%s] 數據獲取成功!%n", operationName); 
                return jsonNode;
            } else {
                System.err.printf("❌ [%s] HTTP 錯誤: %s, URL: %s%n", operationName, response.getStatusCode(), url);
                throw new RuntimeException("HTTP 錯誤: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.printf("❌ [%s] 請求失敗: %s, URL: %s%n", operationName, e.getMessage(), url);
            throw e;
        }
    }
    
    // 格式化 JSON 輸出
    public void printFormattedJSON(Object data, String title) {
        System.out.printf("%n📋 === %s ===%n", title);
        try {
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            System.out.println(jsonString);
        } catch (Exception e) {
            System.err.println("   ❌ 格式化 JSON 失敗: " + e.getMessage());
            System.out.println("   原始數據: " + data);
        }
        System.out.println("=".repeat(50));
    }
    
    // 獲取 Raydium Pool 從 API
    private JsonNode getRaydiumPoolFromAPI(String poolId, String lpMint) {
        try {
            JsonNode mainPairs = httpGet(RAYDIUM_V2_MAIN_PAIRS_API, "Raydium V2 Main Pairs (AMM)");
            if (mainPairs != null && mainPairs.isArray()) {
                for (JsonNode pair : mainPairs) {
                    String ammId = getStringValue(pair, "ammId");
                    String pairLpMint = getStringValue(pair, "lpMint");
                    
                    if (poolId.equals(ammId) || (lpMint != null && lpMint.equals(pairLpMint))) {
                        return pair;
                    }
                }
            }
        } catch (Exception e) {
            // 錯誤已在 httpGet 中記錄
        }
        return null;
    }
    
    // 模擬獲取 Vault 餘額
    private double getVaultBalance(String vaultAddress, int decimals) {
        if (vaultAddress.equals(AMM_WSOL_VAULT)) {
            return 1000000.0; 
        } else if (vaultAddress.equals(AMM_USDC_VAULT)) {
            return 150000000.0; 
        }
        return 0.0;
    }
    
    // 模擬獲取 AMM LP Token 供應量
    private Map<String, Object> getAMMLPTokenSupply() {
        Map<String, Object> result = new HashMap<>();
        result.put("amount", 5000000.0); 
        result.put("decimals", 6);
        return result;
    }
    
    // 獲取 AMM Pool 資訊
    public PoolInfo getAMMPoolInfo() {
        try {
            double wsolReserve = getVaultBalance(AMM_WSOL_VAULT, KNOWN_TOKENS_INFO.get(WSOL_MINT).getDecimals());
            double usdcReserve = getVaultBalance(AMM_USDC_VAULT, KNOWN_TOKENS_INFO.get(USDC_MINT).getDecimals());
            Map<String, Object> lpTokenDataMap = getAMMLPTokenSupply(); // 更名以避免與類名衝突
            JsonNode apiPoolInfo = getRaydiumPoolFromAPI(RAYDIUM_AMM_POOL_ID, AMM_LP_TOKEN_MINT);
            
            double price = wsolReserve > 0 ? usdcReserve / wsolReserve : 
                          (apiPoolInfo != null ? getDoubleValue(apiPoolInfo, "price") : 0);
            double tvl = apiPoolInfo != null ? getDoubleValue(apiPoolInfo, "tvl") : (usdcReserve * 2); // 簡化 TVL 計算
            
            PoolInfo poolInfo = new PoolInfo();
            poolInfo.setPoolId(RAYDIUM_AMM_POOL_ID);
            poolInfo.setPoolType("Raydium AMM V4");
            
            TokenInfo wsolInfo = KNOWN_TOKENS_INFO.get(WSOL_MINT);
            PoolInfo.TokenData baseToken = new PoolInfo.TokenData(
                WSOL_MINT, wsolInfo.getSymbol(), wsolInfo.getDecimals(), AMM_WSOL_VAULT, wsolReserve);
            poolInfo.setBaseToken(baseToken);
            
            TokenInfo usdcInfo = KNOWN_TOKENS_INFO.get(USDC_MINT);
            PoolInfo.TokenData quoteToken = new PoolInfo.TokenData(
                USDC_MINT, usdcInfo.getSymbol(), usdcInfo.getDecimals(), AMM_USDC_VAULT, usdcReserve);
            poolInfo.setQuoteToken(quoteToken);
            
            PoolInfo.LpTokenData lpToken = new PoolInfo.LpTokenData(
                AMM_LP_TOKEN_MINT, 
                (Double) lpTokenDataMap.get("amount"), 
                (Integer) lpTokenDataMap.get("decimals"));
            poolInfo.setLpToken(lpToken);
            
            PoolInfo.PoolStats poolStats = new PoolInfo.PoolStats();
            poolStats.setTvl(tvl);
            poolStats.setPrice(price);
            if (apiPoolInfo != null) {
                poolStats.setVolume24h(getDoubleValue(apiPoolInfo, "volume24h"));
                JsonNode aprNode = apiPoolInfo.get("apr");
                if (aprNode != null && aprNode.has("day")) { // 確保 apr.day 存在
                    poolStats.setApr(getDoubleValue(aprNode, "day"));
                }
            }
            poolInfo.setPoolStats(poolStats);
            
            return poolInfo;
            
        } catch (Exception error) {
            System.err.println("❌ 獲取 AMM Pool 資訊失敗: " + error.getMessage());
            return null;
        }
    }
    
    // CLMM Position 相關方法
    private JsonNode fetchCLMMData(String positionNftMint) {
        String apiUrl = CLMM_POSITION_API_URL_BASE + positionNftMint;
        try {
            // System.out.printf("🚀 開始獲取 CLMM 倉位數據: %s%n", positionNftMint); // 移至 analyzeCLMMPosition
            return httpGet(apiUrl, String.format("CLMM 倉位 %s", positionNftMint.substring(0, Math.min(8, positionNftMint.length()))));
        } catch (Exception error) {
            // 錯誤已在 httpGet 中記錄
            return null;
        }
    }
    
    private ParsedCLMMTokenInfo parseCLMMTokenInfo(
            String mint,
            Object rawAmount,
            Object rawFeeOwed,
            String apiProvidedSymbol,
            Integer apiProvidedDecimals,
            String apiProvidedName) {
        
        if (mint == null) return null;
        
        String symbol = apiProvidedSymbol;
        Integer decimals = apiProvidedDecimals;
        String name = apiProvidedName;
        
        if (symbol == null || decimals == null || "N/A".equals(symbol)) { // 檢查 N/A
            TokenInfo knownInfo = getKnownTokenInfo(mint);
            if (symbol == null || "N/A".equals(symbol)) symbol = knownInfo.getSymbol();
            if (decimals == null) decimals = knownInfo.getDecimals();
            if (name == null || "N/A".equals(name)) name = knownInfo.getName();
        }
        
        if (decimals == null) decimals = 0; // 避免 NullPointerException
        if (symbol == null) symbol = "未知";
        
        return new ParsedCLMMTokenInfo(
            mint,
            symbol,
            name,
            decimals,
            formatBigNumber(rawAmount, decimals),
            String.valueOf(rawAmount != null ? rawAmount : "0"),
            formatBigNumber(rawFeeOwed, decimals),
            String.valueOf(rawFeeOwed != null ? rawFeeOwed : "0")
        );
    }
    
    private ParsedCLMMPositionInfo parseCLMMData(JsonNode rawData, String positionIdFromInput) {
        ParsedCLMMPositionInfo result = new ParsedCLMMPositionInfo();
        result.setId(positionIdFromInput);

        if (rawData == null) {
            System.err.printf("❌ CLMM 倉位 %s 的原始數據為 null，無法解析。%n", positionIdFromInput);
            result.setPoolId("獲取失敗");
            result.setStatus("獲取失敗");
            // 其他欄位將保持其預設的 null 或 0 值
            return result;
        }

        JsonNode poolInfoNode = rawData.get("poolInfo"); // 更名以避免與類名衝突
        JsonNode positionInfoNode = rawData.get("positionInfo"); // 更名
        
        result.setPoolId(poolInfoNode != null ? getStringValue(poolInfoNode, "id", "獲取失敗") : getStringValue(rawData, "poolId", "獲取失敗"));
        result.setPoolName(poolInfoNode != null ? getStringValue(poolInfoNode, "name") : getStringValue(rawData, "poolName"));
        result.setOwner(getStringValue(rawData, "owner"));

        // 解析 Tokens (mintA, mintB)
        String mintA_address = null, mintA_symbol = null, mintA_name = null;
        Integer mintA_decimals = null;
        if (poolInfoNode != null && poolInfoNode.has("mintA")) {
            JsonNode mintANode = poolInfoNode.get("mintA");
            mintA_address = getStringValue(mintANode, "address");
            mintA_symbol = getStringValue(mintANode, "symbol");
            mintA_decimals = getIntegerValue(mintANode, "decimals");
            mintA_name = getStringValue(mintANode, "name");
        } else {
             mintA_address = getStringValue(rawData, "mint0");
        }

        String mintB_address = null, mintB_symbol = null, mintB_name = null;
        Integer mintB_decimals = null;
        if (poolInfoNode != null && poolInfoNode.has("mintB")) {
            JsonNode mintBNode = poolInfoNode.get("mintB");
            mintB_address = getStringValue(mintBNode, "address");
            mintB_symbol = getStringValue(mintBNode, "symbol");
            mintB_decimals = getIntegerValue(mintBNode, "decimals");
            mintB_name = getStringValue(mintBNode, "name");
        } else {
            mintB_address = getStringValue(rawData, "mint1");
        }
        
        // 解析 Amounts 和 Fees
        Object amountA_value = null, amountB_value = null, feeOwedA_value = null, feeOwedB_value = null;
        if (positionInfoNode != null) {
            amountA_value = positionInfoNode.has("amountA") ? (positionInfoNode.get("amountA").isTextual() ? positionInfoNode.get("amountA").asText() : positionInfoNode.get("amountA").asDouble()) : null;
            amountB_value = positionInfoNode.has("amountB") ? (positionInfoNode.get("amountB").isTextual() ? positionInfoNode.get("amountB").asText() : positionInfoNode.get("amountB").asDouble()) : null;
            if (positionInfoNode.has("unclaimedFee")) {
                JsonNode unclaimedFeeNode = positionInfoNode.get("unclaimedFee");
                feeOwedA_value = unclaimedFeeNode.has("amountA") ? (unclaimedFeeNode.get("amountA").isTextual() ? unclaimedFeeNode.get("amountA").asText() : unclaimedFeeNode.get("amountA").asDouble()) : null;
                feeOwedB_value = unclaimedFeeNode.has("amountB") ? (unclaimedFeeNode.get("amountB").isTextual() ? unclaimedFeeNode.get("amountB").asText() : unclaimedFeeNode.get("amountB").asDouble()) : null;
                result.setUnclaimedFeesUSD(unclaimedFeeNode.has("usdFeeValue") ? getDoubleValue(unclaimedFeeNode, "usdFeeValue") : null);
                result.setUnclaimedRewardsUSD(unclaimedFeeNode.has("usdRewardValue") ? getDoubleValue(unclaimedFeeNode, "usdRewardValue") : null);
                result.setTotalUnclaimedUSD(unclaimedFeeNode.has("usdValue") ? getDoubleValue(unclaimedFeeNode, "usdValue") : null);
            }
        } else { // Fallback if positionInfo is not present
            amountA_value = rawData.has("amount0") ? (rawData.get("amount0").isTextual() ? rawData.get("amount0").asText() : rawData.get("amount0").asDouble()) : null;
            amountB_value = rawData.has("amount1") ? (rawData.get("amount1").isTextual() ? rawData.get("amount1").asText() : rawData.get("amount1").asDouble()) : null;
            feeOwedA_value = rawData.has("feeOwed0") ? (rawData.get("feeOwed0").isTextual() ? rawData.get("feeOwed0").asText() : rawData.get("feeOwed0").asDouble()) : null;
            feeOwedB_value = rawData.has("feeOwed1") ? (rawData.get("feeOwed1").isTextual() ? rawData.get("feeOwed1").asText() : rawData.get("feeOwed1").asDouble()) : null;
        }

        result.setToken0(parseCLMMTokenInfo(mintA_address, amountA_value, feeOwedA_value, mintA_symbol, mintA_decimals, mintA_name));
        result.setToken1(parseCLMMTokenInfo(mintB_address, amountB_value, feeOwedB_value, mintB_symbol, mintB_decimals, mintB_name));
        
        // 解析 Rewards
        List<ParsedCLMMRewardInfo> rewards = new ArrayList<>();
        if (poolInfoNode != null && poolInfoNode.has("rewardDefaultInfos") && positionInfoNode != null && positionInfoNode.has("unclaimedFee")) {
            JsonNode rewardDefaultInfos = poolInfoNode.get("rewardDefaultInfos");
            JsonNode rewardArray = positionInfoNode.get("unclaimedFee").get("reward");
            if (rewardDefaultInfos.isArray() && rewardArray != null && rewardArray.isArray()) {
                for (int i = 0; i < rewardDefaultInfos.size() && i < rewardArray.size(); i++) {
                    JsonNode rewardMeta = rewardDefaultInfos.get(i);
                    JsonNode rewardAmountNode = rewardArray.get(i); // 更名
                    if (rewardMeta != null && rewardMeta.has("mint") && rewardAmountNode != null) {
                        JsonNode mintNode = rewardMeta.get("mint"); // 更名
                        ParsedCLMMRewardInfo rewardInfo = new ParsedCLMMRewardInfo();
                        rewardInfo.setMint(getStringValue(mintNode, "address"));
                        rewardInfo.setSymbol(getStringValue(mintNode, "symbol"));
                        rewardInfo.setName(getStringValue(mintNode, "name"));
                        Integer rDecimals = getIntegerValue(mintNode, "decimals");
                        rewardInfo.setDecimals(rDecimals != null ? rDecimals : getKnownTokenInfo(rewardInfo.getMint()).getDecimals());
                        Object rewardAmountValue = rewardAmountNode.isTextual() ? rewardAmountNode.asText() : rewardAmountNode.asDouble();
                        rewardInfo.setPendingRewardFormatted(formatBigNumber(rewardAmountValue, rewardInfo.getDecimals(), 8));
                        rewardInfo.setPendingRewardRaw(String.valueOf(rewardAmountValue));
                        if (rewardDefaultInfos.size() == 1 && result.getUnclaimedRewardsUSD() != null) {
                             rewardInfo.setValueUSD(String.format("%.4f", result.getUnclaimedRewardsUSD()));
                        }
                        rewards.add(rewardInfo);
                    }
                }
            }
        } else if (rawData.has("rewardInfos") && rawData.get("rewardInfos").isArray()) { // Fallback
             JsonNode rewardInfos = rawData.get("rewardInfos");
             for (JsonNode rawReward : rewardInfos) {
                ParsedCLMMRewardInfo rewardInfo = new ParsedCLMMRewardInfo();
                rewardInfo.setMint(getStringValue(rawReward, "rewardMint", getStringValue(rawReward, "mint")));
                TokenInfo knownInfo = getKnownTokenInfo(rewardInfo.getMint());
                Integer rDecimals = getIntegerValue(rawReward, "tokenDecimals");
                rewardInfo.setDecimals(rDecimals != null ? rDecimals : knownInfo.getDecimals());
                rewardInfo.setSymbol(knownInfo.getSymbol());
                rewardInfo.setName(knownInfo.getName());
                String pendingReward = getStringValue(rawReward, "pendingReward");
                rewardInfo.setPendingRewardFormatted(formatBigNumber(pendingReward, rewardInfo.getDecimals(), 8));
                rewardInfo.setPendingRewardRaw(pendingReward);
                rewardInfo.setPrice(rawReward.has("rewardPrice") ? getDoubleValue(rawReward, "rewardPrice") : null);
                 if (rewardInfo.getPrice() != null) {
                    try {
                        double amount = Double.parseDouble(rewardInfo.getPendingRewardFormatted());
                        rewardInfo.setValueUSD(String.format("%.4f", amount * rewardInfo.getPrice()));
                    } catch (NumberFormatException e) { /*忽略*/ }
                }
                rewards.add(rewardInfo);
             }
        }
        result.setRewards(rewards);

        // Tick, Liquidity, Status, USD Value, TVL Percentage
        result.setTickLower(getIntegerValue(rawData, "tickLower"));
        result.setTickUpper(getIntegerValue(rawData, "tickUpper"));
        result.setLiquidityRaw(getStringValue(rawData, "liquidity", null)); // null if not found

        JsonNode statusNode = rawData.get("status");
        if (statusNode != null && !statusNode.isNull()) {
            result.setStatus(statusNode.isTextual() ? statusNode.asText() : (statusNode.isInt() ? statusNode.asInt() : "格式未知"));
        } else {
            result.setStatus("獲取失敗");
        }
        
        // 從 attributes 獲取額外資訊 (如果主要路徑未提供)
        JsonNode attributes = rawData.get("attributes");
        if (attributes != null && attributes.isArray()) {
            for (JsonNode attr : attributes) {
                String traitType = getStringValue(attr, "trait_type");
                if (result.getTickLower() == null && ("tickLowerIndex".equals(traitType) || "tickLower".equals(traitType))) {
                    result.setTickLower(getIntegerValue(attr, "value"));
                }
                if (result.getTickUpper() == null && ("tickUpperIndex".equals(traitType) || "tickUpper".equals(traitType))) {
                     result.setTickUpper(getIntegerValue(attr, "value"));
                }
                if (result.getLiquidityRaw() == null && "liquidity".equals(traitType)) {
                    result.setLiquidityRaw(getStringValue(attr, "value"));
                }
            }
        }

        if (positionInfoNode != null) {
            result.setUsdValue(positionInfoNode.has("usdValue") ? getDoubleValue(positionInfoNode, "usdValue") : null);
            result.setTvlPercentage(positionInfoNode.has("tvlPercentage") ? getDoubleValue(positionInfoNode, "tvlPercentage") : null);
        }
        
        // 價格格式化
        String priceLowerStr = getStringValue(rawData, "priceLower", null);
        String priceUpperStr = getStringValue(rawData, "priceUpper", null);
        int quoteTokenDecimalsForPrice = result.getToken1() != null ? result.getToken1().getDecimals() : 6;

        if (priceLowerStr != null && !priceLowerStr.equals("N/A")) {
            result.setPriceLowerFormatted(formatBigNumber(priceLowerStr, quoteTokenDecimalsForPrice, 6));
        }
        if (priceUpperStr != null && !priceUpperStr.equals("N/A")) {
            result.setPriceUpperFormatted(formatBigNumber(priceUpperStr, quoteTokenDecimalsForPrice, 6));
        }
        
        return result;
    }
    
    // 顯示 CLMM Position 資訊
    public void displayCLMMPosition(ParsedCLMMPositionInfo parsedInfo) {
        System.out.println("\n✨ === CLMM 倉位詳細資訊 (中文) === ✨");
        System.out.printf("倉位 ID (NFT Mint): %s%n", parsedInfo.getId() != null ? parsedInfo.getId() : "獲取失敗");
        System.out.printf("所屬池 ID: %s%n", parsedInfo.getPoolId() != null ? parsedInfo.getPoolId() : "獲取失敗");
        
        if (parsedInfo.getPoolName() != null && !parsedInfo.getPoolName().equals("N/A")) {
            System.out.printf("池子名稱: %s%n", parsedInfo.getPoolName());
        }
        if (parsedInfo.getOwner() != null && !parsedInfo.getOwner().equals("N/A")) {
            System.out.printf("擁有者: %s%n", parsedInfo.getOwner());
        }
        
        System.out.println("\n--- 倉位狀態與範圍 ---");
        System.out.printf("  狀態: %s%n", parsedInfo.getStatus() != null ? parsedInfo.getStatus().toString() : "獲取失敗");
        System.out.printf("  Tick 範圍: %s 至 %s%n", 
                         parsedInfo.getTickLower() != null ? parsedInfo.getTickLower() : "獲取失敗",
                         parsedInfo.getTickUpper() != null ? parsedInfo.getTickUpper() : "獲取失敗");
        
        if (parsedInfo.getPriceLowerFormatted() != null && parsedInfo.getPriceUpperFormatted() != null) {
            String baseSymbol = (parsedInfo.getToken0() != null && parsedInfo.getToken0().getSymbol() != null) ? parsedInfo.getToken0().getSymbol() : "Token0";
            String quoteSymbol = (parsedInfo.getToken1() != null && parsedInfo.getToken1().getSymbol() != null) ? parsedInfo.getToken1().getSymbol() : "Token1";
            System.out.printf("  價格下限 (近似): %s %s / %s%n", parsedInfo.getPriceLowerFormatted(), quoteSymbol, baseSymbol);
            System.out.printf("  價格上限 (近似): %s %s / %s%n", parsedInfo.getPriceUpperFormatted(), quoteSymbol, baseSymbol);
        } else {
            System.out.println("  價格範圍: 獲取失敗 (API未提供有效 priceLower/priceUpper 或解析失敗)");
        }
        
        System.out.printf("  流動性 (原始值): %s%n", parsedInfo.getLiquidityRaw() != null ? parsedInfo.getLiquidityRaw() : "獲取失敗");
        
        if (parsedInfo.getTvlPercentage() != null) {
            System.out.printf("  池佔比 (TVL Percentage): %.6f%%%n", parsedInfo.getTvlPercentage());
        }
        if (parsedInfo.getUsdValue() != null) {
            System.out.printf("  倉位總估值 (USD): $%.2f%n", parsedInfo.getUsdValue());
        }
        
        // Token 0 資訊
        if (parsedInfo.getToken0() != null) {
            ParsedCLMMTokenInfo token0 = parsedInfo.getToken0();
            System.out.printf("%n--- 代幣 0 (%s - %s) ---%n", 
                             token0.getSymbol() != null ? token0.getSymbol() : "未知符號", 
                             token0.getName() != null ? token0.getName() : (token0.getMint() != null ? token0.getMint() : "未知 Mint"));
            System.out.printf("  估計數量: %s%n", token0.getAmountFormatted() != null ? token0.getAmountFormatted() : "獲取失敗");
            System.out.printf("  未領手續費: %s%n", token0.getFeeOwedFormatted() != null ? token0.getFeeOwedFormatted() : "獲取失敗");
        } else {
            System.out.println("\n--- 代幣 0 ---");
            System.out.println("  資訊獲取失敗");
        }
        
        // Token 1 資訊
        if (parsedInfo.getToken1() != null) {
            ParsedCLMMTokenInfo token1 = parsedInfo.getToken1();
            System.out.printf("%n--- 代幣 1 (%s - %s) ---%n", 
                             token1.getSymbol() != null ? token1.getSymbol() : "未知符號", 
                             token1.getName() != null ? token1.getName() : (token1.getMint() != null ? token1.getMint() : "未知 Mint"));
            System.out.printf("  估計數量: %s%n", token1.getAmountFormatted() != null ? token1.getAmountFormatted() : "獲取失敗");
            System.out.printf("  未領手續費: %s%n", token1.getFeeOwedFormatted() != null ? token1.getFeeOwedFormatted() : "獲取失敗");
        } else {
            System.out.println("\n--- 代幣 1 ---");
            System.out.println("  資訊獲取失敗");
        }
        
        // 未領取總收益
        System.out.println("\n--- 未領取總收益 ---");
        if (parsedInfo.getUnclaimedFeesUSD() != null) {
            System.out.printf("  手續費總美元價值: $%.2f%n", parsedInfo.getUnclaimedFeesUSD());
        } else {
            System.out.println("  手續費總美元價值: N/A (或無未領手續費)");
        }
        
        // 獎勵資訊
        if (parsedInfo.getRewards() != null && !parsedInfo.getRewards().isEmpty()) {
            System.out.println("\n🏆 獎勵資訊:");
            for (int i = 0; i < parsedInfo.getRewards().size(); i++) {
                ParsedCLMMRewardInfo reward = parsedInfo.getRewards().get(i);
                System.out.printf("  獎勵 %d: %s (%s)%n", 
                                 i + 1, 
                                 reward.getSymbol() != null ? reward.getSymbol() : "未知符號", 
                                 reward.getName() != null ? reward.getName() : (reward.getMint() != null ? reward.getMint() : "未知 Mint"));
                System.out.printf("    待領取數量: %s%n", reward.getPendingRewardFormatted() != null ? reward.getPendingRewardFormatted() : "獲取失敗");
                if (reward.getPrice() != null) {
                    System.out.printf("    參考價格: %s USD / %s%n", reward.getPrice(), reward.getSymbol() != null ? reward.getSymbol() : "未知符號");
                }
                if (reward.getValueUSD() != null) {
                    System.out.printf("    估計價值: $%s USD%n", reward.getValueUSD());
                }
            }
            if (parsedInfo.getUnclaimedRewardsUSD() != null) {
                System.out.printf("  獎勵總美元價值: $%.2f%n", parsedInfo.getUnclaimedRewardsUSD());
            }
        } else {
            System.out.println("🏆 獎勵資訊: 無或獲取失敗。");
        }
        
        if (parsedInfo.getTotalUnclaimedUSD() != null) {
            System.out.printf("  所有未領取總美元價值: $%.2f%n", parsedInfo.getTotalUnclaimedUSD());
        } else {
            System.out.println("  所有未領取總美元價值: N/A");
        }
        
        // 指定輸出 (用戶請求日誌)
        System.out.println("\n--- 指定輸出 (用戶請求日誌) ---");
        String token0Symbol = (parsedInfo.getToken0() != null && parsedInfo.getToken0().getSymbol() != null) ? parsedInfo.getToken0().getSymbol() : "Token0";
        String token1Symbol = (parsedInfo.getToken1() != null && parsedInfo.getToken1().getSymbol() != null) ? parsedInfo.getToken1().getSymbol() : "Token1";
        
        System.out.printf("池佔比 (TVL Percentage): %s%n", 
                         parsedInfo.getTvlPercentage() != null ? String.format("%.6f%%", parsedInfo.getTvlPercentage()) : "N/A");
        System.out.printf("倉位總美元價值 (USD Value): %s%n", 
                         parsedInfo.getUsdValue() != null ? String.format("$%.2f", parsedInfo.getUsdValue()) : "N/A");
        System.out.printf("%s 數量: %s%n", token0Symbol, 
                         (parsedInfo.getToken0() != null && parsedInfo.getToken0().getAmountFormatted() != null) ? parsedInfo.getToken0().getAmountFormatted() : "N/A");
        System.out.printf("%s 數量: %s%n", token1Symbol, 
                         (parsedInfo.getToken1() != null && parsedInfo.getToken1().getAmountFormatted() != null) ? parsedInfo.getToken1().getAmountFormatted() : "N/A");
        System.out.printf("未領取 %s 手續費: %s%n", token0Symbol, 
                         (parsedInfo.getToken0() != null && parsedInfo.getToken0().getFeeOwedFormatted() != null) ? parsedInfo.getToken0().getFeeOwedFormatted() : "N/A");
        System.out.printf("未領取 %s 手續費: %s%n", token1Symbol, 
                         (parsedInfo.getToken1() != null && parsedInfo.getToken1().getFeeOwedFormatted() != null) ? parsedInfo.getToken1().getFeeOwedFormatted() : "N/A");
        System.out.printf("手續費總美元價值 (USD Fee Value): %s%n", 
                         parsedInfo.getUnclaimedFeesUSD() != null ? String.format("$%.2f", parsedInfo.getUnclaimedFeesUSD()) : "N/A");
        System.out.printf("未領取總收益美元價值 (Unclaimed Total USD Value): %s%n", 
                         parsedInfo.getTotalUnclaimedUSD() != null ? String.format("$%.2f", parsedInfo.getTotalUnclaimedUSD()) : "N/A");
        
        System.out.println("=".repeat(70));
    }
    
    public void analyzeAMMPosition() { 
        PoolInfo poolData = getAMMPoolInfo();
        if (poolData == null) {
            // System.out.println("❌ 無法獲取 AMM Pool 資訊"); // 保持簡潔，錯誤已在 getAMMPoolInfo 記錄
            return;
        }
    }
    
    public void analyzeCLMMPosition(String positionNftMint) {
        System.out.printf("🚀 開始分析 CLMM 倉位: %s%n", positionNftMint);
        JsonNode rawData = fetchCLMMData(positionNftMint);
        // rawData 可能為 null，parseCLMMData 內部已處理此情況
        ParsedCLMMPositionInfo parsedInfo = parseCLMMData(rawData, positionNftMint);
        displayCLMMPosition(parsedInfo);
    }
}
