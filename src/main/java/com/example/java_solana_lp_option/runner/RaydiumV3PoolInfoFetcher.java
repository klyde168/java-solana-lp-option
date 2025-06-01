package com.example.java_solana_lp_option.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String API_URL = "https://api-v3.raydium.io/pools/info/ids?ids=8sLbNZoA1cfnvMJLPfp98ZLAnFSYCFApfJKMbiXNLwxj";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int API_TIMEOUT_MS = 15000; // 15 秒超時

    public RaydiumV3PoolInfoFetcher() {
        this.objectMapper = new ObjectMapper();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(API_TIMEOUT_MS);
        factory.setReadTimeout(API_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
        fetchAndDisplayPoolInfo();
    }

    @Scheduled(cron = "0 0 */8 * * *") // 每8小時執行一次 (例如 00:00, 08:00, 16:00)
    // @Scheduled(fixedRate = 1000 * 60 * 1) // 測試用：每1分鐘執行一次
    public void fetchAndDisplayPoolInfo() {
        System.out.println("================================================================================");
        System.out.printf("🚀 [%s] 開始執行 Raydium V3 Pool 資訊獲取任務...%n", LocalDateTime.now().format(formatter));
        System.out.println("API URL: " + API_URL);
        System.out.println("================================================================================");

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(API_URL, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                if (rootNode != null && rootNode.has("success") && rootNode.get("success").asBoolean() && rootNode.has("data")) {
                    JsonNode poolsNode = rootNode.get("data");

                    if (poolsNode.isArray() && !poolsNode.isEmpty()) {
                        // 我們只處理第一個池子的資訊，因為API URL中只有一個ID
                        JsonNode poolInfo = poolsNode.get(0);
                        displayPoolDetails(poolInfo);
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
        System.out.printf("✅ [%s] Raydium V3 Pool 資訊獲取任務執行完畢。%n", LocalDateTime.now().format(formatter));
        System.out.println("================================================================================");
    }

    private void displayPoolDetails(JsonNode poolInfo) {
        if (poolInfo == null || poolInfo.isNull()) {
            System.err.println("❌ 池子資訊為空，無法顯示。");
            return;
        }

        System.out.println("\n💧 池子詳細資訊 (Pool Details):");
        System.out.println("--------------------------------------------------------------------------------");

        // mintA 和 mintB 中的 symbol 在新 JSON 中是空的
        String mintASymbol = getPathAsString(poolInfo, "mintA.symbol", "N/A");
        String mintBSymbol = getPathAsString(poolInfo, "mintB.symbol", "N/A");

        String price = formatPrice(getPathAsDouble(poolInfo, "price", null));
        String mintAmountA = formatNumber(getPathAsDouble(poolInfo, "mintAmountA", null), 6);
        String mintAmountB = formatNumber(getPathAsDouble(poolInfo, "mintAmountB", null), 6);
        
        Double tempFeeRate = getPathAsDouble(poolInfo, "feeRate", null);
        String feeRateStr;
        if (tempFeeRate != null) {
            // feeRate from API (e.g., 0.0001) needs to be multiplied by 100 for percentage display (0.01%)
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
            // day.apr from API (e.g., 26.53) is assumed to be the direct percentage value
            dayAprStr = formatAsPercentageNumber(tempDayApr, 2) + "%";
        } else {
            dayAprStr = "N/A";
        }

        Double tempDayFeeApr = getPathAsDouble(poolInfo, "day.feeApr", null);
        String dayFeeAprStr;
        if (tempDayFeeApr != null) {
            // day.feeApr from API (e.g., 24.72) is assumed to be the direct percentage value
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
        return targetNode.isTextual() ? targetNode.asText() : (targetNode.isObject() && targetNode.isEmpty() ? defaultValue : targetNode.toString());
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

    // Formats a number as a percentage string, assumes 'value' is the number to be displayed before '%'
    private String formatAsPercentageNumber(Double value, int precision) {
        if (value == null) return "N/A";
        try {
            BigDecimal bd = BigDecimal.valueOf(value);
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(precision);
            df.setMinimumFractionDigits(precision); // Ensure fixed precision
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
