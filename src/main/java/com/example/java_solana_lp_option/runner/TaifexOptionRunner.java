package com.example.java_solana_lp_option.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.java_solana_lp_option.entity.TaifexOptionData;
import com.example.java_solana_lp_option.entity.TaifexOptionDelta;
import com.example.java_solana_lp_option.repository.TaifexOptionDataRepository;
import com.example.java_solana_lp_option.repository.TaifexOptionDeltaRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.converter.StringHttpMessageConverter;
import java.nio.charset.StandardCharsets;

@Component
@Order(3)
public class TaifexOptionRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter;
    private final TaifexOptionDataRepository taifexOptionDataRepository;
    private final TaifexOptionDeltaRepository taifexOptionDeltaRepository;

    public TaifexOptionRunner(TaifexOptionDataRepository taifexOptionDataRepository, 
                              TaifexOptionDeltaRepository taifexOptionDeltaRepository) {
        this.restTemplate = new RestTemplate();
        // 設定 RestTemplate 使用 UTF-8 編碼
        this.restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        this.taifexOptionDataRepository = taifexOptionDataRepository;
        this.taifexOptionDeltaRepository = taifexOptionDeltaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 應用程式啟動，TaifexOptionRunner 已啟用 ===");
        System.out.println("📅 排程設定：");
        System.out.println("   🕐 每日執行：每天 09:00 (台股開盤後)");
        System.out.println("   🕐 下午執行：每天 15:00 (台股收盤後)");
        System.out.println("   📊 同時收集：每日市場報告 + Delta值");
        
        fetchTaifexOptionData("啟動時執行");
        fetchTaifexOptionDelta("啟動時執行");
        
        // 組合資料功能已停用
        // System.out.println("\n🔄 開始組合市場資料和 Delta 資料...");
        // combinationService.combineMarketAndDeltaData();
    }
    
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void scheduledFetchMorning() {
        System.out.println("\n⏰ 早上定時任務觸發 - " + LocalDateTime.now().format(dateFormatter));
        fetchTaifexOptionData("每日09:00執行");
        fetchTaifexOptionDelta("每日09:00執行");
        
        // 組合資料功能已停用
        // System.out.println("\n🔄 開始組合市場資料和 Delta 資料...");
        // combinationService.combineMarketAndDeltaData();
    }
    
    @Scheduled(cron = "0 0 15 * * MON-FRI")
    public void scheduledFetchAfternoon() {
        System.out.println("\n⏰ 下午定時任務觸發 - " + LocalDateTime.now().format(dateFormatter));
        fetchTaifexOptionData("每日15:00執行");
        fetchTaifexOptionDelta("每日15:00執行");
        
        // 組合資料功能已停用
        // System.out.println("\n🔄 開始組合市場資料和 Delta 資料...");
        // combinationService.combineMarketAndDeltaData();
    }
    
    private void fetchTaifexOptionData(String trigger) {
        System.out.println("=== 開始執行 TAIFEX 選擇權資料查詢 (" + trigger + ") ===");
        
        try {
            String apiUrl = "https://openapi.taifex.com.tw/v1/DailyMarketReportOpt";
            
            System.out.println("正在呼叫 TAIFEX API: " + apiUrl);
            
            // 設定正確的 HTTP headers 來處理中文編碼
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json; charset=UTF-8");
            headers.set("Content-Type", "application/json; charset=UTF-8");
            
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, httpEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                if (jsonNode.isArray()) {
                    System.out.println("成功取得 " + jsonNode.size() + " 筆 TAIFEX 選擇權資料");
                    System.out.println("正在篩選 TXO 合約且排除無效資料 (收盤價≠-, 成交量≠0, 未平倉≠0):");
                    System.out.println("=".repeat(80));
                    
                    int totalCount = 0;
                    int displayCount = 0;
                    int savedCount = 0;
                    
                    for (JsonNode optionData : jsonNode) {
                        // 只處理 Contract 為 "TXO" 的資料
                        String contract = getStringValue(optionData, "Contract");
                        if (!"TXO".equals(contract)) {
                            continue;
                        }
                        
                        // 排除不符合條件的資料
                        String closePrice = getStringValue(optionData, "Close");
                        String volume = getStringValue(optionData, "Volume");
                        String openInterest = getStringValue(optionData, "OpenInterest");
                        
                        // 排除收盤價為 "-"、成交量為 "0"、未平倉為 "0" 的資料
                        if ("-".equals(closePrice) || "0".equals(volume) || "0".equals(openInterest)) {
                            continue;
                        }
                        
                        totalCount++;
                        
                        try {
                            // 儲存資料到資料庫
                            TaifexOptionData entity = createTaifexOptionEntity(optionData);
                            taifexOptionDataRepository.save(entity);
                            savedCount++;
                            
                            // 顯示前10筆資料詳情
                            if (displayCount < 10) {
                                displayOptionData(optionData, displayCount + 1);
                                displayCount++;
                            }
                            
                        } catch (Exception e) {
                            System.err.printf("❌ 處理第 %d 筆資料時發生錯誤: %s%n", totalCount, e.getMessage());
                        }
                    }
                    
                    if (totalCount > 10) {
                        System.out.println("... 以及其他 " + (totalCount - 10) + " 筆選擇權資料");
                    }
                    
                    System.out.println("✅ 總計處理: " + totalCount + " 筆有效 TXO 合約資料");
                    System.out.println("💾 成功儲存: " + savedCount + " 筆有效 TXO 合約資料");
                    
                    // 顯示統計資訊 (僅統計 TXO 合約)
                    displayTXOStatistics(jsonNode);
                    
                } else {
                    System.out.println("API 回應不是預期的陣列格式");
                }
                
            } else {
                System.err.println("API 呼叫失敗，狀態碼: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("執行過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== TAIFEX 選擇權資料查詢完成 (" + trigger + ") ===");
    }
    
    private void fetchTaifexOptionDelta(String trigger) {
        System.out.println("=== 開始執行 TAIFEX 選擇權 Delta 查詢 (" + trigger + ") ===");
        
        try {
            String apiUrl = "https://openapi.taifex.com.tw/v1/DailyOptionsDelta";
            
            System.out.println("正在呼叫 TAIFEX Delta API: " + apiUrl);
            
            // 設定正確的 HTTP headers 來處理中文編碼
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json; charset=UTF-8");
            headers.set("Content-Type", "application/json; charset=UTF-8");
            
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, httpEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                if (jsonNode.isArray()) {
                    System.out.println("成功取得 " + jsonNode.size() + " 筆 TAIFEX Delta 資料");
                    System.out.println("正在篩選 TXO 合約 Delta 值:");
                    System.out.println("=".repeat(80));
                    
                    int totalCount = 0;
                    int displayCount = 0;
                    int savedCount = 0;
                    
                    for (JsonNode deltaData : jsonNode) {
                        // 只處理 Contract 為 "TXO" 的資料
                        String contract = getStringValue(deltaData, "Contract");
                        if (!"TXO".equals(contract)) {
                            continue;
                        }
                        
                        // 排除無效的 Delta 值
                        String deltaValue = getStringValue(deltaData, "Delta");
                        if (deltaValue.isEmpty() || "-".equals(deltaValue)) {
                            continue;
                        }
                        
                        totalCount++;
                        
                        try {
                            // 儲存資料到資料庫
                            TaifexOptionDelta entity = createTaifexOptionDeltaEntity(deltaData);
                            taifexOptionDeltaRepository.save(entity);
                            savedCount++;
                            
                            // 顯示前10筆資料詳情
                            if (displayCount < 10) {
                                displayDeltaData(deltaData, displayCount + 1);
                                displayCount++;
                            }
                            
                        } catch (Exception e) {
                            System.err.printf("❌ 處理第 %d 筆 Delta 資料時發生錯誤: %s%n", totalCount, e.getMessage());
                        }
                    }
                    
                    if (totalCount > 10) {
                        System.out.println("... 以及其他 " + (totalCount - 10) + " 筆 TXO Delta 資料");
                    }
                    
                    System.out.println("✅ 總計處理: " + totalCount + " 筆有效 TXO Delta 資料");
                    System.out.println("💾 成功儲存: " + savedCount + " 筆有效 TXO Delta 資料");
                    
                    // 顯示 Delta 統計資訊
                    displayDeltaStatistics(jsonNode);
                    
                } else {
                    System.out.println("Delta API 回應不是預期的陣列格式");
                }
                
            } else {
                System.err.println("Delta API 呼叫失敗，狀態碼: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("執行 Delta 查詢過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== TAIFEX 選擇權 Delta 查詢完成 (" + trigger + ") ===");
    }
    
    /**
     * 建立 TAIFEX 選擇權實體
     */
    private TaifexOptionData createTaifexOptionEntity(JsonNode optionData) {
        TaifexOptionData entity = new TaifexOptionData();
        
        entity.setDate(getStringValue(optionData, "Date"));
        entity.setContract(getStringValue(optionData, "Contract"));
        entity.setContractMonthWeek(getStringValue(optionData, "ContractMonth(Week)"));
        entity.setStrikePrice(getStringValue(optionData, "StrikePrice"));
        entity.setCallPut(getStringValue(optionData, "CallPut"));
        entity.setOpenPrice(getStringValue(optionData, "Open"));
        entity.setHighPrice(getStringValue(optionData, "High"));
        entity.setLowPrice(getStringValue(optionData, "Low"));
        entity.setClosePrice(getStringValue(optionData, "Close"));
        entity.setVolume(getStringValue(optionData, "Volume"));
        entity.setSettlementPrice(getStringValue(optionData, "SettlementPrice"));
        entity.setOpenInterest(getStringValue(optionData, "OpenInterest"));
        entity.setBestBid(getStringValue(optionData, "BestBid"));
        entity.setBestAsk(getStringValue(optionData, "BestAsk"));
        entity.setHistoricalHigh(getStringValue(optionData, "HistoricalHigh"));
        entity.setHistoricalLow(getStringValue(optionData, "HistoricalLow"));
        entity.setTradingHalt(getStringValue(optionData, "TradingHalt"));
        entity.setTradingSession(getStringValue(optionData, "TradingSession"));
        
        return entity;
    }
    
    /**
     * 顯示選擇權資料詳情
     */
    private void displayOptionData(JsonNode optionData, int index) {
        String contract = getStringValue(optionData, "Contract");
        String contractMonth = getStringValue(optionData, "ContractMonth(Week)");
        String strikePrice = getStringValue(optionData, "StrikePrice");
        String callPut = getStringValue(optionData, "CallPut");
        String closePrice = getStringValue(optionData, "Close");
        String volume = getStringValue(optionData, "Volume");
        String openInterest = getStringValue(optionData, "OpenInterest");
        String date = getStringValue(optionData, "Date");
        
        // 轉換 Call/Put 顯示
        String callPutDisplay;
        if ("買權".equals(callPut)) {
            callPutDisplay = "Call(買權)";
        } else if ("賣權".equals(callPut)) {
            callPutDisplay = "Put(賣權)";
        } else {
            callPutDisplay = callPut;
        }
        
        System.out.printf("TAIFEX 選擇權 %d: %s %s %s %s%n", index, contract, contractMonth, strikePrice, callPutDisplay);
        System.out.printf("  交易日期: %s%n", date);
        System.out.printf("  收盤價: %s%n", closePrice);
        System.out.printf("  成交量: %s%n", volume);
        System.out.printf("  未平倉: %s%n", openInterest);
        System.out.println("-".repeat(50));
    }
    
    /**
     * 顯示有效 TXO 合約統計資訊
     */
    private void displayTXOStatistics(JsonNode dataArray) {
        System.out.println("\n📊 有效 TXO 合約統計資訊:");
        
        int callCount = 0;
        int putCount = 0;
        int totalVolume = 0;
        
        for (JsonNode data : dataArray) {
            // 只統計 TXO 合約
            String contract = getStringValue(data, "Contract");
            if (!"TXO".equals(contract)) {
                continue;
            }
            
            // 套用相同的篩選條件
            String closePrice = getStringValue(data, "Close");
            String volume = getStringValue(data, "Volume");
            String openInterest = getStringValue(data, "OpenInterest");
            
            if ("-".equals(closePrice) || "0".equals(volume) || "0".equals(openInterest)) {
                continue;
            }
            
            String callPut = getStringValue(data, "CallPut");
            if ("買權".equals(callPut)) {
                callCount++;
            } else if ("賣權".equals(callPut)) {
                putCount++;
            }
            
            try {
                String volumeStr = getStringValue(data, "Volume");
                if (!volumeStr.isEmpty() && !"-".equals(volumeStr)) {
                    totalVolume += Integer.parseInt(volumeStr);
                }
            } catch (NumberFormatException e) {
                // 忽略無法解析的交易量
            }
        }
        
        System.out.printf("📈 TXO Call(買權)合約數: %d%n", callCount);
        System.out.printf("📉 TXO Put(賣權)合約數: %d%n", putCount);
        System.out.printf("📊 TXO 總交易量: %,d%n", totalVolume);
        System.out.printf("⚖️  TXO Call/Put 比例: %.2f%n", putCount > 0 ? (double) callCount / putCount : 0.0);
    }
    
    /**
     * 建立 TAIFEX 選擇權 Delta 實體
     */
    private TaifexOptionDelta createTaifexOptionDeltaEntity(JsonNode deltaData) {
        TaifexOptionDelta entity = new TaifexOptionDelta();
        
        entity.setContract(getStringValue(deltaData, "Contract"));
        entity.setCallPut(getStringValue(deltaData, "CallPut"));
        entity.setContractMonthWeek(getStringValue(deltaData, "ContractMonth(Week)"));
        entity.setStrikePrice(getStringValue(deltaData, "StrikePrice"));
        entity.setDeltaValue(getStringValue(deltaData, "Delta"));
        
        return entity;
    }
    
    /**
     * 顯示 Delta 資料詳情
     */
    private void displayDeltaData(JsonNode deltaData, int index) {
        String contract = getStringValue(deltaData, "Contract");
        String contractMonth = getStringValue(deltaData, "ContractMonth(Week)");
        String strikePrice = getStringValue(deltaData, "StrikePrice");
        String callPut = getStringValue(deltaData, "CallPut");
        String deltaValue = getStringValue(deltaData, "Delta");
        
        // 轉換 Call/Put 顯示（修正 Delta 資料的顯示）
        String callPutDisplay;
        if ("買權".equals(callPut)) {
            callPutDisplay = "Call(買權)";
        } else if ("賣權".equals(callPut)) {
            callPutDisplay = "Put(賣權)";
        } else {
            // 根據 Delta 值判斷類型（Call 通常為正值，Put 通常為負值）
            try {
                double delta = Double.parseDouble(deltaValue);
                if (delta > 0) {
                    callPutDisplay = "Call(買權)";
                } else {
                    callPutDisplay = "Put(賣權)";
                }
            } catch (NumberFormatException e) {
                callPutDisplay = callPut;
            }
        }
        
        System.out.printf("TXO Delta %d: %s %s %s %s%n", index, contract, contractMonth, strikePrice, callPutDisplay);
        System.out.printf("  Delta值: %s%n", deltaValue);
        System.out.println("-".repeat(50));
    }
    
    /**
     * 顯示 Delta 統計資訊
     */
    private void displayDeltaStatistics(JsonNode dataArray) {
        System.out.println("\n📊 TXO Delta 統計資訊:");
        
        int callCount = 0;
        int putCount = 0;
        double totalCallDelta = 0.0;
        double totalPutDelta = 0.0;
        double maxCallDelta = Double.MIN_VALUE;
        double minPutDelta = Double.MAX_VALUE;
        
        for (JsonNode data : dataArray) {
            // 只統計 TXO 合約
            String contract = getStringValue(data, "Contract");
            if (!"TXO".equals(contract)) {
                continue;
            }
            
            // 排除無效的 Delta 值
            String deltaValue = getStringValue(data, "Delta");
            if (deltaValue.isEmpty() || "-".equals(deltaValue)) {
                continue;
            }
            
            try {
                double delta = Double.parseDouble(deltaValue);
                String callPut = getStringValue(data, "CallPut");
                
                if ("買權".equals(callPut)) {
                    callCount++;
                    totalCallDelta += delta;
                    maxCallDelta = Math.max(maxCallDelta, delta);
                } else if ("賣權".equals(callPut)) {
                    putCount++;
                    totalPutDelta += delta;
                    minPutDelta = Math.min(minPutDelta, delta);
                }
            } catch (NumberFormatException e) {
                // 忽略無法解析的 Delta 值
            }
        }
        
        System.out.printf("📈 TXO Call(買權) Delta統計:%n");
        System.out.printf("   合約數: %d%n", callCount);
        System.out.printf("   平均Delta: %.4f%n", callCount > 0 ? totalCallDelta / callCount : 0.0);
        System.out.printf("   最大Delta: %.4f%n", maxCallDelta != Double.MIN_VALUE ? maxCallDelta : 0.0);
        
        System.out.printf("📉 TXO Put(賣權) Delta統計:%n");
        System.out.printf("   合約數: %d%n", putCount);
        System.out.printf("   平均Delta: %.4f%n", putCount > 0 ? totalPutDelta / putCount : 0.0);
        System.out.printf("   最小Delta: %.4f%n", minPutDelta != Double.MAX_VALUE ? minPutDelta : 0.0);
        
        System.out.printf("⚖️  Delta 總合約數: %d%n", callCount + putCount);
    }
    
    /**
     * 安全取得字串值
     */
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : "";
    }
}