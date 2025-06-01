# Java Solana LP Option

一個基於 Spring Boot 的應用程式，用於自動取得和儲存 Deribit SOL 選擇權的市場資料。

## 功能特色

### 🔄 自動排程任務
- **每 8 小時**：查詢所有 SOL 基礎貨幣的選擇權工具列表
- **每 1 小時**：定期更新工具列表
- **每 3 分鐘**：高頻率監控市場變化

### 📊 資料收集
- 自動篩選 `base_currency` 為 SOL 的選擇權工具
- 逐一查詢每個工具的詳細訂單簿資料
- 限制每秒一次 API 呼叫，避免觸及 API 限制

### 💾 資料儲存
- 將希臘字母（Delta, Gamma, Vega, Theta, Rho）儲存到 PostgreSQL
- 記錄價格資訊（指數價格、標的價格、標記價格）
- 儲存隱含波動率和未平倉合約資料
- 自動記錄時間戳記和格式化時間

### 📈 監控資料
收集的資料包括：
- **希臘字母**：Delta, Gamma, Vega, Theta, Rho
- **價格資訊**：Index Price, Underlying Price, Mark Price
- **市場資訊**：Open Interest, Mark IV, Bid IV, Ask IV
- **時間資訊**：原始時間戳記 + 格式化時間（yyyy/MM/dd HH:mm:ss）

## 技術架構

### 後端技術
- **Spring Boot 4.0.0-SNAPSHOT**
- **Java 17**
- **PostgreSQL** 資料庫
- **Spring Data JPA** ORM
- **Jackson** JSON 處理
- **Spring Scheduling** 任務排程

### API 來源
- **Deribit Public API**
  - 工具列表：`/api/v2/public/get_instruments`
  - 訂單簿：`/api/v2/public/get_order_book`

## 快速開始

### 環境需求
```bash
- Java 17+
- PostgreSQL 12+
- Maven 3.6+
```

### 1. 複製專案
```bash
git clone <repository-url>
cd java-solana-lp-option
```

### 2. 設定資料庫
```sql
-- 建立資料庫
CREATE DATABASE solana_lp_db;

-- 建立使用者（可選）
CREATE USER your_username WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE solana_lp_db TO your_username;
```

### 3. 設定應用程式
複製並修改設定檔：
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

編輯 `application.properties`：
```properties
# 資料庫配置
spring.datasource.url=jdbc:postgresql://localhost:5432/solana_lp_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 日誌配置
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### 4. 執行應用程式
```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或者先編譯後執行
./mvnw clean package
java -jar target/java-solana-lp-option-0.0.1-SNAPSHOT.jar
```

## 專案結構

```
src/main/java/com/example/java_solana_lp_option/
├── JavaSolanaLpOptionApplication.java          # 主應用程式
├── entity/
│   └── OptionData.java                         # 資料實體類別
├── repository/
│   └── OptionDataRepository.java               # 資料庫操作介面
└── runner/
    ├── DeribitInstrumentsRunner.java           # 工具列表查詢（排程任務）
    └── DeribitOrderBookRunner.java             # 訂單簿資料查詢

src/main/resources/
├── application.properties                      # 應用程式設定
└── application.properties.example             # 設定範例檔
```

## 資料表結構

### option_data 表
| 欄位名稱 | 類型 | 說明 |
|---------|------|------|
| id | BIGINT | 主鍵（自動遞增）|
| instrument_name | VARCHAR | 工具名稱 |
| state | VARCHAR | 狀態 |
| timestamp_value | BIGINT | 原始時間戳記 |
| formatted_time | VARCHAR | 格式化時間 |
| change_id | VARCHAR | 變更ID |
| delta_value | DOUBLE | Delta 希臘字母 |
| gamma_value | DOUBLE | Gamma 希臘字母 |
| vega_value | DOUBLE | Vega 希臘字母 |
| theta_value | DOUBLE | Theta 希臘字母 |
| rho_value | DOUBLE | Rho 希臘字母 |
| index_price | DOUBLE | 指數價格 |
| underlying_price | DOUBLE | 標的價格 |
| mark_price | DOUBLE | 標記價格 |
| open_interest | DOUBLE | 未平倉合約 |
| mark_iv | DOUBLE | 標記隱含波動率 |
| bid_iv | DOUBLE | 買方隱含波動率 |
| ask_iv | DOUBLE | 賣方隱含波動率 |
| created_at | TIMESTAMP | 建立時間 |

## 執行日誌範例

```
=== 應用程式啟動，DeribitInstrumentsRunner 已啟用 ===
📅 排程設定：
   🕐 每8小時執行：每天 0:00, 8:00, 16:00
   🕐 每1小時執行：每小時的整點
   🕐 每3分鐘執行：每小時的 0, 3, 6, 9... 分

