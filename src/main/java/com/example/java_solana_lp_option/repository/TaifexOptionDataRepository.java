package com.example.java_solana_lp_option.repository;

import com.example.java_solana_lp_option.entity.TaifexOptionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaifexOptionDataRepository extends JpaRepository<TaifexOptionData, Long> {

    /**
     * 根據日期查詢選擇權資料
     */
    List<TaifexOptionData> findByDate(String date);

    /**
     * 根據合約類型查詢選擇權資料
     */
    List<TaifexOptionData> findByContract(String contract);

    /**
     * 根據日期和合約類型查詢選擇權資料
     */
    List<TaifexOptionData> findByDateAndContract(String date, String contract);

    /**
     * 根據Call/Put類型查詢選擇權資料
     */
    List<TaifexOptionData> findByCallPut(String callPut);

    /**
     * 根據履約價查詢選擇權資料
     */
    List<TaifexOptionData> findByStrikePrice(String strikePrice);

    /**
     * 根據日期範圍查詢選擇權資料
     */
    List<TaifexOptionData> findByDateBetween(String startDate, String endDate);

    /**
     * 根據合約月份查詢選擇權資料
     */
    List<TaifexOptionData> findByContractMonthWeek(String contractMonthWeek);

    /**
     * 根據建立時間範圍查詢選擇權資料
     */
    List<TaifexOptionData> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查詢最新的選擇權資料（按建立時間排序）
     */
    @Query("SELECT t FROM TaifexOptionData t ORDER BY t.createdAt DESC")
    List<TaifexOptionData> findLatestData();

    /**
     * 查詢特定日期的最新資料
     */
    @Query("SELECT t FROM TaifexOptionData t WHERE t.date = :date ORDER BY t.createdAt DESC")
    List<TaifexOptionData> findLatestDataByDate(@Param("date") String date);

    /**
     * 查詢具有交易量的資料（排除交易量為0或空的資料）
     */
    @Query("SELECT t FROM TaifexOptionData t WHERE t.volume IS NOT NULL AND t.volume != '0' AND t.volume != ''")
    List<TaifexOptionData> findDataWithVolume();

    /**
     * 查詢最高未平倉合約的選擇權
     */
    @Query("SELECT t FROM TaifexOptionData t WHERE t.openInterest IS NOT NULL ORDER BY CAST(t.openInterest AS integer) DESC")
    List<TaifexOptionData> findByHighestOpenInterest();

    /**
     * 統計特定日期的資料筆數
     */
    @Query("SELECT COUNT(t) FROM TaifexOptionData t WHERE t.date = :date")
    Long countByDate(@Param("date") String date);

    /**
     * 查詢特定合約和日期的Call選擇權
     */
    @Query("SELECT t FROM TaifexOptionData t WHERE t.contract = :contract AND t.date = :date AND t.callPut = '買權'")
    List<TaifexOptionData> findCallOptionsByContractAndDate(@Param("contract") String contract, @Param("date") String date);

    /**
     * 查詢特定合約和日期的Put選擇權
     */
    @Query("SELECT t FROM TaifexOptionData t WHERE t.contract = :contract AND t.date = :date AND t.callPut = '賣權'")
    List<TaifexOptionData> findPutOptionsByContractAndDate(@Param("contract") String contract, @Param("date") String date);
}