package com.example.java_solana_lp_option.repository;

import com.example.java_solana_lp_option.entity.OptionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OptionDataRepository extends JpaRepository<OptionData, Long> {
    
    // 根據工具名稱查詢
    List<OptionData> findByInstrumentName(String instrumentName);
    
    // 根據工具名稱和時間範圍查詢
    List<OptionData> findByInstrumentNameAndCreatedAtBetween(
        String instrumentName, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    // 查詢最新的記錄
    @Query("SELECT o FROM OptionData o WHERE o.instrumentName = :instrumentName ORDER BY o.createdAt DESC")
    List<OptionData> findLatestByInstrumentName(@Param("instrumentName") String instrumentName);
    
    // 查詢今天的所有記錄 - 使用參數化查詢更安全
    @Query("SELECT o FROM OptionData o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay ORDER BY o.createdAt DESC")
    List<OptionData> findTodayRecords(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // 查詢最近的記錄（更實用的方法）
    @Query("SELECT o FROM OptionData o ORDER BY o.createdAt DESC")
    List<OptionData> findAllOrderByCreatedAtDesc();
}