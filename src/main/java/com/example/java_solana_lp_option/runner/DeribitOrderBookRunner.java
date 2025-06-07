package com.example.java_solana_lp_option.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.java_solana_lp_option.entity.OptionData;
import com.example.java_solana_lp_option.repository.OptionDataRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.core.annotation.Order;

@Component
@Order(2) // 讓這個 Runner 在 DeribitInstrumentsRunner 之後執行
public class DeribitOrderBookRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter;
    private final OptionDataRepository optionDataRepository;

    public DeribitOrderBookRunner(OptionDataRepository optionDataRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        this.optionDataRepository = optionDataRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== DeribitOrderBookRunner 已啟用 ===");
        System.out.println("📊 等待 DeribitInstrumentsRunner 傳送工具名稱...");
        
        // 不再自動執行固定工具，改為等待其他 Runner 呼叫
    }
    
    /**
     * 公開方法供其他類別呼叫，查詢指定工具的訂單簿資料
     */
    public void fetchOrderBookData(String instrumentName) {
        // System.out.println("\n=== 開始查詢 " + instrumentName + " 訂單簿資料 ===");
        
        try {
            String apiUrl = "https://www.deribit.com/api/v2/public/get_order_book?depth=5&instrument_name=" + instrumentName;
            
            // System.out.println("正在呼叫 API: " + apiUrl);
            
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // 解析 JSON 回應
                JsonNode result = jsonNode.get("result");
                if (result != null) {
                    // 儲存資料到資料庫
                    saveToDatabase(result, instrumentName);
                } else {
                    System.out.println("API 回應中沒有找到訂單簿資料");
                }
                
            } else {
                System.err.println("API 呼叫失敗，狀態碼: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("執行過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        // System.out.println("=== " + instrumentName + " 訂單簿查詢完成 ===");
    }
    
    /**
     * 將時間戳記轉換為 yyyy/MM/dd HH:mm:ss 格式
     * @param timestampStr 毫秒時間戳記字串
     * @return 格式化的日期時間字串
     */
    private String formatTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            // 將毫秒時間戳記轉換為 Instant
            Instant instant = Instant.ofEpochMilli(timestamp);
            // 轉換為本地時間 (系統預設時區)
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            // 格式化為指定格式
            return localDateTime.format(dateFormatter);
        } catch (NumberFormatException e) {
            System.err.println("無法解析時間戳記: " + timestampStr);
            return timestampStr; // 如果轉換失敗，返回原始字串
        }
    }
    
    /**
     * 安全取得字串值
     */
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : "N/A";
    }
    
    /**
     * 安全取得數值
     */
    private double getDoubleValueAsDouble(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asDouble();
        }
        return 0.0;
    }
    
    /**
     * 儲存資料到資料庫
     */
    private void saveToDatabase(JsonNode result, String instrumentName) {
        try {
            System.out.println("💾 正在儲存資料到資料庫...");
            
            OptionData optionData = new OptionData();
            
            // 基本資訊
            optionData.setInstrumentName(instrumentName);
            optionData.setState(getStringValue(result, "state"));
            optionData.setTimestampValue(result.get("timestamp").asLong());
            optionData.setFormattedTime(formatTimestamp(String.valueOf(result.get("timestamp").asLong())));
            optionData.setChangeId(getStringValue(result, "change_id"));
            
            // 希臘字母
            JsonNode greeks = result.get("greeks");
            if (greeks != null) {
                optionData.setDeltaValue(getDoubleValueAsDouble(greeks, "delta"));
                optionData.setGammaValue(getDoubleValueAsDouble(greeks, "gamma"));
                optionData.setVegaValue(getDoubleValueAsDouble(greeks, "vega"));
                optionData.setThetaValue(getDoubleValueAsDouble(greeks, "theta"));
                optionData.setRhoValue(getDoubleValueAsDouble(greeks, "rho"));
            }
            
            // 價格資訊
            optionData.setIndexPrice(getDoubleValueAsDouble(result, "index_price"));
            optionData.setUnderlyingPrice(getDoubleValueAsDouble(result, "underlying_price"));
            optionData.setMarkPrice(getDoubleValueAsDouble(result, "mark_price"));
            optionData.setOpenInterest(getDoubleValueAsDouble(result, "open_interest"));
            
            // 隱含波動率
            optionData.setMarkIv(getDoubleValueAsDouble(result, "mark_iv"));
            optionData.setBidIv(getDoubleValueAsDouble(result, "bid_iv"));
            optionData.setAskIv(getDoubleValueAsDouble(result, "ask_iv"));
            
            // 儲存到資料庫
            OptionData savedData = optionDataRepository.save(optionData);
            
            System.out.printf("✅ 資料已成功儲存到資料庫！記錄ID: %d%n", savedData.getId());
            
            /* 註解掉詳細資訊顯示
            System.out.printf("📊 工具: %s, 時間: %s%n", savedData.getInstrumentName(), savedData.getFormattedTime());
            */
            
        } catch (Exception e) {
            System.err.println("❌ 儲存資料到資料庫時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
}