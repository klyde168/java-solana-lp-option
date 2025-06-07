package com.example.java_solana_lp_option.repository;

import com.example.java_solana_lp_option.entity.TaifexOptionDelta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaifexOptionDeltaRepository extends JpaRepository<TaifexOptionDelta, Long> {

    /**
     * 根據合約類型查詢 Delta 資料
     */
    List<TaifexOptionDelta> findByContract(String contract);

    /**
     * 根據Call/Put類型查詢 Delta 資料
     */
    List<TaifexOptionDelta> findByCallPut(String callPut);

    /**
     * 根據合約和Call/Put類型查詢 Delta 資料
     */
    List<TaifexOptionDelta> findByContractAndCallPut(String contract, String callPut);

    /**
     * 根據履約價查詢 Delta 資料
     */
    List<TaifexOptionDelta> findByStrikePrice(String strikePrice);

    /**
     * 根據合約月份查詢 Delta 資料
     */
    List<TaifexOptionDelta> findByContractMonthWeek(String contractMonthWeek);

    /**
     * 根據建立時間範圍查詢 Delta 資料
     */
    List<TaifexOptionDelta> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查詢最新的 Delta 資料（按建立時間排序）
     */
    @Query("SELECT t FROM TaifexOptionDelta t ORDER BY t.createdAt DESC")
    List<TaifexOptionDelta> findLatestDeltaData();

    /**
     * 查詢特定合約的最新 Delta 資料
     */
    @Query("SELECT t FROM TaifexOptionDelta t WHERE t.contract = :contract ORDER BY t.createdAt DESC")
    List<TaifexOptionDelta> findLatestDeltaByContract(@Param("contract") String contract);

    /**
     * 查詢 TXO 合約的 Call 選擇權 Delta
     */
    @Query("SELECT t FROM TaifexOptionDelta t WHERE t.contract = 'TXO' AND t.callPut = '買權' ORDER BY t.strikePrice ASC")
    List<TaifexOptionDelta> findTXOCallDeltas();

    /**
     * 查詢 TXO 合約的 Put 選擇權 Delta
     */
    @Query("SELECT t FROM TaifexOptionDelta t WHERE t.contract = 'TXO' AND t.callPut = '賣權' ORDER BY t.strikePrice ASC")
    List<TaifexOptionDelta> findTXOPutDeltas();

    /**
     * 查詢 Delta 絕對值大於指定閾值的資料
     */
    @Query("SELECT t FROM TaifexOptionDelta t WHERE t.deltaValue IS NOT NULL AND t.deltaValue != ''")
    List<TaifexOptionDelta> findValidDeltaData();

    /**
     * 統計特定合約的 Delta 資料筆數
     */
    @Query("SELECT COUNT(t) FROM TaifexOptionDelta t WHERE t.contract = :contract")
    Long countByContract(@Param("contract") String contract);

    /**
     * 查詢今日最新的 Delta 資料
     */
    @Query("SELECT t FROM TaifexOptionDelta t WHERE t.createdAt >= :startOfDay AND t.createdAt < :startOfNextDay ORDER BY t.createdAt DESC")
    List<TaifexOptionDelta> findTodayLatestDelta(@Param("startOfDay") LocalDateTime startOfDay, @Param("startOfNextDay") LocalDateTime startOfNextDay);
}