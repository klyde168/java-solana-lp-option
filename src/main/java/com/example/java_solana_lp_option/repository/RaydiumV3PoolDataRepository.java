// 檔案路徑: com/example/java_solana_lp_option/repository/RaydiumV3PoolDataRepository.java
package com.example.java_solana_lp_option.repository;

import com.example.java_solana_lp_option.entity.RaydiumV3PoolData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // 引入需要的類別
import java.util.List;          // 引入需要的類別

@Repository
// 將主鍵類型從 String 改為 Long
public interface RaydiumV3PoolDataRepository extends JpaRepository<RaydiumV3PoolData, Long> {
    
    // 您可以在此處添加自訂的查詢方法 (如果需要)
    // 例如，根據 poolId 查詢並按 fetchedAt 降序排列
    List<RaydiumV3PoolData> findByPoolIdOrderByFetchedAtDesc(String poolId);

    // 例如，根據 poolId 和時間範圍查詢
    List<RaydiumV3PoolData> findByPoolIdAndFetchedAtBetweenOrderByFetchedAtDesc(String poolId, LocalDateTime startTime, LocalDateTime endTime);
}
