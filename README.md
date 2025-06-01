# Java Solana LP Option

一個基於 Spring Boot 的應用程式，整合了 Deribit 選擇權資料收集、Raydium 流動性池監控和 CLMM Position 分析功能。

## 🌟 功能特色

### 🔄 Deribit 選擇權資料收集
- **自動排程任務**：每 8 小時查詢 SOL 基礎貨幣的選擇權工具列表
- **高頻率監控**：每 3 分鐘更新市場資料
- **希臘字母收集**：自動儲存 Delta, Gamma, Vega, Theta, Rho
- **價格與 IV 監控**：記錄指數價格、標的價格、隱含波動率

### 🏊 Raydium 流動性池監控
- **池資訊追蹤**：自動獲取和儲存 V3 池的詳細資訊
- **TVL 與交易量**：監控總鎖倉價值和 24 小時交易量
- **APR 計算**：追蹤手續費年化收益率和總 APR
- **定期更新**：每 8 小時自動同步最新數據

### 📊 CLMM Position 分析器 (NEW!)
- **實時 Position 分析**：分析 Raydium CLMM Position NFT
- **區塊鏈數據讀取**：直接從 Solana 節點獲取 tick 範圍
- **收益追蹤**：監控未領取手續費和獎勵
- **價格範圍分析**：計算當前價格在 Position 範圍內的位置
- **批次處理**：支援同時分析多個 Position

### 💾 資料儲存與管理
- **PostgreSQL 資料庫**：儲存所有歷史資料
- **JPA 實體映射**：結構化的資料模型
- **時間序列資料**：支援歷史趨勢分析
- **索引優化**：高效的資料查詢

## 🏗️ 技術架構

### 後端技術棧
- **Spring Boot 4.0.0-SNAPSHOT**
- **Java 17**
- **PostgreSQL** 資料庫
- **Spring Data JPA** ORM
- **Jackson** JSON 處理
- **Spring Scheduling** 任務排程

### 區塊鏈整合
- **Solana RPC 客戶端**：直接與 Solana 節點通信
- **Token Extensions 解析**：支援 Token Extensions Program
- **Raydium CLMM 協議**：深度整合 CLMM Position 分析

### API 數據來源
- **Deribit Public API**：選擇權市場資料
- **Raydium V3 API**：流動性池資訊
- **Solana RPC**：區塊鏈原生資料

## 🚀 快速開始

### 環境需求
```bash
- Java 17+
- PostgreSQL 12+
- Maven 3.6+
- Solana RPC 存取 (可選)
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

# Solana 節點配置
solana.rpcUrl=https://api.mainnet-beta.solana.com
solana.enableBlockchainData=true
solana.network=mainnet-beta
solana.connectTimeout=30000
solana.readTimeout=60000
solana.maxRetries=5

# 日誌配置
logging.level.org.hibernate.SQL=DEBUG
logging.level.com.example.java_solana_lp_option=INFO
```

### 4. 環境變數配置（可選）
```bash
# CLMM Position 分析
export CLMM_POSITION_ID=68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o
export CLMM_BATCH_POSITION_IDS=68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o,9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM
```

### 5. 執行應用程式
```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或者先編譯後執行
./mvnw clean package
java -jar target/java-solana-lp-option-0.0.1-SNAPSHOT.jar
```

## 📁 專案結構

```
src/main/java/com/example/java_solana_lp_option/
├── JavaSolanaLpOptionApplication.java          # 主應用程式
├── entity/
│   ├── OptionData.java                         # 選擇權資料實體
│   └── RaydiumV3PoolData.java                  # Raydium V3 池資料實體
├── repository/
│   ├── OptionDataRepository.java               # 選擇權資料庫操作
│   └── RaydiumV3PoolDataRepository.java        # 池資料庫操作
├── runner/
│   ├── DeribitInstrumentsRunner.java           # Deribit 工具列表查詢
│   ├── DeribitOrderBookRunner.java             # 訂單簿資料查詢
│   ├── RaydiumV3PoolInfoFetcher.java           # Raydium 池資訊獲取
│   └── CLMMPositionRunner.java                 # CLMM Position 分析器
├── analyzer/
│   └── CLMMPositionAnalyzer.java               # CLMM Position 核心分析邏輯
├── service/
│   └── SolanaService.java                      # Solana 區塊鏈服務
└── config/
    └── SolanaConfig.java                       # Solana 配置管理

src/main/resources/
├── application.properties                      # 應用程式設定
└── application.properties.example             # 設定範例檔
```

## 📊 資料表結構