=== 開始執行 Deribit 工具列表查詢 (啟動時執行) ===
正在呼叫 API: https://www.deribit.com/api/v2/public/get_instruments?currency=USDC&kind=option
成功取得 150 個選擇權工具，正在篩選 SOL 基礎貨幣的工具:
================================================================================
SOL 工具 1: SOL_USDC-27JUN25-170-C
  基礎貨幣: SOL
  類型: CALL 選擇權
  履約價: 170
  到期時間: 2025/06/27 08:00:00
--------------------------------------------------
...
總計: 45 個 SOL 基礎貨幣的 USDC 選擇權工具

🔄 開始逐一查詢每個 SOL 工具的訂單簿資料...

📊 處理第 1/45 個工具: SOL_USDC-27JUN25-170-C
=== 開始查詢 SOL_USDC-27JUN25-170-C 訂單簿資料 ===
================================================================================
📊 工具名稱: SOL_USDC-27JUN25-170-C
================================================================================
📈 狀態: open
🕐 時間: 2025/06/01 17:05:38
🔄 變更ID: 38951612955
--------------------------------------------------
🔢 希臘字母和價格資訊:
--------------------------------------------------
📊 Delta: 0.335750
📊 Gamma: 0.011900
📊 Vega: 0.148830
📊 Theta: -0.215210
📊 Rho: 0.032180

💰 指數價格 (Index Price): 153.068700
💰 標的價格 (Underlying Price): 153.068700
💰 標記價格 (Mark Price): 6.132600

📈 未平倉合約 (Open Interest): 3980.000000

📊 標記隱含波動率 (Mark IV): 75.060000
📊 買方隱含波動率 (Bid IV): 72.130000
📊 賣方隱含波動率 (Ask IV): 78.180000
--------------------------------------------------

💾 正在儲存資料到資料庫...
✅ 資料已成功儲存到資料庫！記錄ID: 1
📊 工具: SOL_USDC-27JUN25-170-C, 時間: 2025/06/01 17:05:38
=== SOL_USDC-27JUN25-170-C 訂單簿查詢完成 ===
⏱️  等待 1 秒後處理下一個工具...

📊 處理第 2/45 個工具: SOL_USDC-28JUN25-180-P
...
```

## 排程說明

### Cron 表達式格式
```
秒 分 時 日 月 週
```

### 排程設定
- **每8小時**：`0 0 */8 * * *` → 00:00, 08:00, 16:00
- **每1小時**：`0 0 * * * *` → 每小時整點
- **每3分鐘**：`0 */3 * * * *` → 每小時的 0, 3, 6, 9... 分

## API 限制處理

- 每秒最多一次 API 呼叫
- 自動錯誤重試機制
- 完整的異常處理和日誌記錄

## 開發指南

### 新增排程任務
在 `DeribitInstrumentsRunner` 中新增方法：
```java
@Scheduled(cron = "0 0 */6 * * *") // 每6小時
public void scheduledTask6Hours() {
    fetchInstruments("每6小時執行");
}
```

### 查詢資料庫
使用 `OptionDataRepository` 的內建方法：
```java
// 查詢特定工具
List<OptionData> data = repository.findByInstrumentName("SOL_USDC-27JUN25-170-C");

// 查詢時間範圍
List<OptionData> rangeData = repository.findByInstrumentNameAndCreatedAtBetween(
    "SOL_USDC-27JUN25-170-C", startTime, endTime);

// 查詢最新記錄
List<OptionData> latest = repository.findAllOrderByCreatedAtDesc();
```

## 故障排除

### 常見問題

1. **資料庫連線失敗**
   ```
   檢查 PostgreSQL 是否運行
   確認 application.properties 中的連線資訊
   驗證使用者權限
   ```

2. **API 呼叫失敗**
   ```
   檢查網路連線
   確認 Deribit API 可用性
   查看 API 限制是否超出
   ```

3. **排程未執行**
   ```
   確認主應用程式有 @EnableScheduling 註解
   檢查 cron 表達式語法
   查看應用程式日誌
   ```

### 日誌設定
在 `application.properties` 中調整日誌級別：
```properties
# 顯示 SQL 查詢
logging.level.org.hibernate.SQL=DEBUG

# 顯示參數綁定
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# 應用程式日誌
logging.level.com.example.java_solana_lp_option=DEBUG
```

## 貢獻指南

1. Fork 專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 授權

本專案採用 MIT 授權 - 詳見 [LICENSE](LICENSE) 檔案

## 聯絡方式

如有問題或建議，請提交 Issue 或聯絡專案維護者。

---

**注意**：請確保遵守 Deribit API 的使用條款和限制。本應用程式僅供教育和研究目的使用。