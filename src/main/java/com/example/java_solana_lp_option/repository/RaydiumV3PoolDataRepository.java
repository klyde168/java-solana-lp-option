package com.example.java_solana_lp_option.repository;

import com.example.java_solana_lp_option.entity.RaydiumV3PoolData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaydiumV3PoolDataRepository extends JpaRepository<RaydiumV3PoolData, String> {
    // JpaRepository<EntityClassName, PrimaryKeyType>
    // 您可以在此處添加自訂的查詢方法 (如果需要)
}