### option_data 表（Deribit 選擇權資料）
| 欄位名稱 | 類型 | 說明 |
|---------|------|------|
| id | BIGINT | 主鍵（自動遞增）|
| instrument_name | VARCHAR | 工具名稱 |
| state | VARCHAR | 狀態 |
| timestamp_value | BIGINT | 原始時間戳記 |
| formatted_time | VARCHAR | 格式化時間 |
| delta_value | DOUBLE | Delta 希臘字母 |
| gamma_value | DOUBLE | Gamma 希臘字母 |
| vega_value | DOUBLE | Vega 希臘字母 |
| theta_value | DOUBLE | Theta 希臘字母 |
| rho_value | DOUBLE | Rho 希臘字母 |
| index_price | DOUBLE | 指數價格 |
| mark_price | DOUBLE | 標記價格 |
| open_interest | DOUBLE | 未平倉合約 |
| mark_iv | DOUBLE | 標記隱含波動率 |
| created_at | TIMESTAMP | 建立時間 |

### raydium_v3_pool_data 表（Raydium 池資料）
| 欄位名稱 | 類型 | 說明 |
|---------|------|------|
| id | BIGINT | 主鍵（自動遞增）|
| pool_id | VARCHAR | 池 ID |
| mint_a_symbol | VARCHAR | 代幣 A 符號 |
| mint_b_symbol | VARCHAR | 代幣 B 符號 |
| price | DOUBLE | 當前價格 |
| tvl | DOUBLE | 總鎖倉價值 |
| fee_rate | DOUBLE | 手續費率 |
| day_volume | DOUBLE | 24H 交易量 |
| day_apr | DOUBLE | 24H APR |
| day_fee_apr | DOUBLE | 24H 手續費 APR |
| fetched_at | TIMESTAMP | 獲取時間 |

## 🎯 功能使用指南

### Deribit 選擇權分析
```java
// 系統會自動執行以下排程任務：
// - 每8小時：完整工具列表掃描
// - 每3分鐘：高頻市場資料更新

// 查詢特定工具的歷史資料
List<OptionData> data = optionDataRepository.findByInstrumentName("SOL_USDC-27JUN25-170-C");

// 查詢時間範圍內的資料
List<OptionData> rangeData = optionDataRepository.findByInstrumentNameAndCreatedAtBetween(
    "SOL_USDC-27JUN25-170-C", startTime, endTime);
```

### Raydium 池監控
```java
// 查詢特定池的歷史資料
List<RaydiumV3PoolData> poolData = raydiumRepository.findByPoolIdOrderByFetchedAtDesc("8sLbNZoA1cfnvMJLPfp98ZLAnFSYCFApfJKMbiXNLwxj");

// 查詢時間範圍內的池資料
List<RaydiumV3PoolData> rangeData = raydiumRepository.findByPoolIdAndFetchedAtBetweenOrderByFetchedAtDesc(
    poolId, startTime, endTime);
```

### CLMM Position 分析
```java
// 手動觸發 Position 分析
@Autowired
private CLMMPositionRunner positionRunner;

// 分析單一 Position
positionRunner.manualAnalyze("single", "68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o");

// 批次分析
positionRunner.manualAnalyze("batch", "id1,id2,id3");

// 快速狀態檢查
positionRunner.manualAnalyze("quick", null);

// 生成摘要報告
positionRunner.manualAnalyze("summary", null);
```

## ⚙️ 排程配置

### Cron 表達式設定
```java
// Deribit 選擇權
@Scheduled(cron = "0 0 */8 * * *")  // 每8小時：00:00, 08:00, 16:00
@Scheduled(cron = "0 */3 * * * *")  // 每3分鐘

// Raydium 池資訊
@Scheduled(cron = "0 0 */8 * * *")  // 每8小時

// CLMM Position 檢查
@Scheduled(cron = "0 0 */8 * * *")  // 每8小時
```

## 🔧 CLMM Position 分析功能詳解

### 支援的分析類型
1. **單一 Position 分析**：深度分析特定 Position 的所有資訊
2. **批次 Position 分析**：同時分析多個 Position 並比較
3. **快速狀態檢查**：快速獲取 Position 的關鍵指標
4. **摘要報告生成**：統計所有 Position 的總體表現

### 分析指標
- **位置價值**：Position 的當前美元價值
- **TVL 佔比**：Position 在池中的佔比
- **未領收益**：累積但尚未領取的手續費和獎勵
- **價格範圍**：Position 的有效價格區間
- **活躍狀態**：當前價格是否在有效範圍內

