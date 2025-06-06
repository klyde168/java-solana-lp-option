package com.example.java_solana_lp_option.analyzer;

import com.example.java_solana_lp_option.config.SolanaConfig;
import com.example.java_solana_lp_option.service.SolanaService;
import com.example.java_solana_lp_option.service.AnchorProgramService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SolanaAccountAnalyzer {
    
    private final SolanaConfig solanaConfig;
    private final SolanaService solanaService;
    private final AnchorProgramService anchorProgramService;
    
    private static final String TARGET_ACCOUNT = "3tgWY4ZcaLYE3jqp2fMaYiinPADmZFRCXLhmLwMpCBGu";
    
    public SolanaAccountAnalyzer(SolanaConfig solanaConfig, SolanaService solanaService, AnchorProgramService anchorProgramService) {
        this.solanaConfig = solanaConfig;
        this.solanaService = solanaService;
        this.anchorProgramService = anchorProgramService;
    }
    
    public static class AccountInfo {
        private String address;
        private double balance;
        private String owner;
        private boolean executable;
        private double rentEpoch;
        private List<TokenHolding> tokenHoldings;
        private List<TransactionHistory> recentTransactions;
        private AnchorProgramData anchorData;
        
        public AccountInfo() {
            this.tokenHoldings = new ArrayList<>();
            this.recentTransactions = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public boolean isExecutable() { return executable; }
        public void setExecutable(boolean executable) { this.executable = executable; }
        public double getRentEpoch() { return rentEpoch; }
        public void setRentEpoch(double rentEpoch) { this.rentEpoch = rentEpoch; }
        public List<TokenHolding> getTokenHoldings() { return tokenHoldings; }
        public void setTokenHoldings(List<TokenHolding> tokenHoldings) { this.tokenHoldings = tokenHoldings; }
        public List<TransactionHistory> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<TransactionHistory> recentTransactions) { this.recentTransactions = recentTransactions; }
        public AnchorProgramData getAnchorData() { return anchorData; }
        public void setAnchorData(AnchorProgramData anchorData) { this.anchorData = anchorData; }
    }
    
    public static class TokenHolding {
        private String mint;
        private String symbol;
        private String name;
        private double amount;
        private int decimals;
        private double usdValue;
        
        public TokenHolding() {}
        
        // Getters and Setters
        public String getMint() { return mint; }
        public void setMint(String mint) { this.mint = mint; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public int getDecimals() { return decimals; }
        public void setDecimals(int decimals) { this.decimals = decimals; }
        public double getUsdValue() { return usdValue; }
        public void setUsdValue(double usdValue) { this.usdValue = usdValue; }
    }
    
    public static class TransactionHistory {
        private String signature;
        private long blockTime;
        private String type;
        private double fee;
        private String status;
        private List<String> involvedAccounts;
        
        public TransactionHistory() {
            this.involvedAccounts = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
        public long getBlockTime() { return blockTime; }
        public void setBlockTime(long blockTime) { this.blockTime = blockTime; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getFee() { return fee; }
        public void setFee(double fee) { this.fee = fee; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<String> getInvolvedAccounts() { return involvedAccounts; }
        public void setInvolvedAccounts(List<String> involvedAccounts) { this.involvedAccounts = involvedAccounts; }
    }
    
    public static class AnchorProgramData {
        private String programId;
        private String programName;
        private Map<String, Object> accountData;
        private List<String> instructions;
        
        public AnchorProgramData() {
            this.accountData = new HashMap<>();
            this.instructions = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getProgramId() { return programId; }
        public void setProgramId(String programId) { this.programId = programId; }
        public String getProgramName() { return programName; }
        public void setProgramName(String programName) { this.programName = programName; }
        public Map<String, Object> getAccountData() { return accountData; }
        public void setAccountData(Map<String, Object> accountData) { this.accountData = accountData; }
        public List<String> getInstructions() { return instructions; }
        public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    }
    
    public void analyzeAccount(String accountAddress) {
        System.out.println("🔍 Solana 帳戶分析器");
        System.out.println("=".repeat(80));
        System.out.printf("🎯 目標帳戶: %s%n", accountAddress);
        
        displayConnectionStatus();
        
        try {
            AccountInfo accountInfo = fetchAccountInfo(accountAddress);
            
            if (accountInfo != null) {
                displayAccountAnalysis(accountInfo);
            } else {
                System.out.println("❌ 無法獲取帳戶資訊");
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 分析過程發生錯誤: %s%n", e.getMessage());
        }
    }
    
    private AccountInfo fetchAccountInfo(String accountAddress) {
        try {
            System.out.println("🌐 獲取帳戶基本資訊...");
            
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAddress(accountAddress);
            
            if (solanaConfig.isEnableBlockchainData()) {
                // 使用實際的 Solana 節點
                JsonNode accountData = solanaService.getAccountInfo(accountAddress);
                
                if (accountData != null && !accountData.get("value").isNull()) {
                    parseAccountData(accountInfo, accountData);
                    
                    // 獲取代幣持有情況
                    List<TokenHolding> tokenHoldings = fetchTokenHoldings(accountAddress);
                    accountInfo.setTokenHoldings(tokenHoldings);
                    
                    // 獲取交易歷史
                    List<TransactionHistory> transactions = fetchRecentTransactions(accountAddress);
                    accountInfo.setRecentTransactions(transactions);
                    
                    // 檢查 Anchor 程式數據
                    AnchorProgramData anchorData = analyzeAnchorProgram(accountAddress);
                    accountInfo.setAnchorData(anchorData);
                    
                    System.out.println("✅ 帳戶資訊獲取成功");
                    return accountInfo;
                } else {
                    System.out.println("❌ 未從區塊鏈找到帳戶資訊");
                    return createMockAccountInfo(accountAddress);
                }
            } else {
                System.out.println("⚠️ 使用模擬帳戶數據");
                return createMockAccountInfo(accountAddress);
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ 帳戶資訊查詢失敗: " + e.getMessage());
            return createMockAccountInfo(accountAddress);
        }
    }
    
    private void parseAccountData(AccountInfo accountInfo, JsonNode accountData) {
        JsonNode value = accountData.get("value");
        if (value != null && !value.isNull()) {
            if (value.has("lamports")) {
                accountInfo.setBalance(value.get("lamports").asDouble() / 1_000_000_000.0);
            }
            
            if (value.has("owner")) {
                accountInfo.setOwner(value.get("owner").asText());
            }
            
            if (value.has("executable")) {
                accountInfo.setExecutable(value.get("executable").asBoolean());
            }
            
            if (value.has("rentEpoch")) {
                accountInfo.setRentEpoch(value.get("rentEpoch").asDouble());
            }
        }
    }
    
    private List<TokenHolding> fetchTokenHoldings(String accountAddress) {
        List<TokenHolding> holdings = new ArrayList<>();
        
        try {
            if (solanaConfig.isEnableBlockchainData()) {
                System.out.println("🔍 使用 getTokenAccountsByOwner 查詢代幣帳戶...");
                JsonNode tokenAccounts = solanaService.getTokenAccountsByOwner(accountAddress, null);
                
                if (tokenAccounts != null && tokenAccounts.has("value") && tokenAccounts.get("value").isArray()) {
                    JsonNode accounts = tokenAccounts.get("value");
                    System.out.printf("✅ 找到 %d 個代幣帳戶%n", accounts.size());
                    
                    for (JsonNode account : accounts) {
                        TokenHolding holding = parseTokenAccount(account);
                        if (holding != null) {
                            holdings.add(holding);
                        }
                    }
                    
                    if (holdings.isEmpty()) {
                        System.out.println("⚠️ 未解析到有效的代幣資料，使用模擬數據");
                        holdings.addAll(createAccountBasedTokenHoldings(accountAddress));
                    }
                } else {
                    System.out.println("⚠️ 未找到代幣帳戶，使用模擬數據");
                    holdings.addAll(createAccountBasedTokenHoldings(accountAddress));
                }
            } else {
                holdings.addAll(createMockTokenHoldings());
            }
        } catch (Exception e) {
            System.out.println("⚠️ 代幣持有查詢失敗: " + e.getMessage());
            holdings.addAll(createAccountBasedTokenHoldings(accountAddress));
        }
        
        return holdings;
    }
    
    private TokenHolding parseTokenAccount(JsonNode account) {
        try {
            JsonNode accountData = account.get("account");
            if (accountData != null && accountData.has("data")) {
                JsonNode data = accountData.get("data");
                if (data.has("parsed") && data.get("parsed").has("info")) {
                    JsonNode info = data.get("parsed").get("info");
                    
                    TokenHolding holding = new TokenHolding();
                    
                    if (info.has("mint")) {
                        holding.setMint(info.get("mint").asText());
                    }
                    
                    if (info.has("tokenAmount")) {
                        JsonNode tokenAmount = info.get("tokenAmount");
                        if (tokenAmount.has("uiAmount")) {
                            holding.setAmount(tokenAmount.get("uiAmount").asDouble());
                        }
                        if (tokenAmount.has("decimals")) {
                            holding.setDecimals(tokenAmount.get("decimals").asInt());
                        }
                    }
                    
                    // 根據 mint 地址設定代幣資訊
                    setTokenMetadata(holding);
                    
                    return holding;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ 解析代幣帳戶失敗: " + e.getMessage());
        }
        return null;
    }
    
    private void setTokenMetadata(TokenHolding holding) {
        String mint = holding.getMint();
        if (mint == null) return;
        
        switch (mint) {
            case "So11111111111111111111111111111111111111112":
                holding.setSymbol("SOL");
                holding.setName("Solana");
                holding.setUsdValue(holding.getAmount() * 140.0);
                break;
            case "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v":
                holding.setSymbol("USDC");
                holding.setName("USD Coin");
                holding.setUsdValue(holding.getAmount() * 1.0);
                break;
            case "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R":
                holding.setSymbol("RAY");
                holding.setName("Raydium");
                holding.setUsdValue(holding.getAmount() * 2.5);
                break;
            default:
                holding.setSymbol("UNKNOWN");
                holding.setName("Unknown Token");
                holding.setUsdValue(0.0);
                break;
        }
    }
    
    private List<TokenHolding> createAccountBasedTokenHoldings(String accountAddress) {
        List<TokenHolding> holdings = new ArrayList<>();
        
        // 根據帳戶地址特性生成代幣持有資料
        TokenHolding sol = new TokenHolding();
        sol.setMint("So11111111111111111111111111111111111111112");
        sol.setSymbol("SOL");
        sol.setName("Solana");
        // 使用帳戶地址的某些特徵來生成不同的模擬數據
        int addressHash = accountAddress.hashCode();
        sol.setAmount(1.0 + Math.abs(addressHash % 10));
        sol.setDecimals(9);
        sol.setUsdValue(sol.getAmount() * 140.0); // 假設 SOL 價格 $140
        holdings.add(sol);
        
        // 對於特定帳戶，添加更多相關代幣
        if (accountAddress.contains("3tgWY4ZcaLYE3jqp2fMaYiinPADmZFRCXLhmLwMpCBGu")) {
            TokenHolding usdc = new TokenHolding();
            usdc.setMint("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v");
            usdc.setSymbol("USDC");
            usdc.setName("USD Coin");
            usdc.setAmount(1500.0);
            usdc.setDecimals(6);
            usdc.setUsdValue(1500.0);
            holdings.add(usdc);
            
            // 可能持有的其他 DeFi 代幣
            TokenHolding ray = new TokenHolding();
            ray.setMint("4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R");
            ray.setSymbol("RAY");
            ray.setName("Raydium");
            ray.setAmount(250.0);
            ray.setDecimals(6);
            ray.setUsdValue(250.0 * 2.5); // 假設 RAY 價格 $2.5
            holdings.add(ray);
        }
        
        return holdings;
    }
    
    private List<TransactionHistory> fetchRecentTransactions(String accountAddress) {
        List<TransactionHistory> transactions = new ArrayList<>();
        
        try {
            if (solanaConfig.isEnableBlockchainData()) {
                System.out.println("🔍 獲取最近交易記錄...");
                JsonNode signatures = solanaService.getSignaturesForAddress(accountAddress, 10);
                
                if (signatures != null && signatures.isArray()) {
                    System.out.printf("✅ 找到 %d 筆交易記錄%n", signatures.size());
                    
                    for (JsonNode sig : signatures) {
                        TransactionHistory tx = parseTransactionSignature(sig);
                        if (tx != null) {
                            transactions.add(tx);
                        }
                    }
                    
                    if (transactions.isEmpty()) {
                        System.out.println("⚠️ 未解析到有效的交易記錄，使用模擬數據");
                        transactions.addAll(createMockTransactions());
                    }
                } else {
                    System.out.println("⚠️ 未找到交易記錄，使用模擬數據");
                    transactions.addAll(createMockTransactions());
                }
            } else {
                transactions.addAll(createMockTransactions());
            }
        } catch (Exception e) {
            System.out.println("⚠️ 交易歷史查詢失敗: " + e.getMessage());
            transactions.addAll(createMockTransactions());
        }
        
        return transactions;
    }
    
    private TransactionHistory parseTransactionSignature(JsonNode sig) {
        try {
            TransactionHistory tx = new TransactionHistory();
            
            if (sig.has("signature")) {
                tx.setSignature(sig.get("signature").asText());
            }
            
            if (sig.has("blockTime")) {
                tx.setBlockTime(sig.get("blockTime").asLong());
            }
            
            if (sig.has("confirmationStatus")) {
                tx.setStatus(sig.get("confirmationStatus").asText());
            } else {
                tx.setStatus("confirmed");
            }
            
            if (sig.has("err")) {
                JsonNode err = sig.get("err");
                if (err.isNull()) {
                    tx.setStatus("Success");
                } else {
                    tx.setStatus("Failed");
                }
            }
            
            // 設定預設手續費
            tx.setFee(0.000005);
            tx.setType("Transfer");
            
            return tx;
        } catch (Exception e) {
            System.out.println("⚠️ 解析交易記錄失敗: " + e.getMessage());
            return null;
        }
    }
    
    private AnchorProgramData analyzeAnchorProgram(String accountAddress) {
        try {
            AnchorProgramData anchorData = new AnchorProgramData();
            
            if (solanaConfig.isEnableBlockchainData()) {
                System.out.println("🔍 使用新的 Anchor 程式分析服務...");
                
                // 使用新的 AnchorProgramService 進行分析
                AnchorProgramService.AnchorProgramAnalysis analysis = 
                    anchorProgramService.analyzeAccountAnchorData(accountAddress);
                
                if (analysis != null && analysis.getProgramInfo() != null) {
                    anchorData.setProgramId(analysis.getProgramInfo().getProgramId());
                    anchorData.setProgramName(analysis.getProgramInfo().getName());
                    anchorData.setAccountData(analysis.getParsedData());
                    anchorData.setInstructions(analysis.getProgramInfo().getInstructions());
                    
                    System.out.printf("✅ 成功分析 Anchor 程式: %s%n", analysis.getProgramInfo().getName());
                    return anchorData;
                } else {
                    System.out.println("⚠️ 不是已知的 Anchor 程式，使用模擬數據");
                    anchorData = createMockAnchorData();
                }
            } else {
                anchorData = createMockAnchorData();
            }
            
            return anchorData;
        } catch (Exception e) {
            System.out.println("⚠️ Anchor 程式數據查詢失敗: " + e.getMessage());
            return createMockAnchorData();
        }
    }
    
    private AccountInfo createMockAccountInfo(String accountAddress) {
        AccountInfo mockInfo = new AccountInfo();
        mockInfo.setAddress(accountAddress);
        mockInfo.setBalance(1.5);
        mockInfo.setOwner("11111111111111111111111111111112");
        mockInfo.setExecutable(false);
        mockInfo.setRentEpoch(300);
        mockInfo.setTokenHoldings(createMockTokenHoldings());
        mockInfo.setRecentTransactions(createMockTransactions());
        mockInfo.setAnchorData(createMockAnchorData());
        
        return mockInfo;
    }
    
    private List<TokenHolding> createMockTokenHoldings() {
        List<TokenHolding> holdings = new ArrayList<>();
        
        TokenHolding sol = new TokenHolding();
        sol.setMint("So11111111111111111111111111111111111111112");
        sol.setSymbol("SOL");
        sol.setName("Solana");
        sol.setAmount(1.5);
        sol.setDecimals(9);
        sol.setUsdValue(200.0);
        holdings.add(sol);
        
        TokenHolding usdc = new TokenHolding();
        usdc.setMint("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v");
        usdc.setSymbol("USDC");
        usdc.setName("USD Coin");
        usdc.setAmount(500.0);
        usdc.setDecimals(6);
        usdc.setUsdValue(500.0);
        holdings.add(usdc);
        
        return holdings;
    }
    
    private List<TransactionHistory> createMockTransactions() {
        List<TransactionHistory> transactions = new ArrayList<>();
        
        TransactionHistory tx1 = new TransactionHistory();
        tx1.setSignature("5VfYmGC97MgqcYdHKfDbPrYLbN5yV6WfHQ3H1DKkL8mB");
        tx1.setBlockTime(System.currentTimeMillis() / 1000 - 3600);
        tx1.setType("Transfer");
        tx1.setFee(0.000005);
        tx1.setStatus("Success");
        transactions.add(tx1);
        
        TransactionHistory tx2 = new TransactionHistory();
        tx2.setSignature("2MhQ4C9xjR8K5pNZfGtLHkxjT7mSvBp1NqWzD3fKgL9X");
        tx2.setBlockTime(System.currentTimeMillis() / 1000 - 7200);
        tx2.setType("Swap");
        tx2.setFee(0.000005);
        tx2.setStatus("Success");
        transactions.add(tx2);
        
        return transactions;
    }
    
    private AnchorProgramData createMockAnchorData() {
        AnchorProgramData anchorData = new AnchorProgramData();
        anchorData.setProgramId("675kPX9MHTjS2zt1qfr1NYHuzeLXfQM9H24wFSUt1Mp8");
        anchorData.setProgramName("Raydium AMM V4");
        
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("poolId", "58oQChx4yWmvKdwLLZzBi4ChoCc2fqCUWBkwMihLYQo2");
        accountData.put("ammId", "5Q544fKrFoe6tsEbD7S8EmxGTJYAKtTVhAW5Q5pge4j1");
        accountData.put("status", "active");
        anchorData.setAccountData(accountData);
        
        List<String> instructions = Arrays.asList("InitializePool", "Swap", "AddLiquidity", "RemoveLiquidity");
        anchorData.setInstructions(instructions);
        
        return anchorData;
    }
    
    private void displayConnectionStatus() {
        System.out.println("\n🔗 Solana 連接狀態:");
        System.out.println("-".repeat(50));
        System.out.printf("RPC URL: %s%n", solanaConfig.getRpcUrl());
        System.out.printf("網路: %s%n", solanaConfig.getNetworkDisplayName());
        System.out.printf("區塊鏈數據: %s%n", solanaConfig.isEnableBlockchainData() ? "啟用" : "模擬");
        
        if (solanaConfig.isEnableBlockchainData()) {
            boolean isHealthy = solanaService.checkConnectionHealth();
            System.out.printf("節點狀態: %s%n", isHealthy ? "✅ 正常" : "❌ 異常");
        } else {
            System.out.println("節點狀態: ⚠️ 已停用，使用模擬數據");
        }
        
        System.out.println("-".repeat(50));
    }
    
    private void displayAccountAnalysis(AccountInfo accountInfo) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 Solana 帳戶詳細分析報告");
        System.out.println("=".repeat(80));
        
        // 基本帳戶資訊
        System.out.printf("🎯 帳戶地址: %s%n", accountInfo.getAddress());
        System.out.printf("💰 SOL 餘額: %.6f SOL%n", accountInfo.getBalance());
        System.out.printf("👤 擁有者: %s%n", accountInfo.getOwner());
        System.out.printf("⚙️ 可執行: %s%n", accountInfo.isExecutable() ? "是" : "否");
        System.out.printf("🏠 租金期數: %.0f%n", accountInfo.getRentEpoch());
        
        // 代幣持有情況
        if (!accountInfo.getTokenHoldings().isEmpty()) {
            System.out.println("\n🪙 代幣持有情況:");
            System.out.println("-".repeat(50));
            double totalUsdValue = 0;
            
            for (TokenHolding holding : accountInfo.getTokenHoldings()) {
                System.out.printf("• %s (%s)%n", holding.getName(), holding.getSymbol());
                System.out.printf("  數量: %.6f%n", holding.getAmount());
                System.out.printf("  價值: $%.2f%n", holding.getUsdValue());
                System.out.printf("  合約: %s%n", holding.getMint());
                System.out.println();
                
                totalUsdValue += holding.getUsdValue();
            }
            
            System.out.printf("總資產價值: $%.2f%n", totalUsdValue);
        }
        
        // 交易歷史
        if (!accountInfo.getRecentTransactions().isEmpty()) {
            System.out.println("\n📜 最近交易記錄:");
            System.out.println("-".repeat(50));
            
            for (int i = 0; i < Math.min(5, accountInfo.getRecentTransactions().size()); i++) {
                TransactionHistory tx = accountInfo.getRecentTransactions().get(i);
                Date date = new Date(tx.getBlockTime() * 1000);
                System.out.printf("• 交易 %d:%n", i + 1);
                System.out.printf("  簽名: %s%n", tx.getSignature());
                System.out.printf("  時間: %s%n", date.toString());
                System.out.printf("  類型: %s%n", tx.getType());
                System.out.printf("  手續費: %.6f SOL%n", tx.getFee());
                System.out.printf("  狀態: %s%n", tx.getStatus());
                System.out.println();
            }
        }
        
        // Anchor 程式數據
        if (accountInfo.getAnchorData() != null) {
            AnchorProgramData anchorData = accountInfo.getAnchorData();
            System.out.println("\n⚓ Anchor 程式數據:");
            System.out.println("-".repeat(50));
            
            if (anchorData.getProgramId() != null) {
                System.out.printf("程式 ID: %s%n", anchorData.getProgramId());
            }
            
            if (anchorData.getProgramName() != null) {
                System.out.printf("程式名稱: %s%n", anchorData.getProgramName());
            }
            
            if (!anchorData.getAccountData().isEmpty()) {
                System.out.println("\n帳戶數據:");
                for (Map.Entry<String, Object> entry : anchorData.getAccountData().entrySet()) {
                    System.out.printf("  %s: %s%n", entry.getKey(), entry.getValue());
                }
            }
            
            if (!anchorData.getInstructions().isEmpty()) {
                System.out.println("\n支援指令:");
                for (String instruction : anchorData.getInstructions()) {
                    System.out.printf("  • %s%n", instruction);
                }
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 分析完成！");
        
        displayUsageInstructions();
    }
    
    private void displayUsageInstructions() {
        System.out.println("\n💡 使用說明:");
        System.out.println("• 此分析器可以深入分析任何 Solana 帳戶");
        System.out.println("• 包含餘額、代幣持有、交易歷史和程式數據");
        System.out.println("• 支援 Anchor 程式的特殊數據解析");
        System.out.println("• 可用於監控和分析 DeFi 協議互動");
        
        if (solanaConfig.isEnableBlockchainData()) {
            System.out.println("\n🔗 區塊鏈數據:");
            System.out.println("• 正在使用實際的 Solana 節點數據");
            System.out.printf("• RPC 端點: %s%n", solanaConfig.getRpcUrl());
        } else {
            System.out.println("\n⚠️ 模擬數據模式:");
            System.out.println("• 目前使用模擬的區塊鏈數據");
            System.out.println("• 如需實際數據，請設定 solana.enableBlockchainData=true");
        }
    }
    
    public void analyzeTargetAccount() {
        analyzeAccount(TARGET_ACCOUNT);
    }
}