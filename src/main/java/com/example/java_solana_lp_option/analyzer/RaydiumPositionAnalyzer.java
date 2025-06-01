package com.example.java_solana_lp_option.analyzer;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
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
    
    // 已知代幣資訊
    private static final Map<String, TokenInfo> KNOWN_TOKENS_INFO = new HashMap<>();
    
    static {
        KNOWN_TOKENS_INFO.put(WSOL_MINT, new TokenInfo("WSOL", 9, "Wrapped SOL"));
        KNOWN_TOKENS_INFO.put(USDC_MINT, new TokenInfo("USDC", 6, "USD Coin"));
        KNOWN_TOKENS_INFO.put(RAY_MINT, new TokenInfo("RAY", 6, "Raydium"));
        KNOWN_TOKENS_INFO.put(AMM_LP_TOKEN_MINT, new TokenInfo("RAY-USDC-LP", 6, "Raydium LP Token (WSOL-USDC AMM V4)"));
    }
    
    public RaydiumPositionAnalyzer() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
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
        private Object status;
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
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : "N/A";
    }
    
    // 安全取得數值
    private double getDoubleValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asDouble();
        }
        return 0.0;
    }
    
    // 安全取得整數值
    private Integer getIntegerValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asInt();
        }
        return null;
    }
    
    // HTTP GET 請求
    public JsonNode httpGet(String url, String operationName) throws Exception {
        // System.out.printf("🌐 [%s] 正在從 %s 獲取數據...%n", operationName, url); // 移除此行
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                // System.out.printf("✅ [%s] 數據獲取成功!%n", operationName); // 移除此行
                return jsonNode;
            } else {
                throw new RuntimeException("HTTP 錯誤: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.printf("❌ [%s] 請求失敗: %s%n", operationName, e.getMessage());
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
        // System.out.printf("ℹ️ [getRaydiumPoolFromAPI] 嘗試從 API 獲取 AMM Pool %s 的額外資訊...%n", poolId);
        try {
            JsonNode mainPairs = httpGet(RAYDIUM_V2_MAIN_PAIRS_API, "Raydium V2 Main Pairs (AMM)");
            if (mainPairs != null && mainPairs.isArray()) {
                for (JsonNode pair : mainPairs) {
                    String ammId = getStringValue(pair, "ammId");
                    String pairLpMint = getStringValue(pair, "lpMint");
                    
                    if (poolId.equals(ammId) || (lpMint != null && lpMint.equals(pairLpMint))) {
                        // System.out.println("✅ 從 V2 Main Pairs API 找到 AMM Pool 資訊"); // 如果需要此日誌，可以取消註解
                        return pair;
                    }
                }
            }
        } catch (Exception e) {
            System.err.printf("   ⚠️ V2 Main Pairs API 查詢失敗: %s%n", e.getMessage());
        }
        return null;
    }
    
    // 模擬獲取 Vault 餘額 (由於無法直接連接 Solana RPC，這裡返回模擬數據)
    private double getVaultBalance(String vaultAddress, int decimals) {
        // System.out.printf("🔍 [模擬] 查詢 Vault 餘額: %s...%n", vaultAddress.substring(0, 8));
        
        // 模擬數據 - 在實際應用中需要連接 Solana RPC
        if (vaultAddress.equals(AMM_WSOL_VAULT)) {
            return 1000000.0; // 模擬 WSOL 餘額
        } else if (vaultAddress.equals(AMM_USDC_VAULT)) {
            return 150000000.0; // 模擬 USDC 餘額
        }
        return 0.0;
    }
    
    // 模擬獲取 AMM LP Token 供應量
    private Map<String, Object> getAMMLPTokenSupply() {
        // System.out.println("🔍 [模擬] 查詢 AMM LP Token 供應量...");
        Map<String, Object> result = new HashMap<>();
        result.put("amount", 5000000.0); // 模擬總供應量
        result.put("decimals", 6);
        return result;
    }
    
    // 獲取 AMM Pool 資訊
    public PoolInfo getAMMPoolInfo() {
        try {
            // System.out.printf("🔍 分析 Raydium AMM Pool: %s%n", RAYDIUM_AMM_POOL_ID); // 移除此行
            
            // 並行獲取資料
            double wsolReserve = getVaultBalance(AMM_WSOL_VAULT, KNOWN_TOKENS_INFO.get(WSOL_MINT).getDecimals());
            double usdcReserve = getVaultBalance(AMM_USDC_VAULT, KNOWN_TOKENS_INFO.get(USDC_MINT).getDecimals());
            Map<String, Object> lpTokenData = getAMMLPTokenSupply();
            JsonNode apiPoolInfo = getRaydiumPoolFromAPI(RAYDIUM_AMM_POOL_ID, AMM_LP_TOKEN_MINT);
            
            double price = wsolReserve > 0 ? usdcReserve / wsolReserve : 
                          (apiPoolInfo != null ? getDoubleValue(apiPoolInfo, "price") : 0);
            double tvl = apiPoolInfo != null ? getDoubleValue(apiPoolInfo, "tvl") : (usdcReserve * 2);
            
            PoolInfo poolInfo = new PoolInfo();
            poolInfo.setPoolId(RAYDIUM_AMM_POOL_ID);
            poolInfo.setPoolType("Raydium AMM V4");
            
            // 設定 Base Token (WSOL)
            TokenInfo wsolInfo = KNOWN_TOKENS_INFO.get(WSOL_MINT);
            PoolInfo.TokenData baseToken = new PoolInfo.TokenData(
                WSOL_MINT, wsolInfo.getSymbol(), wsolInfo.getDecimals(), AMM_WSOL_VAULT, wsolReserve);
            poolInfo.setBaseToken(baseToken);
            
            // 設定 Quote Token (USDC)
            TokenInfo usdcInfo = KNOWN_TOKENS_INFO.get(USDC_MINT);
            PoolInfo.TokenData quoteToken = new PoolInfo.TokenData(
                USDC_MINT, usdcInfo.getSymbol(), usdcInfo.getDecimals(), AMM_USDC_VAULT, usdcReserve);
            poolInfo.setQuoteToken(quoteToken);
            
            // 設定 LP Token
            PoolInfo.LpTokenData lpToken = new PoolInfo.LpTokenData(
                AMM_LP_TOKEN_MINT, 
                (Double) lpTokenData.get("amount"), 
                (Integer) lpTokenData.get("decimals"));
            poolInfo.setLpToken(lpToken);
            
            // 設定 Pool Stats
            PoolInfo.PoolStats poolStats = new PoolInfo.PoolStats();
            poolStats.setTvl(tvl);
            poolStats.setPrice(price);
            if (apiPoolInfo != null) {
                poolStats.setVolume24h(getDoubleValue(apiPoolInfo, "volume24h"));
                JsonNode aprNode = apiPoolInfo.get("apr");
                if (aprNode != null) {
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
    
    // 獲取 CLMM 資料
    private JsonNode fetchCLMMData(String positionNftMint) {
        String apiUrl = CLMM_POSITION_API_URL_BASE + positionNftMint;
        try {
            return httpGet(apiUrl, String.format("CLMM 倉位 %s... 查詢", positionNftMint.substring(0, 8)));
        } catch (Exception error) {
            System.err.printf("❌ 獲取 CLMM 倉位 %s 數據失敗: %s%n", positionNftMint, error.getMessage());
            return null;
        }
    }
    
    // 解析 CLMM Token 資訊
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
        
        if (symbol == null || decimals == null) {
            TokenInfo knownInfo = getKnownTokenInfo(mint);
            if (symbol == null) symbol = knownInfo.getSymbol();
            if (decimals == null) decimals = knownInfo.getDecimals();
            if (name == null && knownInfo.getName() != null) name = knownInfo.getName();
        }
        
        //if (decimals == null) decimals = 0;
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
    
    // 解析 CLMM 資料
    private ParsedCLMMPositionInfo parseCLMMData(JsonNode rawData, String positionIdFromInput) {
        JsonNode poolInfo = rawData.get("poolInfo");
        JsonNode positionInfo = rawData.get("positionInfo");
        
        // 獲取 mint 地址
        String mintA_address = null;
        String mintA_symbol = null;
        Integer mintA_decimals = null;
        String mintA_name = null;
        
        if (poolInfo != null) {
            JsonNode mintA = poolInfo.get("mintA");
            if (mintA != null) {
                mintA_address = getStringValue(mintA, "address");
                mintA_symbol = getStringValue(mintA, "symbol");
                mintA_decimals = getIntegerValue(mintA, "decimals");
                mintA_name = getStringValue(mintA, "name");
            }
        }
        if (mintA_address == null) {
            mintA_address = getStringValue(rawData, "mint0");
        }
        
        String mintB_address = null;
        String mintB_symbol = null;
        Integer mintB_decimals = null;
        String mintB_name = null;
        
        if (poolInfo != null) {
            JsonNode mintB = poolInfo.get("mintB");
            if (mintB != null) {
                mintB_address = getStringValue(mintB, "address");
                mintB_symbol = getStringValue(mintB, "symbol");
                mintB_decimals = getIntegerValue(mintB, "decimals");
                mintB_name = getStringValue(mintB, "name");
            }
        }
        if (mintB_address == null) {
            mintB_address = getStringValue(rawData, "mint1");
        }
        
        // 獲取 amount 值
        Object amountA_value = null;
        Object amountB_value = null;
        Object feeOwedA_value = null;
        Object feeOwedB_value = null;
        
        if (positionInfo != null) {
            JsonNode amountA = positionInfo.get("amountA");
            if (amountA != null) amountA_value = amountA.isTextual() ? amountA.asText() : amountA.asDouble();
            
            JsonNode amountB = positionInfo.get("amountB");
            if (amountB != null) amountB_value = amountB.isTextual() ? amountB.asText() : amountB.asDouble();
            
            JsonNode unclaimedFee = positionInfo.get("unclaimedFee");
            if (unclaimedFee != null) {
                JsonNode unclaimedAmountA = unclaimedFee.get("amountA");
                if (unclaimedAmountA != null) feeOwedA_value = unclaimedAmountA.isTextual() ? unclaimedAmountA.asText() : unclaimedAmountA.asDouble();
                
                JsonNode unclaimedAmountB = unclaimedFee.get("amountB");
                if (unclaimedAmountB != null) feeOwedB_value = unclaimedAmountB.isTextual() ? unclaimedAmountB.asText() : unclaimedAmountB.asDouble();
            }
        }
        
        if (amountA_value == null) {
            JsonNode amount0 = rawData.get("amount0");
            if (amount0 != null) amountA_value = amount0.isTextual() ? amount0.asText() : amount0.asDouble();
        }
        if (amountB_value == null) {
            JsonNode amount1 = rawData.get("amount1");
            if (amount1 != null) amountB_value = amount1.isTextual() ? amount1.asText() : amount1.asDouble();
        }
        if (feeOwedA_value == null) {
            JsonNode feeOwed0 = rawData.get("feeOwed0");
            if (feeOwed0 != null) feeOwedA_value = feeOwed0.isTextual() ? feeOwed0.asText() : feeOwed0.asDouble();
        }
        if (feeOwedB_value == null) {
            JsonNode feeOwed1 = rawData.get("feeOwed1");
            if (feeOwed1 != null) feeOwedB_value = feeOwed1.isTextual() ? feeOwed1.asText() : feeOwed1.asDouble();
        }
        
        ParsedCLMMTokenInfo token0 = parseCLMMTokenInfo(mintA_address, amountA_value, feeOwedA_value, mintA_symbol, mintA_decimals, mintA_name);
        ParsedCLMMTokenInfo token1 = parseCLMMTokenInfo(mintB_address, amountB_value, feeOwedB_value, mintB_symbol, mintB_decimals, mintB_name);
        
        // 獲取費用和獎勵資訊
        Double unclaimedFeesUSD = null;
        Double unclaimedRewardsUSD = null;
        Double totalUnclaimedUSD = null;
        
        if (positionInfo != null) {
            JsonNode unclaimedFee = positionInfo.get("unclaimedFee");
            if (unclaimedFee != null) {
                JsonNode usdFeeValue = unclaimedFee.get("usdFeeValue");
                if (usdFeeValue != null && !usdFeeValue.isNull()) {
                    unclaimedFeesUSD = usdFeeValue.asDouble();
                }
                
                JsonNode usdRewardValue = unclaimedFee.get("usdRewardValue");
                if (usdRewardValue != null && !usdRewardValue.isNull()) {
                    unclaimedRewardsUSD = usdRewardValue.asDouble();
                }
                
                JsonNode usdValue = unclaimedFee.get("usdValue");
                if (usdValue != null && !usdValue.isNull()) {
                    totalUnclaimedUSD = usdValue.asDouble();
                }
            }
        }
        
        // 解析獎勵資訊
        List<ParsedCLMMRewardInfo> rewards = new ArrayList<>();
        
        if (poolInfo != null && positionInfo != null) {
            JsonNode rewardDefaultInfos = poolInfo.get("rewardDefaultInfos");
            JsonNode unclaimedFee = positionInfo.get("unclaimedFee");
            
            if (rewardDefaultInfos != null && rewardDefaultInfos.isArray() && 
                unclaimedFee != null) {
                JsonNode rewardArray = unclaimedFee.get("reward");
                if (rewardArray != null && rewardArray.isArray()) {
                    
                    for (int i = 0; i < rewardDefaultInfos.size() && i < rewardArray.size(); i++) {
                        JsonNode rewardMeta = rewardDefaultInfos.get(i);
                        JsonNode rewardAmount = rewardArray.get(i);
                        
                        if (rewardMeta != null && rewardAmount != null) {
                            JsonNode mint = rewardMeta.get("mint");
                            if (mint != null) {
                                String rewardMintAddress = getStringValue(mint, "address");
                                String rewardSymbol = getStringValue(mint, "symbol");
                                String rewardName = getStringValue(mint, "name");
                                Integer rewardDecimals = getIntegerValue(mint, "decimals");
                                
                                TokenInfo knownInfo = getKnownTokenInfo(rewardMintAddress);
                                if (rewardDecimals == null) rewardDecimals = knownInfo.getDecimals();
                                if (rewardDecimals == 0) rewardDecimals = 6; // 預設值
                                
                                Object rewardAmountValue = rewardAmount.isTextual() ? rewardAmount.asText() : rewardAmount.asDouble();
                                
                                ParsedCLMMRewardInfo rewardInfo = new ParsedCLMMRewardInfo();
                                rewardInfo.setMint(rewardMintAddress);
                                rewardInfo.setSymbol(rewardSymbol != null && !rewardSymbol.equals("N/A") ? rewardSymbol : knownInfo.getSymbol());
                                rewardInfo.setName(rewardName != null && !rewardName.equals("N/A") ? rewardName : knownInfo.getName());
                                rewardInfo.setDecimals(rewardDecimals);
                                rewardInfo.setPendingRewardFormatted(formatBigNumber(rewardAmountValue, rewardDecimals, 8));
                                rewardInfo.setPendingRewardRaw(String.valueOf(rewardAmountValue));
                                
                                // 如果只有一個獎勵，使用總獎勵 USD 值
                                if (rewardDefaultInfos.size() == 1 && unclaimedRewardsUSD != null) {
                                    rewardInfo.setValueUSD(String.format("%.4f", unclaimedRewardsUSD));
                                }
                                
                                rewards.add(rewardInfo);
                            }
                        }
                    }
                }
            }
        } else {
            // 備用：直接從 rewardInfos 讀取
            JsonNode rewardInfos = rawData.get("rewardInfos");
            if (rewardInfos != null && rewardInfos.isArray()) {
                for (JsonNode rawReward : rewardInfos) {
                    String rewardMint = getStringValue(rawReward, "rewardMint");
                    if (rewardMint.equals("N/A")) {
                        rewardMint = getStringValue(rawReward, "mint");
                    }
                    
                    TokenInfo knownInfo = getKnownTokenInfo(rewardMint);
                    Integer rewardDecimals = getIntegerValue(rawReward, "tokenDecimals");
                    if (rewardDecimals == null) rewardDecimals = knownInfo.getDecimals();
                    if (rewardDecimals == 0) rewardDecimals = 6;
                    
                    String pendingReward = getStringValue(rawReward, "pendingReward");
                    Double rewardPrice = null;
                    JsonNode priceNode = rawReward.get("rewardPrice");
                    if (priceNode != null && !priceNode.isNull()) {
                        rewardPrice = priceNode.asDouble();
                    }
                    
                    ParsedCLMMRewardInfo rewardInfo = new ParsedCLMMRewardInfo();
                    rewardInfo.setMint(rewardMint);
                    rewardInfo.setSymbol(knownInfo.getSymbol());
                    rewardInfo.setName(knownInfo.getName());
                    rewardInfo.setDecimals(rewardDecimals);
                    rewardInfo.setPendingRewardFormatted(formatBigNumber(pendingReward, rewardDecimals, 8));
                    rewardInfo.setPendingRewardRaw(pendingReward);
                    rewardInfo.setPrice(rewardPrice);
                    
                    if (rewardPrice != null) {
                        try {
                            double amount = Double.parseDouble(formatBigNumber(pendingReward, rewardDecimals, 8));
                            double valueUSD = amount * rewardPrice;
                            rewardInfo.setValueUSD(String.format("%.4f", valueUSD));
                        } catch (NumberFormatException e) {
                            // 忽略轉換錯誤
                        }
                    }
                    
                    rewards.add(rewardInfo);
                }
            }
        }
        
        // 獲取其他屬性
        Integer tickLower = getIntegerValue(rawData, "tickLower");
        Integer tickUpper = getIntegerValue(rawData, "tickUpper");
        String liquidity = getStringValue(rawData, "liquidity");
        
        // 從 attributes 獲取額外資訊
        JsonNode attributes = rawData.get("attributes");
        if (attributes != null && attributes.isArray()) {
            for (JsonNode attr : attributes) {
                String traitType = getStringValue(attr, "trait_type");
                if ("tickLowerIndex".equals(traitType) || "tickLower".equals(traitType)) {
                    tickLower = getIntegerValue(attr, "value");
                } else if ("tickUpperIndex".equals(traitType) || "tickUpper".equals(traitType)) {
                    tickUpper = getIntegerValue(attr, "value");
                } else if ("liquidity".equals(traitType)) {
                    liquidity = getStringValue(attr, "value");
                }
            }
        }
        
        // 建構回傳物件
        ParsedCLMMPositionInfo result = new ParsedCLMMPositionInfo();
        result.setId(positionIdFromInput);
        result.setPoolId(poolInfo != null ? getStringValue(poolInfo, "id") : getStringValue(rawData, "poolId"));
        result.setPoolName(poolInfo != null ? getStringValue(poolInfo, "name") : getStringValue(rawData, "poolName"));
        result.setOwner(getStringValue(rawData, "owner"));
        result.setTickLower(tickLower);
        result.setTickUpper(tickUpper);
        
        // 價格格式化
        String priceLower = getStringValue(rawData, "priceLower");
        String priceUpper = getStringValue(rawData, "priceUpper");
        int quoteTokenDecimalsForPrice = token1 != null ? token1.getDecimals() : 6;
        
        if (!priceLower.equals("N/A")) {
            result.setPriceLowerFormatted(formatBigNumber(priceLower, quoteTokenDecimalsForPrice, 6));
        }
        if (!priceUpper.equals("N/A")) {
            result.setPriceUpperFormatted(formatBigNumber(priceUpper, quoteTokenDecimalsForPrice, 6));
        }
        
        result.setLiquidityRaw(liquidity.equals("N/A") ? null : liquidity);
        result.setToken0(token0);
        result.setToken1(token1);
        result.setRewards(rewards);
        
        JsonNode statusNode = rawData.get("status");
        if (statusNode != null) {
            result.setStatus(statusNode.isTextual() ? statusNode.asText() : statusNode.asInt());
        }
        
        if (positionInfo != null) {
            JsonNode usdValueNode = positionInfo.get("usdValue");
            if (usdValueNode != null && !usdValueNode.isNull()) {
                result.setUsdValue(usdValueNode.asDouble());
            }
            
            JsonNode tvlPercentageNode = positionInfo.get("tvlPercentage");
            if (tvlPercentageNode != null && !tvlPercentageNode.isNull()) {
                result.setTvlPercentage(tvlPercentageNode.asDouble());
            }
        }
        
        result.setUnclaimedFeesUSD(unclaimedFeesUSD);
        result.setUnclaimedRewardsUSD(unclaimedRewardsUSD);
        result.setTotalUnclaimedUSD(totalUnclaimedUSD);
        
        return result;
    }
    
    // 顯示 CLMM Position 資訊
    public void displayCLMMPosition(ParsedCLMMPositionInfo parsedInfo) {
        System.out.println("\n✨ === CLMM 倉位詳細資訊 (中文) === ✨");
        System.out.printf("倉位 ID (NFT Mint): %s%n", parsedInfo.getId() != null ? parsedInfo.getId() : "未知");
        System.out.printf("所屬池 ID: %s%n", parsedInfo.getPoolId() != null ? parsedInfo.getPoolId() : "未知");
        
        if (parsedInfo.getPoolName() != null && !parsedInfo.getPoolName().equals("N/A")) {
            System.out.printf("池子名稱: %s%n", parsedInfo.getPoolName());
        }
        if (parsedInfo.getOwner() != null && !parsedInfo.getOwner().equals("N/A")) {
            System.out.printf("擁有者: %s%n", parsedInfo.getOwner());
        }
        
        System.out.println("\n--- 倉位狀態與範圍 ---");
        System.out.printf("  狀態: %s%n", parsedInfo.getStatus() != null ? parsedInfo.getStatus() : "未知");
        System.out.printf("  Tick 範圍: %s 至 %s%n", 
                         parsedInfo.getTickLower() != null ? parsedInfo.getTickLower() : "未知",
                         parsedInfo.getTickUpper() != null ? parsedInfo.getTickUpper() : "未知");
        
        if (parsedInfo.getPriceLowerFormatted() != null && parsedInfo.getPriceUpperFormatted() != null) {
            String baseSymbol = parsedInfo.getToken0() != null ? parsedInfo.getToken0().getSymbol() : "Token0";
            String quoteSymbol = parsedInfo.getToken1() != null ? parsedInfo.getToken1().getSymbol() : "Token1";
            System.out.printf("  價格下限 (近似): %s %s / %s%n", parsedInfo.getPriceLowerFormatted(), quoteSymbol, baseSymbol);
            System.out.printf("  價格上限 (近似): %s %s / %s%n", parsedInfo.getPriceUpperFormatted(), quoteSymbol, baseSymbol);
        } else {
            System.out.println("  價格範圍: 未知 (API未提供priceLower/priceUpper)");
        }
        
        System.out.printf("  流動性 (原始值): %s%n", parsedInfo.getLiquidityRaw() != null ? parsedInfo.getLiquidityRaw() : "未知");
        
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
                             token0.getSymbol(), 
                             token0.getName() != null ? token0.getName() : token0.getMint());
            System.out.printf("  估計數量: %s%n", token0.getAmountFormatted());
            System.out.printf("  未領手續費: %s%n", token0.getFeeOwedFormatted());
        } else {
            System.out.println("\n--- 代幣 0 ---");
            System.out.println("  資訊未知");
        }
        
        // Token 1 資訊
        if (parsedInfo.getToken1() != null) {
            ParsedCLMMTokenInfo token1 = parsedInfo.getToken1();
            System.out.printf("%n--- 代幣 1 (%s - %s) ---%n", 
                             token1.getSymbol(), 
                             token1.getName() != null ? token1.getName() : token1.getMint());
            System.out.printf("  估計數量: %s%n", token1.getAmountFormatted());
            System.out.printf("  未領手續費: %s%n", token1.getFeeOwedFormatted());
        } else {
            System.out.println("\n--- 代幣 1 ---");
            System.out.println("  資訊未知");
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
                                 reward.getSymbol(), 
                                 reward.getName() != null ? reward.getName() : reward.getMint());
                System.out.printf("    待領取數量: %s%n", reward.getPendingRewardFormatted());
                if (reward.getPrice() != null) {
                    System.out.printf("    參考價格: %s USD / %s%n", reward.getPrice(), reward.getSymbol());
                }
                if (reward.getValueUSD() != null) {
                    System.out.printf("    估計價值: $%s USD%n", reward.getValueUSD());
                }
            }
            if (parsedInfo.getUnclaimedRewardsUSD() != null) {
                System.out.printf("  獎勵總美元價值: $%.2f%n", parsedInfo.getUnclaimedRewardsUSD());
            }
        } else {
            System.out.println("🏆 獎勵資訊: 無。");
        }
        
        if (parsedInfo.getTotalUnclaimedUSD() != null) {
            System.out.printf("  所有未領取總美元價值: $%.2f%n", parsedInfo.getTotalUnclaimedUSD());
        } else {
            System.out.println("  所有未領取總美元價值: N/A");
        }
        
        // 指定輸出 (用戶請求日誌)
        System.out.println("\n--- 指定輸出 (用戶請求日誌) ---");
        String token0Symbol = parsedInfo.getToken0() != null ? parsedInfo.getToken0().getSymbol() : "Token0";
        String token1Symbol = parsedInfo.getToken1() != null ? parsedInfo.getToken1().getSymbol() : "Token1";
        
        System.out.printf("池佔比 (TVL Percentage): %s%n", 
                         parsedInfo.getTvlPercentage() != null ? String.format("%.6f%%", parsedInfo.getTvlPercentage()) : "N/A");
        System.out.printf("倉位總美元價值 (USD Value): %s%n", 
                         parsedInfo.getUsdValue() != null ? String.format("$%.2f", parsedInfo.getUsdValue()) : "N/A");
        System.out.printf("%s 數量: %s%n", token0Symbol, 
                         parsedInfo.getToken0() != null ? parsedInfo.getToken0().getAmountFormatted() : "N/A");
        System.out.printf("%s 數量: %s%n", token1Symbol, 
                         parsedInfo.getToken1() != null ? parsedInfo.getToken1().getAmountFormatted() : "N/A");
        System.out.printf("未領取 %s 手續費: %s%n", token0Symbol, 
                         parsedInfo.getToken0() != null ? parsedInfo.getToken0().getFeeOwedFormatted() : "N/A");
        System.out.printf("未領取 %s 手續費: %s%n", token1Symbol, 
                         parsedInfo.getToken1() != null ? parsedInfo.getToken1().getFeeOwedFormatted() : "N/A");
        System.out.printf("手續費總美元價值 (USD Fee Value): %s%n", 
                         parsedInfo.getUnclaimedFeesUSD() != null ? String.format("$%.2f", parsedInfo.getUnclaimedFeesUSD()) : "N/A");
        System.out.printf("未領取總收益美元價值 (Unclaimed Total USD Value): %s%n", 
                         parsedInfo.getTotalUnclaimedUSD() != null ? String.format("$%.2f", parsedInfo.getTotalUnclaimedUSD()) : "N/A");
        
        System.out.println("=".repeat(70));
    }
    
    // 分析 AMM Position
    public void analyzeAMMPosition(String userWallet) {
        // System.out.println("🎯 Raydium AMM Position Liquidity 完整分析"); // 移除此行
        PoolInfo poolData = getAMMPoolInfo();
        if (poolData == null) {
            System.out.println("❌ 無法獲取 AMM Pool 資訊");
            return;
        }
        
        // 註解掉詳細 JSON 輸出，只顯示重要摘要
        // printFormattedJSON(poolData, "AMM Pool 詳細資訊");
        
        // 顯示簡潔的摘要資訊 - 以下皆移除
        // System.out.println("\n📊 === AMM Pool 摘要資訊 ===");
        // System.out.printf("Pool ID: %s%n", poolData.getPoolId());
        // System.out.printf("Pool 類型: %s%n", poolData.getPoolType());
        // System.out.printf("Base Token: %s (儲備: %.2f)%n", 
        //                  poolData.getBaseToken().getSymbol(), 
        //                  poolData.getBaseToken().getReserve());
        // System.out.printf("Quote Token: %s (儲備: %.2f)%n", 
        //                  poolData.getQuoteToken().getSymbol(), 
        //                  poolData.getQuoteToken().getReserve());
        // System.out.printf("當前價格: %.2f %s/%s%n", 
        //                  poolData.getPoolStats().getPrice(),
        //                  poolData.getQuoteToken().getSymbol(),
        //                  poolData.getBaseToken().getSymbol());
        // System.out.printf("總鎖定價值 (TVL): $%.0f%n", poolData.getPoolStats().getTvl());
        
        // if (poolData.getPoolStats().getApr() != null) {
        //     System.out.printf("年化收益率 (APR): %.2f%%%n", poolData.getPoolStats().getApr());
        // }
        // if (poolData.getPoolStats().getVolume24h() != null) {
        //     System.out.printf("24小時交易量: $%.0f%n", poolData.getPoolStats().getVolume24h());
        // }
        // System.out.println("=".repeat(50));
        
        if (userWallet != null && !userWallet.trim().isEmpty()) {
            // System.out.printf("👤 用戶錢包分析功能需要 Solana RPC 連接，目前僅顯示 Pool 資訊%n"); // 如果需要用戶錢包相關日誌，可以取消註解此行
        }
    }
    
    // 分析 CLMM Position
    public void analyzeCLMMPosition(String positionNftMint) {
        System.out.printf("🚀 開始分析 CLMM 倉位: %s%n", positionNftMint);
        JsonNode rawData = fetchCLMMData(positionNftMint);
        if (rawData != null) {
            ParsedCLMMPositionInfo parsedInfo = parseCLMMData(rawData, positionNftMint);
            displayCLMMPosition(parsedInfo);
        } else {
            System.out.printf("❌ 未能獲取或解析 CLMM 倉位 %s 的數據。%n", positionNftMint);
            ParsedCLMMPositionInfo fallbackInfo = new ParsedCLMMPositionInfo();
            fallbackInfo.setId(positionNftMint);
            fallbackInfo.setPoolId("獲取失敗");
            displayCLMMPosition(fallbackInfo);
        }
    }
}
