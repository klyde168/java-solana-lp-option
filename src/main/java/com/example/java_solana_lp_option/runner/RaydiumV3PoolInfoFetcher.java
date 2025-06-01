package com.example.java_solana_lp_option.runner;

import com.example.java_solana_lp_option.entity.RaydiumV3PoolData; // 新增導入
import com.example.java_solana_lp_option.repository.RaydiumV3PoolDataRepository; // 新增導入
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired; // 確保 Autowired 被導入
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RaydiumV3PoolInfoFetcher {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RaydiumV3PoolDataRepository raydiumV3PoolDataRepository; // 新增 Repository
    private static final String API_URL = "https://api-v3.raydium.io/pools/info/ids?ids=8sLbNZoA1cfnvMJLPfp98ZLAnFSYCFApfJKMbiXNLwxj";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int API_TIMEOUT_MS = 15000; // 15 秒超時

    @Autowired // 添加 Autowired 以進行依賴注入
    public RaydiumV3PoolInfoFetcher(RaydiumV3PoolDataRepository raydiumV3PoolDataRepository) {
        this.objectMapper = new ObjectMapper();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(API_TIMEOUT_MS);
        factory.setReadTimeout(API_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
        this.raydiumV3PoolDataRepository = raydiumV3PoolDataRepository; // 初始化 Repository
        // 應用程式啟動時執行一次獲取和顯示，以及儲存
        fetchAndProcessPoolInfo();
    }

    @Scheduled(cron = "0 0 */8 * * *") // 每8小時執行一次 (例如 00:00, 08:00, 16:00)
    public void scheduledFetchAndProcessPoolInfo() {
        fetchAndProcessPoolInfo();
    }

    public void fetchAndProcessPoolInfo() {
        System.out.println("================================================================================");
        System.out.printf("🚀 [%s] 開始執行 Raydium V3 Pool 資訊獲取與儲存任務...%n", LocalDateTime.now().format(formatter));
        System.out.println("API URL: " + API_URL);
        System.out.println("================================================================================");

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(API_URL, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                if (rootNode != null && rootNode.has("success") && rootNode.get("success").asBoolean() && rootNode.has("data")) {
                    JsonNode poolsNode = rootNode.get("data");

                    if (poolsNode.isArray() && !poolsNode.isEmpty()) {
                        JsonNode poolInfoNode = poolsNode.get(0); // API URL中只有一個ID，所以取第一個
                        displayPoolDetails(poolInfoNode); // 保留顯示邏輯
                        savePoolData(poolInfoNode); // 新增儲存邏輯
                    } else {
                        System.err.println("❌ API 回應中的 'data' 陣列為空或無效。");
                    }
                } else {
                    System.err.println("❌ API 回應格式不符預期，或請求未成功，或缺少 'data' 欄位。");
                    System.err.println("初步回應內容: " + (response.getBody().length() > 300 ? response.getBody().substring(0, 300) + "..." : response.getBody()));
                }
            } else {
                System.err.printf("❌ API 請求失敗，狀態碼: %s%n", response.getStatusCode());
                if (response.getBody() != null) {
                    System.err.println("錯誤回應內容: " + response.getBody());
                }
            }
        } catch (Exception e) {
            System.err.printf("❌ 執行過程中發生錯誤: %s%n", e.getMessage());
            e.printStackTrace();
        }
        System.out.println("================================================================================");
        System.out.printf("✅ [%s] Raydium V3 Pool 資訊獲取與儲存任務執行完畢。%n", LocalDateTime.now().format(formatter));
        System.out.println("================================================================================");
    }

    private void savePoolData(JsonNode poolInfoNode) {
        try {
            RaydiumV3PoolData poolData = new RaydiumV3PoolData();

            String poolId = getPathAsString(poolInfoNode, "id", null);
            if (poolId == null || poolId.equals("N/A")) {
                System.err.println("❌ 池子 ID 無效，無法儲存資料。");
                return;
            }
            poolData.setPoolId(poolId);

            // 雖然 API 回應的 mintA.symbol 和 mintB.symbol 可能為空或 "N/A"
            // 但我們的 RaydiumV3PoolData 實體定義了這些欄位，所以還是嘗試獲取
            // 如果您的業務邏輯確定這些欄位總是"N/A"，可以考慮直接設為 null 或固定值
            poolData.setMintASymbol(getPathAsString(poolInfoNode, "mintA.symbol", null));
            poolData.setMintBSymbol(getPathAsString(poolInfoNode, "mintB.symbol", null));

            poolData.setPrice(getPathAsDouble(poolInfoNode, "price", null));
            poolData.setMintAmountA(getPathAsDouble(poolInfoNode, "mintAmountA", null));
            poolData.setMintAmountB(getPathAsDouble(poolInfoNode, "mintAmountB", null));
            poolData.setFeeRate(getPathAsDouble(poolInfoNode, "feeRate", null)); // 儲存原始費率
            poolData.setTvl(getPathAsDouble(poolInfoNode, "tvl", null));
            poolData.setDayVolume(getPathAsDouble(poolInfoNode, "day.volume", null));
            poolData.setDayVolumeFee(getPathAsDouble(poolInfoNode, "day.volumeFee", null));
            poolData.setDayApr(getPathAsDouble(poolInfoNode, "day.apr", null)); // 儲存原始 APR
            poolData.setDayFeeApr(getPathAsDouble(poolInfoNode, "day.feeApr", null)); // 儲存原始 Fee APR

            poolData.setFetchedAt(LocalDateTime.now()); // 設定獲取時間

            raydiumV3PoolDataRepository.save(poolData);
            System.out.printf("💾 池子資料已成功儲存到資料庫 (ID: %s)。%n", poolId);

        } catch (Exception e) {
            System.err.printf("❌ 儲存池子資料到資料庫時發生錯誤: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayPoolDetails(JsonNode poolInfo) {
        if (poolInfo == null || poolInfo.isNull()) {
            System.err.println("❌ 池子資訊為空，無法顯示。");
            return;
        }

        System.out.println("\n💧 池子詳細資訊 (Pool Details):");
        System.out.println("--------------------------------------------------------------------------------");

        String mintASymbol = getPathAsString(poolInfo, "mintA.symbol", "N/A");
        String mintBSymbol = getPathAsString(poolInfo, "mintB.symbol", "N/A");

        String price = formatPrice(getPathAsDouble(poolInfo, "price", null));
        String mintAmountA = formatNumber(getPathAsDouble(poolInfo, "mintAmountA", null), 6);
        String mintAmountB = formatNumber(getPathAsDouble(poolInfo, "mintAmountB", null), 6);
        
        Double tempFeeRate = getPathAsDouble(poolInfo, "feeRate", null);
        String feeRateStr;
        if (tempFeeRate != null) {
            feeRateStr = formatAsPercentageNumber(tempFeeRate * 100, 4) + "%";
        } else {
            feeRateStr = "N/A";
        }

        String tvl = formatCurrency(getPathAsDouble(poolInfo, "tvl", null));

        String dayVolume = formatCurrency(getPathAsDouble(poolInfo, "day.volume", null));
        String dayVolumeFee = formatCurrency(getPathAsDouble(poolInfo, "day.volumeFee", null));

        Double tempDayApr = getPathAsDouble(poolInfo, "day.apr", null);
        String dayAprStr;
        if (tempDayApr != null) {
            dayAprStr = formatAsPercentageNumber(tempDayApr, 2) + "%";
        } else {
            dayAprStr = "N/A";
        }

        Double tempDayFeeApr = getPathAsDouble(poolInfo, "day.feeApr", null);
        String dayFeeAprStr;
        if (tempDayFeeApr != null) {
            dayFeeAprStr = formatAsPercentageNumber(tempDayFeeApr, 2) + "%";
        } else {
            dayFeeAprStr = "N/A";
        }
        
        String poolId = getPathAsString(poolInfo, "id", "N/A");
        System.out.printf("🆔 池子 ID (Pool ID): %s%n", poolId);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("🔄 代幣對 (Token Pair): %s / %s%n", mintASymbol, mintBSymbol);
        System.out.printf("💲 價格 (Price %s/%s): %s %s%n", mintBSymbol, mintASymbol, price, mintBSymbol.equals("N/A") ? "" : mintBSymbol);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("💰 %s 數量 (Mint Amount %s): %s%n", mintASymbol.equals("N/A")? "代幣A" : mintASymbol, mintASymbol.equals("N/A")? "代幣A" : mintASymbol, mintAmountA);
        System.out.printf("💰 %s 數量 (Mint Amount %s): %s%n", mintBSymbol.equals("N/A")? "代幣B" : mintBSymbol, mintBSymbol.equals("N/A")? "代幣B" : mintBSymbol, mintAmountB);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("💸 交易費率 (Fee Rate): %s%n", feeRateStr);
        System.out.printf("🔒 總鎖倉價值 (TVL): %s%n", tvl);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("📅 每日數據 (Day Stats):");
        System.out.printf("  📈 交易量 (Volume): %s%n", dayVolume);
        System.out.printf("  💵 交易費收入 (Volume Fee): %s%n", dayVolumeFee);
        System.out.printf("  📈 年化收益率 (APR): %s%n", dayAprStr);
        System.out.printf("  💸 手續費年化收益率 (Fee APR): %s%n", dayFeeAprStr);
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    private String getPathAsString(JsonNode node, String path, String defaultValue) {
        JsonNode targetNode = node;
        for (String key : path.split("\\.")) {
            if (targetNode == null || targetNode.isNull() || !targetNode.has(key) || targetNode.get(key).isNull()) {
                return defaultValue;
            }
            targetNode = targetNode.get(key);
        }
        // 如果 targetNode 是物件且為空 (例如 {}), 或者不是文字，則回傳 defaultValue 或 toString()
        if (targetNode.isObject() && targetNode.isEmpty()) {
            return defaultValue;
        }
        return targetNode.isTextual() ? targetNode.asText() : targetNode.toString();
    }

    private Double getPathAsDouble(JsonNode node, String path, Double defaultValue) {
        JsonNode targetNode = node;
        for (String key : path.split("\\.")) {
            if (targetNode == null || targetNode.isNull() || !targetNode.has(key) || targetNode.get(key).isNull()) {
                return defaultValue;
            }
            targetNode = targetNode.get(key);
        }
        return targetNode.isNumber() ? targetNode.asDouble() : defaultValue;
    }

    private String formatNumber(Double value, int precision) {
        if (value == null) return "N/A";
        try {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(precision);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);
            return df.format(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
    
    private String formatPrice(Double value) {
        if (value == null) return "N/A";
        DecimalFormat df;
        if (value >= 1) {
            df = new DecimalFormat("#,##0.00####");
        } else if (value > 0.000001) {
            df = new DecimalFormat("0.000000##");
        } else if (value == 0) {
            return "0";
        } else {
            df = new DecimalFormat("0.######E0");
        }
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(value);
    }

    private String formatAsPercentageNumber(Double value, int precision) {
        if (value == null) return "N/A";
        try {
            BigDecimal bd = BigDecimal.valueOf(value);
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(precision);
            df.setMinimumFractionDigits(precision); 
            df.setGroupingUsed(false);
            return df.format(bd);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String formatCurrency(Double value) {
        if (value == null) return "N/A";
        try {
            DecimalFormat df = new DecimalFormat("$#,##0.00");
            return df.format(value);
        } catch (Exception e) {
            return "$" + formatNumber(value, 2);
        }
    }
}