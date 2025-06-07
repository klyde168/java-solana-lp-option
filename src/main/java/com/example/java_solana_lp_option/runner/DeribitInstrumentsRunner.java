package com.example.java_solana_lp_option.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class DeribitInstrumentsRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter;
    
    @Autowired
    private ApplicationContext applicationContext;

    public DeribitInstrumentsRunner() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 應用程式啟動，DeribitInstrumentsRunner 已啟用 ===");
        // System.out.println("📅 排程設定：");
        // System.out.println("   🕐 每4小時執行：每天 0:00, 8:00, 16:00");
        // System.out.println("   🕐 每1小時執行：每小時的整點");
        // System.out.println("   🕐 每3分鐘執行：每小時的 0, 3, 6, 9... 分");
        
        fetchInstruments("啟動時執行");
    }
    
    @Scheduled(cron = "0 0 */4 * * *")
    public void scheduledFetchInstruments8Hours() {
        System.out.println("\n⏰ 4小時定時任務觸發 - " + LocalDateTime.now().format(dateFormatter));
        fetchInstruments("每4小時執行");
    }
    
    // @Scheduled(cron = "0 0 * * * *")
    // public void scheduledFetchInstruments1Hour() {
    //     System.out.println("\n⏰ 1小時定時任務觸發 - " + LocalDateTime.now().format(dateFormatter));
    //     fetchInstruments("每1小時執行");
    // }
    
    // @Scheduled(cron = "0 */3 * * * *")
    // public void scheduledFetchInstruments3Minutes() {
    //     System.out.println("\n⏰ 3分鐘定時任務觸發 - " + LocalDateTime.now().format(dateFormatter));
    //     fetchInstruments("每3分鐘執行");
    // }
    
    private void fetchInstruments(String trigger) {
        System.out.println("=== 開始執行 Deribit 工具列表查詢 (" + trigger + ") ===");
        
        // 查詢 USDC 選擇權
        //fetchInstrumentsByCurrency("USDC", trigger);
        
        // 查詢 ETH 選擇權
        fetchInstrumentsByCurrency("ETH", trigger);
        
        System.out.println("=== Deribit 工具列表查詢完成 (" + trigger + ") ===");
    }
    
    private void fetchInstrumentsByCurrency(String currency, String trigger) {
        try {
            String apiUrl = "https://www.deribit.com/api/v2/public/get_instruments?currency=" + currency + "&kind=option";
            
            System.out.println("正在呼叫 " + currency + " API: " + apiUrl);
            
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                JsonNode result = jsonNode.get("result");
                if (result != null && result.isArray()) {
                    System.out.println("成功取得 " + result.size() + " 個 " + currency + " 選擇權工具，正在篩選到期日>7天的工具:");
                    System.out.println("=".repeat(80));
                    
                    int totalCount = 0;
                    int displayCount = 0;
                    List<String> solInstruments = new ArrayList<>();
                    
                    for (JsonNode instrument : result) {
                        JsonNode baseCurrencyNode = instrument.get("base_currency");
                        String expirationTimestamp = instrument.get("expiration_timestamp").asText();
                        long daysUntilExpiration = calculateDaysUntilExpiration(expirationTimestamp);
                        
                        // 只處理到期日大於7天的選擇權
                        if (daysUntilExpiration > 7) {
                            totalCount++;
                            String instrumentName = instrument.get("instrument_name").asText();
                            solInstruments.add(instrumentName);
                            
                            if (displayCount < 10) {
                                String strike = instrument.get("strike").asText();
                                String optionType = instrument.get("option_type").asText();
                                String baseCurrency = baseCurrencyNode != null ? baseCurrencyNode.asText() : "N/A";
                                
                                String formattedDate = formatTimestamp(expirationTimestamp);
                                
                                System.out.printf("工具 %d: %s%n", displayCount + 1, instrumentName);
                                System.out.printf("  基礎貨幣: %s%n", baseCurrency);
                                System.out.printf("  類型: %s 選擇權%n", optionType.toUpperCase());
                                System.out.printf("  履約價: %s%n", strike);
                                System.out.printf("  到期時間: %s (%d 天後)%n", formattedDate, daysUntilExpiration);
                                System.out.println("-".repeat(50));
                                
                                displayCount++;
                            }
                        }
                    }
                    
                    if (totalCount > 10) {
                        System.out.println("... 以及其他 " + (totalCount - 10) + " 個選擇權工具");
                    }
                    
                    if (totalCount > 0) {
                        System.out.println("總計: " + totalCount + " 個到期日>7天的 " + currency + " 選擇權工具");
                        
                        // 呼叫 OrderBookRunner 處理每個工具，每秒一次
                        System.out.println("\n🔄 開始逐一查詢每個工具的訂單簿資料...");
                        processInstrumentsWithDelay(solInstruments);
                    } else {
                        System.out.println("⚠️  沒有找到到期日>7天的 " + currency + " 選擇權工具");
                    }
                } else {
                    System.out.println(currency + " API 回應中沒有找到工具資料");
                }
                
            } else {
                System.err.println(currency + " API 呼叫失敗，狀態碼: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("執行 " + currency + " 查詢過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 逐一處理工具列表，每0.5秒傳送一個給 OrderBookRunner
     */
    private void processInstrumentsWithDelay(List<String> instruments) {
        try {
            // 檢查 ApplicationContext 是否仍然活躍
            if (applicationContext instanceof ConfigurableApplicationContext && 
                !((ConfigurableApplicationContext) applicationContext).isActive()) {
                System.out.println("⚠️  ApplicationContext 已關閉，停止處理工具列表");
                return;
            }
            
            // 使用 ApplicationContext 來獲取 OrderBookRunner，避免循環依賴
            DeribitOrderBookRunner orderBookRunner = applicationContext.getBean(DeribitOrderBookRunner.class);
            
            for (int i = 0; i < instruments.size(); i++) {
                String instrumentName = instruments.get(i);
                
                try {
                    // 在每次迭代中檢查 ApplicationContext 是否仍然活躍
                    if (applicationContext instanceof ConfigurableApplicationContext && 
                        !((ConfigurableApplicationContext) applicationContext).isActive()) {
                        System.out.println("⚠️  ApplicationContext 已關閉，中止剩餘工具處理");
                        break;
                    }
                    
                    System.out.printf("\n📊 處理第 %d/%d 個工具: %s%n", 
                        i + 1, instruments.size(), instrumentName);
                    
                    // 呼叫 OrderBookRunner 的方法
                    orderBookRunner.fetchOrderBookData(instrumentName);
                    
                    // 如果不是最後一個，等待0.5秒
                    if (i < instruments.size() - 1) {
                        System.out.println("⏱️  等待 0.5 秒後處理下一個工具...");
                        Thread.sleep(500); // 等待0.5秒
                    }
                    
                } catch (InterruptedException e) {
                    System.err.println("❌ 延遲中斷: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.printf("❌ 處理工具 %s 時發生錯誤: %s%n", instrumentName, e.getMessage());
                    // 繼續處理下一個工具
                }
            }
            
            System.out.printf("\n✅ 完成處理 %d 個工具的訂單簿查詢%n", instruments.size());
            
        } catch (Exception e) {
            System.err.println("❌ 無法獲取 DeribitOrderBookRunner Bean: " + e.getMessage());
        }
    }
    
    private String formatTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            Instant instant = Instant.ofEpochMilli(timestamp);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return localDateTime.format(dateFormatter);
        } catch (NumberFormatException e) {
            System.err.println("無法解析時間戳記: " + timestampStr);
            return timestampStr;
        }
    }
    
    private long calculateDaysUntilExpiration(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            Instant expirationInstant = Instant.ofEpochMilli(timestamp);
            Instant now = Instant.now();
            
            // 計算兩個時間點之間的天數差異
            long days = ChronoUnit.DAYS.between(now, expirationInstant);
            return days;
        } catch (NumberFormatException e) {
            System.err.println("無法解析到期時間戳記: " + timestampStr);
            return 0;
        }
    }
}