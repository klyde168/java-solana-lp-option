package com.example.java_solana_lp_option.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Solana 相關配置 - 支援實際節點連接
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "solana")
public class SolanaConfig {
    
    /**
     * Solana RPC URL
     */
    private String rpcUrl = "https://api.mainnet-beta.solana.com";
    
    /**
     * RPC 連接超時時間 (毫秒)
     */
    private int connectTimeout = 30000;
    
    /**
     * RPC 讀取超時時間 (毫秒)
     */
    private int readTimeout = 60000;
    
    /**
     * API 重試次數
     */
    private int maxRetries = 5;
    
    /**
     * 重試間隔 (毫秒)
     */
    private long retryDelay = 2000;
    
    /**
     * 是否啟用區塊鏈數據讀取
     */
    private boolean enableBlockchainData = true;
    
    /**
     * Solana 網路環境
     */
    private String network = "mainnet-beta";
    
    /**
     * 交易確認等級
     */
    private String commitment = "confirmed";
    
    /**
     * Token Program ID
     */
    private String tokenProgramId = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
    
    /**
     * Raydium CLMM Program IDs
     */
    private String[] raydiumClmmProgramIds = {
        "CAMMCzo5YL8w4VFF8KVHrK22GGUsp5VTaW7grrKgrWqK", // 最新版本
        "CAMMCzo5YL8w4VFF8KVHrK22GGUQpMkFr9g8CV6sjMjA", // 舊版本
        "devi51mZmdwUJGU9hjN27vEz64Gps7uUefqxg27EAtH"   // 可能的其他版本
    };
    
    /**
     * 已知代幣 Mint 地址
     */
    private String wsolMint = "So11111111111111111111111111111111111111112";
    private String usdcMint = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v";
    private String rayMint = "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R";
    
    /**
     * Position 帳戶查詢配置
     */
    private int maxAccountsPerRequest = 100;
    private int accountScanTimeout = 30000;
    
    // Getters and Setters
    public String getRpcUrl() {
        return rpcUrl;
    }
    
    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public boolean isEnableBlockchainData() {
        return enableBlockchainData;
    }
    
    public void setEnableBlockchainData(boolean enableBlockchainData) {
        this.enableBlockchainData = enableBlockchainData;
    }
    
    public String getNetwork() {
        return network;
    }
    
    public void setNetwork(String network) {
        this.network = network;
    }
    
    public String getCommitment() {
        return commitment;
    }
    
    public void setCommitment(String commitment) {
        this.commitment = commitment;
    }
    
    public String getTokenProgramId() {
        return tokenProgramId;
    }
    
    public void setTokenProgramId(String tokenProgramId) {
        this.tokenProgramId = tokenProgramId;
    }
    
    public String[] getRaydiumClmmProgramIds() {
        return raydiumClmmProgramIds;
    }
    
    public void setRaydiumClmmProgramIds(String[] raydiumClmmProgramIds) {
        this.raydiumClmmProgramIds = raydiumClmmProgramIds;
    }
    
    public String getWsolMint() {
        return wsolMint;
    }
    
    public void setWsolMint(String wsolMint) {
        this.wsolMint = wsolMint;
    }
    
    public String getUsdcMint() {
        return usdcMint;
    }
    
    public void setUsdcMint(String usdcMint) {
        this.usdcMint = usdcMint;
    }
    
    public String getRayMint() {
        return rayMint;
    }
    
    public void setRayMint(String rayMint) {
        this.rayMint = rayMint;
    }
    
    public int getMaxAccountsPerRequest() {
        return maxAccountsPerRequest;
    }
    
    public void setMaxAccountsPerRequest(int maxAccountsPerRequest) {
        this.maxAccountsPerRequest = maxAccountsPerRequest;
    }
    
    public int getAccountScanTimeout() {
        return accountScanTimeout;
    }
    
    public void setAccountScanTimeout(int accountScanTimeout) {
        this.accountScanTimeout = accountScanTimeout;
    }
    
    /**
     * 檢查是否為主網
     */
    public boolean isMainnet() {
        return "mainnet-beta".equals(network);
    }
    
    /**
     * 檢查是否為測試網
     */
    public boolean isTestnet() {
        return "testnet".equals(network);
    }
    
    /**
     * 檢查是否為開發網
     */
    public boolean isDevnet() {
        return "devnet".equals(network);
    }
    
    /**
     * 獲取網路顯示名稱
     */
    public String getNetworkDisplayName() {
        switch (network) {
            case "mainnet-beta":
                return "主網 (Mainnet)";
            case "testnet":
                return "測試網 (Testnet)";
            case "devnet":
                return "開發網 (Devnet)";
            default:
                return network;
        }
    }
}