### 區塊鏈數據整合
- **實時 tick 數據**：直接從 Solana 節點讀取 Position 帳戶
- **Token Extensions 支援**：完整解析 NFT 元數據
- **多程序相容**：支援不同版本的 Raydium CLMM 程序

## 🚨 故障排除

### 常見問題與解決方案

1. **資料庫連線失敗**
   ```bash
   # 檢查 PostgreSQL 狀態
   sudo systemctl status postgresql
   
   # 確認連線參數
   psql -h localhost -p 5432 -U your_username -d solana_lp_db
   ```

2. **Solana 節點連接問題**
   ```properties
   # 使用公共 RPC 節點
   solana.rpcUrl=https://api.mainnet-beta.solana.com
   
   # 或暫時停用區塊鏈數據
   solana.enableBlockchainData=false
   ```

3. **API 限制問題**
   ```properties
   # 調整重試設定
   solana.maxRetries=3
   solana.retryDelay=3000
   ```

4. **CLMM Position 404 錯誤**
   ```bash
   # 檢查 Position 是否仍然活躍
   # 或使用有效的 Position ID
   export CLMM_POSITION_ID=68Yz4qUkPPLHjcqpWraXQuLC7UoFUTrybohjEobnhB5o
   ```

### 日誌設定
```properties
# 調整日誌級別
logging.level.com.example.java_solana_lp_option=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# CLMM 分析詳細日誌
logging.level.com.example.java_solana_lp_option.analyzer=TRACE
```

## 📈 性能優化建議

### 資料庫優化
```sql
-- 為查詢頻繁的欄位建立索引
CREATE INDEX idx_option_instrument_time ON option_data(instrument_name, created_at);
CREATE INDEX idx_pool_id_time ON raydium_v3_pool_data(pool_id, fetched_at);

-- 定期清理舊資料（可選）
DELETE FROM option_data WHERE created_at < NOW() - INTERVAL '30 days';
```

### API 呼叫優化
```properties
# 連線池設定
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# 超時設定
solana.connectTimeout=15000
solana.readTimeout=30000
```

## 🛡️ 安全性考量

### API 金鑰管理
```bash
# 使用環境變數儲存敏感資訊
export DATABASE_PASSWORD=your_secure_password
export SOLANA_RPC_URL=https://your-private-rpc-endpoint.com
```

### 資料庫安全
```sql
-- 限制資料庫使用者權限
GRANT SELECT, INSERT, UPDATE ON option_data TO app_user;
GRANT SELECT, INSERT, UPDATE ON raydium_v3_pool_data TO app_user;
```

## 📊 監控與警報

### 應用程式健康檢查
```properties
# 啟用 Spring Boot Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### 關鍵指標監控
- 資料庫連接狀態
- API 呼叫成功率
- Solana 節點連接狀態
- Position 分析成功率
- 排程任務執行狀態

## 🔄 版本更新記錄

### v1.3.0 (最新)
- ✨ 新增 CLMM Position 分析器
- 🔗 整合 Solana 區塊鏈數據讀取
- 📊 支援批次 Position 分析
- 🎯 實時價格範圍監控
- 💰 未領收益追蹤

### v1.2.0
- 🏊 新增 Raydium V3 池監控
- 📈 APR 計算與追蹤
- 🗄️ 池資料歷史儲存

### v1.1.0
- 🔄 選擇權資料自動排程收集
- 📊 希臘字母資料儲存
- ⏱️ 高頻市場資料更新

### v1.0.0
- 🚀 基礎應用程式架構
- 💾 PostgreSQL 資料庫整合
- 📡 Deribit API 整合

## 🤝 貢獻指南

1. Fork 專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

### 開發規範
- 使用 Java 17 語法特性
- 遵循 Spring Boot 最佳實踐
- 添加適當的單元測試
- 更新相關文檔

## 📄 授權

本專案採用 MIT 授權 - 詳見 [LICENSE](LICENSE) 檔案

## 📞 聯絡方式

如有問題或建議，請：
- 提交 GitHub Issue
- 聯絡專案維護者
- 查看 [Wiki](wiki) 獲取更多文檔

---

## 🎯 使用案例

### DeFi 投資組合管理
- 監控 Raydium 流動性池的表現
- 追蹤 CLMM Position 的收益
- 分析選擇權市場的波動率趨勢

### 量化交易策略
- 基於希臘字母的選擇權策略
- 流動性挖礦收益優化
- 跨協議套利機會識別

### 風險管理
- Position 價格範圍監控
- 未領收益閾值警報
- 市場波動率風險評估

**注意**：請確保遵守相關 API 的使用條款和限制。本應用程式僅供教育和研究目的使用，投資決策請謹慎評估風險。