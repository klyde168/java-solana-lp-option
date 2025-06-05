package com.example.java_solana_lp_option.service;

import com.example.java_solana_lp_option.config.SolanaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Base64;

/**
 * Solana 區塊鏈服務 - 處理實際的 RPC 呼叫
 */
@Service
public class SolanaService {
    
    private final SolanaConfig solanaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AtomicLong requestId = new AtomicLong(1);
    
    public SolanaService(SolanaConfig solanaConfig) {
        this.solanaConfig = solanaConfig;
        this.objectMapper = new ObjectMapper();
        
        // 設定 RestTemplate 超時
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(solanaConfig.getConnectTimeout());
        factory.setReadTimeout(solanaConfig.getReadTimeout());
        this.restTemplate = new RestTemplate(factory);
    }
    
    /**
     * 通用的 Solana RPC 呼叫方法
     */
    private JsonNode callSolanaRPC(String method, Object... params) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId.getAndIncrement());
        request.put("method", method);
        request.put("params", Arrays.asList(params));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Java-Solana-LP-Option/1.0");
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        System.out.printf("🌐 呼叫 Solana RPC: %s%n", method);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            solanaConfig.getRpcUrl(), entity, String.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.has("error")) {
                JsonNode error = jsonResponse.get("error");
                throw new RuntimeException("Solana RPC 錯誤: " + error.toString());
            }
            
            return jsonResponse.get("result");
        } else {
            throw new RuntimeException("HTTP 錯誤: " + response.getStatusCode());
        }
    }
    
    /**
     * 獲取帳戶資訊
     */
    public JsonNode getAccountInfo(String publicKey) throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("encoding", "base64");
        config.put("commitment", solanaConfig.getCommitment());
        
        return callSolanaRPC("getAccountInfo", publicKey, config);
    }
    
    /**
     * 獲取多個帳戶資訊
     */
    public JsonNode getMultipleAccounts(List<String> publicKeys) throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("encoding", "base64");
        config.put("commitment", solanaConfig.getCommitment());
        
        return callSolanaRPC("getMultipleAccounts", publicKeys, config);
    }
    
    /**
     * 獲取程序帳戶
     */
    public JsonNode getProgramAccounts(String programId) throws Exception {
        return getProgramAccounts(programId, null);
    }
    
    /**
     * 獲取程序帳戶（帶過濾器）
     */
    public JsonNode getProgramAccounts(String programId, List<Map<String, Object>> filters) throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("encoding", "base64");
        config.put("commitment", solanaConfig.getCommitment());
        
        if (filters != null && !filters.isEmpty()) {
            config.put("filters", filters);
        }
        
        return callSolanaRPC("getProgramAccounts", programId, config);
    }
    
    /**
     * 檢查是否為 Raydium CLMM Program
     */
    private boolean isRaydiumClmmProgram(String programId) {
        for (String raydiumProgramId : solanaConfig.getRaydiumClmmProgramIds()) {
            if (raydiumProgramId.equals(programId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 獲取 Token Extensions 帳戶資訊
     */
    public TokenExtensionsInfo getTokenExtensions(String mintAddress) {
        try {
            System.out.printf("🔍 獲取 Token Extensions 資訊: %s%n", mintAddress);
            
            JsonNode accountInfo = getAccountInfo(mintAddress);
            
            if (accountInfo == null || accountInfo.get("value").isNull()) {
                System.out.println("❌ 帳戶不存在");
                return null;
            }
            
            JsonNode value = accountInfo.get("value");
            String owner = value.get("owner").asText();
            
            System.out.printf("🔍 帳戶擁有者: %s%n", owner);
            
            // 檢查是否為 Token Program 擁有的 NFT
            if (solanaConfig.getTokenProgramId().equals(owner)) {
                System.out.println("✅ 這是標準 Token Program 帳戶");
                String data = value.get("data").get(0).asText();
                byte[] accountData = Base64.getDecoder().decode(data);
                return parseTokenExtensions(accountData, mintAddress);
            } 
            // 檢查是否為 Token Extensions Program 擁有
            else if ("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb".equals(owner)) {
                System.out.println("✅ 這是 Token Extensions Program 帳戶");
                String data = value.get("data").get(0).asText();
                byte[] accountData = Base64.getDecoder().decode(data);
                return parseTokenExtensions(accountData, mintAddress);
            }
            // 檢查是否為 Raydium CLMM Program 擁有的 Position 帳戶
            else if (isRaydiumClmmProgram(owner)) {
                System.out.printf("✅ 這是 Raydium CLMM Position 帳戶 (程序: %s)%n", owner);
                // 對於 CLMM Position 帳戶，我們創建一個虛擬的 TokenExtensionsInfo
                TokenExtensionsInfo info = new TokenExtensionsInfo();
                info.setMintAddress(mintAddress);
                info.setName("Raydium CLMM Position");
                info.setSymbol("CLMM");
                info.setUri("https://dynamic-ipfs.raydium.io/clmm/position?id=" + mintAddress);
                
                System.out.println("✅ 創建 CLMM Position 虛擬元數據");
                return info;
            } else {
                System.out.printf("⚠️ 帳戶由未知程序擁有: %s%n", owner);
                return null;
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 獲取 Token Extensions 失敗: %s%n", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析 Token Extensions 數據
     */
    private TokenExtensionsInfo parseTokenExtensions(byte[] data, String mintAddress) {
        try {
            // 基本的 Token Mint 結構解析
            if (data.length < 82) {
                System.out.println("⚠️ 數據長度不足，可能不是標準的 Token Mint");
                return null;
            }
            
            // 檢查是否有 Extensions
            if (data.length > 82) {
                System.out.println("✅ 發現 Token Extensions 數據");
                
                // 簡化的解析 - 在實際環境中需要完整的 Token Extensions 解析邏輯
                TokenExtensionsInfo info = new TokenExtensionsInfo();
                info.setMintAddress(mintAddress);
                info.setName("CLMM Position NFT");
                info.setSymbol("CLMM");
                info.setUri("https://dynamic-ipfs.raydium.io/clmm/position?id=" + mintAddress);
                
                System.out.printf("📋 Token Extensions 解析完成: %s%n", info.getName());
                return info;
            }
            
            System.out.println("⚠️ 未發現 Token Extensions");
            return null;
            
        } catch (Exception e) {
            System.err.printf("❌ 解析 Token Extensions 失敗: %s%n", e.getMessage());
            return null;
        }
    }
    
    /**
     * 查找 CLMM Position 帳戶
     */
    public PositionAccountInfo findCLMMPositionAccount(String positionNftMint) {
        if (!solanaConfig.isEnableBlockchainData()) {
            System.out.println("⚠️ 區塊鏈數據讀取已停用，使用模擬數據");
            return createMockPositionAccount();
        }
        
        try {
            System.out.printf("🔍 查找 CLMM Position 帳戶: %s%n", positionNftMint);
            
            // 首先嘗試直接讀取該地址的帳戶資訊
            JsonNode accountInfo = getAccountInfo(positionNftMint);
            
            if (accountInfo != null && !accountInfo.get("value").isNull()) {
                JsonNode value = accountInfo.get("value");
                String owner = value.get("owner").asText();
                
                // 如果這個地址本身就是 Raydium CLMM Position 帳戶
                if (isRaydiumClmmProgram(owner)) {
                    System.out.printf("✅ 目標地址本身就是 CLMM Position 帳戶 (程序: %s)%n", owner);
                    
                    String data = value.get("data").get(0).asText();
                    byte[] accountData = Base64.getDecoder().decode(data);
                    
                    // 直接解析這個帳戶的 tick 數據
                    PositionAccountInfo positionInfo = parsePositionAccountData(accountData, positionNftMint);
                    if (positionInfo != null) {
                        return positionInfo;
                    }
                }
            }
            
            // 如果直接讀取失敗，嘗試在程序帳戶中搜索
            System.out.println("🔍 在 Raydium CLMM 程序帳戶中搜索...");
            
            for (String programId : solanaConfig.getRaydiumClmmProgramIds()) {
                System.out.printf("🔍 搜索程序: %s%n", programId);
                
                PositionAccountInfo positionInfo = searchPositionInProgram(programId, positionNftMint);
                if (positionInfo != null) {
                    return positionInfo;
                }
            }
            
            System.out.println("❌ 未找到 CLMM Position 帳戶，使用模擬數據");
            return createMockPositionAccount();
            
        } catch (Exception e) {
            System.err.printf("❌ 查找 CLMM Position 帳戶失敗: %s%n", e.getMessage());
            return createMockPositionAccount();
        }
    }
    
    /**
     * 解析 Position 帳戶數據（針對直接的 CLMM Position 帳戶）
     */
    private PositionAccountInfo parsePositionAccountData(byte[] data, String positionAddress) {
        try {
            System.out.printf("🔍 解析 Position 帳戶數據 (長度: %d)%n", data.length);
            
            // CLMM Position 帳戶的典型結構
            if (data.length >= 264) {
                // 嘗試在固定偏移位置讀取 tick 數據
                // 基於 Raydium CLMM Position 帳戶結構
                int[] possibleOffsets = {
                    41,   // 常見的 tick 偏移位置
                    73,   // 另一個可能的位置
                    105,  // 第三個可能的位置
                    137   // 第四個可能的位置
                };
                
                for (int offset : possibleOffsets) {
                    if (offset + 8 <= data.length) {
                        int tickLower = bytesToInt32LE(data, offset);
                        int tickUpper = bytesToInt32LE(data, offset + 4);
                        
                        if (isValidTickPair(tickLower, tickUpper)) {
                            PositionAccountInfo info = new PositionAccountInfo();
                            info.setTickLower(tickLower);
                            info.setTickUpper(tickUpper);
                            info.setAccountAddress(offset); // 使用偏移作為標識
                            
                            System.out.printf("✅ 在偏移 %d 找到有效 tick 數據: %d 到 %d%n", 
                                offset, tickLower, tickUpper);
                            return info;
                        }
                    }
                }
                
                // 如果固定偏移失敗，進行全面掃描
                System.out.println("🔍 進行全面 tick 數據掃描...");
                return scanForTickData(data);
            } else {
                System.out.printf("⚠️ 帳戶數據長度不足: %d < 264%n", data.length);
                return null;
            }
            
        } catch (Exception e) {
            System.err.printf("❌ 解析 Position 帳戶數據失敗: %s%n", e.getMessage());
            return null;
        }
    }
    
    /**
     * 掃描數據中的 tick 資訊
     */
    private PositionAccountInfo scanForTickData(byte[] data) {
        for (int offset = 0; offset < data.length - 8; offset += 4) {
            int tickLower = bytesToInt32LE(data, offset);
            int tickUpper = bytesToInt32LE(data, offset + 4);
            
            if (isValidTickPair(tickLower, tickUpper)) {
                PositionAccountInfo info = new PositionAccountInfo();
                info.setTickLower(tickLower);
                info.setTickUpper(tickUpper);
                info.setAccountAddress(offset);
                
                System.out.printf("✅ 掃描找到 tick 數據 (偏移 %d): %d 到 %d%n", 
                    offset, tickLower, tickUpper);
                return info;
            }
        }
        
        System.out.println("❌ 掃描未找到有效的 tick 數據");
        return null;
    }
    
    /**
     * 在特定程序中搜索 Position 帳戶
     */
    private PositionAccountInfo searchPositionInProgram(String programId, String positionNftMint) {
        try {
            // 創建過濾器以減少返回的帳戶數量
            List<Map<String, Object>> filters = new ArrayList<>();
            
            // 過濾器：帳戶大小 (CLMM Position 帳戶通常是 264-281 字節)
            Map<String, Object> sizeFilter = new HashMap<>();
            sizeFilter.put("dataSize", 281);
            filters.add(sizeFilter);
            
            JsonNode programAccounts = getProgramAccounts(programId, filters);
            
            if (programAccounts == null || !programAccounts.isArray()) {
                return null;
            }
            
            System.out.printf("📊 找到 %d 個候選帳戶%n", programAccounts.size());
            
            // 檢查每個帳戶
            int maxAccountsToCheck = Math.min(20, programAccounts.size()); // 限制檢查數量
            for (int i = 0; i < maxAccountsToCheck; i++) {
                JsonNode account = programAccounts.get(i);
                String accountAddress = account.get("pubkey").asText();
                
                try {
                    PositionAccountInfo positionInfo = parsePositionAccount(account, positionNftMint);
                    if (positionInfo != null) {
                        System.out.printf("✅ 找到匹配的 Position 帳戶: %s%n", accountAddress);
                        return positionInfo;
                    }
                } catch (Exception e) {
                    // 繼續檢查下一個帳戶
                    continue;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.printf("⚠️ 搜索程序 %s 失敗: %s%n", programId, e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析 Position 帳戶數據
     */
    private PositionAccountInfo parsePositionAccount(JsonNode account, String targetNftMint) {
        try {
            String accountData = account.get("account").get("data").get(0).asText();
            byte[] data = Base64.getDecoder().decode(accountData);
            
            // 在數據中搜索 NFT mint
            byte[] targetMintBytes = base58Decode(targetNftMint);
            
            // 簡化的搜索邏輯
            for (int offset = 0; offset <= data.length - 32; offset += 4) {
                if (offset + 32 <= data.length) {
                    byte[] candidate = Arrays.copyOfRange(data, offset, offset + 32);
                    
                    if (Arrays.equals(candidate, targetMintBytes)) {
                        System.out.printf("✅ 在偏移 %d 找到匹配的 NFT mint%n", offset);
                        
                        // 嘗試解析 tick 數據
                        return extractTickData(data, offset);
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 從帳戶數據中提取 tick 資訊
     */
    private PositionAccountInfo extractTickData(byte[] data, int nftMintOffset) {
        try {
            // 在 NFT mint 附近搜索 tick 數據
            int searchStart = Math.max(0, nftMintOffset - 100);
            int searchEnd = Math.min(data.length - 8, nftMintOffset + 100);
            
            for (int tickOffset = searchStart; tickOffset < searchEnd; tickOffset += 4) {
                if (tickOffset + 8 <= data.length) {
                    int tickLower = bytesToInt32LE(data, tickOffset);
                    int tickUpper = bytesToInt32LE(data, tickOffset + 4);
                    
                    // 驗證 tick 值是否合理
                    if (isValidTickPair(tickLower, tickUpper)) {
                        PositionAccountInfo info = new PositionAccountInfo();
                        info.setTickLower(tickLower);
                        info.setTickUpper(tickUpper);
                        info.setAccountAddress(nftMintOffset); // 臨時使用偏移作為標識
                        
                        System.out.printf("✅ 提取到 tick 數據: %d 到 %d%n", tickLower, tickUpper);
                        return info;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 驗證 tick 值是否合理
     */
    private boolean isValidTickPair(int tickLower, int tickUpper) {
        final int MAX_TICK = 887272;
        
        return Math.abs(tickLower) <= MAX_TICK &&
               Math.abs(tickUpper) <= MAX_TICK &&
               tickLower < tickUpper &&
               (tickUpper - tickLower) > 0 &&
               (tickUpper - tickLower) < 100000; // 合理的範圍差值
    }
    
    /**
     * 創建模擬的 Position 帳戶數據（當區塊鏈數據停用時）
     */
    private PositionAccountInfo createMockPositionAccount() {
        PositionAccountInfo mockInfo = new PositionAccountInfo();
        mockInfo.setTickLower(-18973);
        mockInfo.setTickUpper(-12041);
        mockInfo.setAccountAddress(0); // 模擬地址
        
        System.out.println("⚠️ 使用模擬的 Position 帳戶數據");
        System.out.printf("   Tick 範圍: %d 到 %d%n", mockInfo.getTickLower(), mockInfo.getTickUpper());
        
        return mockInfo;
    }
    
    /**
     * 工具方法：將字節轉換為 32 位小端序整數
     */
    private int bytesToInt32LE(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
               ((data[offset + 1] & 0xFF) << 8) |
               ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 3] & 0xFF) << 24);
    }
    
    /**
     * 工具方法：Base58 解碼（簡化版）
     */
    private byte[] base58Decode(String input) {
        // 這是一個簡化的實現，實際應用中應該使用完整的 Base58 解碼庫
        // 目前返回固定長度的字節數組作為佔位符
        byte[] result = new byte[32];
        // 這裡應該實現實際的 Base58 解碼邏輯
        return result;
    }
    
    /**
     * 檢查 Solana 節點連接狀態
     */
    public boolean checkConnectionHealth() {
        try {
            System.out.println("🔍 檢查 Solana 節點連接狀態...");
            
            callSolanaRPC("getHealth");
            System.out.println("✅ Solana 節點連接正常");
            return true;
            
        } catch (Exception e) {
            System.err.printf("❌ Solana 節點連接失敗: %s%n", e.getMessage());
            return false;
        }
    }
    
    /**
     * 獲取節點版本資訊
     */
    public String getNodeVersion() {
        try {
            JsonNode version = callSolanaRPC("getVersion");
            return version.get("solana-core").asText();
        } catch (Exception e) {
            return "未知版本";
        }
    }
    
    // 內部類定義
    public static class TokenExtensionsInfo {
        private String mintAddress;
        private String name;
        private String symbol;
        private String uri;
        
        // Getters and Setters
        public String getMintAddress() { return mintAddress; }
        public void setMintAddress(String mintAddress) { this.mintAddress = mintAddress; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
    }
    
    public static class PositionAccountInfo {
        private int tickLower;
        private int tickUpper;
        private int accountAddress; // 簡化為 int，實際應該是 String
        
        // Getters and Setters
        public int getTickLower() { return tickLower; }
        public void setTickLower(int tickLower) { this.tickLower = tickLower; }
        public int getTickUpper() { return tickUpper; }
        public void setTickUpper(int tickUpper) { this.tickUpper = tickUpper; }
        public int getAccountAddress() { return accountAddress; }
        public void setAccountAddress(int accountAddress) { this.accountAddress = accountAddress; }
    }
}