package com.example.java_solana_lp_option.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "taifex_option_data")
public class TaifexOptionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private String date;

    @Column(name = "contract")
    private String contract;

    @Column(name = "contract_month_week")
    private String contractMonthWeek;

    @Column(name = "strike_price")
    private String strikePrice;

    @Column(name = "call_put")
    private String callPut;

    @Column(name = "open_price")
    private String openPrice;

    @Column(name = "high_price")
    private String highPrice;

    @Column(name = "low_price")
    private String lowPrice;

    @Column(name = "close_price")
    private String closePrice;

    @Column(name = "volume")
    private String volume;

    @Column(name = "settlement_price")
    private String settlementPrice;

    @Column(name = "open_interest")
    private String openInterest;

    @Column(name = "best_bid")
    private String bestBid;

    @Column(name = "best_ask")
    private String bestAsk;

    @Column(name = "historical_high")
    private String historicalHigh;

    @Column(name = "historical_low")
    private String historicalLow;

    @Column(name = "trading_halt")
    private String tradingHalt;

    @Column(name = "trading_session")
    private String tradingSession;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 預設建構子
    public TaifexOptionData() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getContractMonthWeek() {
        return contractMonthWeek;
    }

    public void setContractMonthWeek(String contractMonthWeek) {
        this.contractMonthWeek = contractMonthWeek;
    }

    public String getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(String strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getCallPut() {
        return callPut;
    }

    public void setCallPut(String callPut) {
        this.callPut = callPut;
    }

    public String getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(String openPrice) {
        this.openPrice = openPrice;
    }

    public String getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(String highPrice) {
        this.highPrice = highPrice;
    }

    public String getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(String lowPrice) {
        this.lowPrice = lowPrice;
    }

    public String getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(String closePrice) {
        this.closePrice = closePrice;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(String settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public String getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(String openInterest) {
        this.openInterest = openInterest;
    }

    public String getBestBid() {
        return bestBid;
    }

    public void setBestBid(String bestBid) {
        this.bestBid = bestBid;
    }

    public String getBestAsk() {
        return bestAsk;
    }

    public void setBestAsk(String bestAsk) {
        this.bestAsk = bestAsk;
    }

    public String getHistoricalHigh() {
        return historicalHigh;
    }

    public void setHistoricalHigh(String historicalHigh) {
        this.historicalHigh = historicalHigh;
    }

    public String getHistoricalLow() {
        return historicalLow;
    }

    public void setHistoricalLow(String historicalLow) {
        this.historicalLow = historicalLow;
    }

    public String getTradingHalt() {
        return tradingHalt;
    }

    public void setTradingHalt(String tradingHalt) {
        this.tradingHalt = tradingHalt;
    }

    public String getTradingSession() {
        return tradingSession;
    }

    public void setTradingSession(String tradingSession) {
        this.tradingSession = tradingSession;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TaifexOptionData{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", contract='" + contract + '\'' +
                ", contractMonthWeek='" + contractMonthWeek + '\'' +
                ", strikePrice='" + strikePrice + '\'' +
                ", callPut='" + callPut + '\'' +
                ", openPrice='" + openPrice + '\'' +
                ", highPrice='" + highPrice + '\'' +
                ", lowPrice='" + lowPrice + '\'' +
                ", closePrice='" + closePrice + '\'' +
                ", volume='" + volume + '\'' +
                ", settlementPrice='" + settlementPrice + '\'' +
                ", openInterest='" + openInterest + '\'' +
                ", bestBid='" + bestBid + '\'' +
                ", bestAsk='" + bestAsk + '\'' +
                ", historicalHigh='" + historicalHigh + '\'' +
                ", historicalLow='" + historicalLow + '\'' +
                ", tradingHalt='" + tradingHalt + '\'' +
                ", tradingSession='" + tradingSession + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}