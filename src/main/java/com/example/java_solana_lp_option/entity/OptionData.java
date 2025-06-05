package com.example.java_solana_lp_option.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "option_data")
public class OptionData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "instrument_name", nullable = false)
    private String instrumentName;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "timestamp_value")
    private Long timestampValue;
    
    @Column(name = "formatted_time")
    private String formattedTime;
    
    @Column(name = "change_id")
    private String changeId;
    
    // 希臘字母
    @Column(name = "delta_value")
    private Double deltaValue;
    
    @Column(name = "gamma_value")
    private Double gammaValue;
    
    @Column(name = "vega_value")
    private Double vegaValue;
    
    @Column(name = "theta_value")
    private Double thetaValue;
    
    @Column(name = "rho_value")
    private Double rhoValue;
    
    // 價格資訊
    @Column(name = "index_price")
    private Double indexPrice;
    
    @Column(name = "underlying_price")
    private Double underlyingPrice;
    
    @Column(name = "mark_price")
    private Double markPrice;
    
    @Column(name = "open_interest")
    private Double openInterest;
    
    // 隱含波動率
    @Column(name = "mark_iv")
    private Double markIv;
    
    @Column(name = "bid_iv")
    private Double bidIv;
    
    @Column(name = "ask_iv")
    private Double askIv;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 建構函數
    public OptionData() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getInstrumentName() {
        return instrumentName;
    }
    
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public Long getTimestampValue() {
        return timestampValue;
    }
    
    public void setTimestampValue(Long timestampValue) {
        this.timestampValue = timestampValue;
    }
    
    public String getFormattedTime() {
        return formattedTime;
    }
    
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    
    public String getChangeId() {
        return changeId;
    }
    
    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }
    
    public Double getDeltaValue() {
        return deltaValue;
    }
    
    public void setDeltaValue(Double deltaValue) {
        this.deltaValue = deltaValue;
    }
    
    public Double getGammaValue() {
        return gammaValue;
    }
    
    public void setGammaValue(Double gammaValue) {
        this.gammaValue = gammaValue;
    }
    
    public Double getVegaValue() {
        return vegaValue;
    }
    
    public void setVegaValue(Double vegaValue) {
        this.vegaValue = vegaValue;
    }
    
    public Double getThetaValue() {
        return thetaValue;
    }
    
    public void setThetaValue(Double thetaValue) {
        this.thetaValue = thetaValue;
    }
    
    public Double getRhoValue() {
        return rhoValue;
    }
    
    public void setRhoValue(Double rhoValue) {
        this.rhoValue = rhoValue;
    }
    
    public Double getIndexPrice() {
        return indexPrice;
    }
    
    public void setIndexPrice(Double indexPrice) {
        this.indexPrice = indexPrice;
    }
    
    public Double getUnderlyingPrice() {
        return underlyingPrice;
    }
    
    public void setUnderlyingPrice(Double underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }
    
    public Double getMarkPrice() {
        return markPrice;
    }
    
    public void setMarkPrice(Double markPrice) {
        this.markPrice = markPrice;
    }
    
    public Double getOpenInterest() {
        return openInterest;
    }
    
    public void setOpenInterest(Double openInterest) {
        this.openInterest = openInterest;
    }
    
    public Double getMarkIv() {
        return markIv;
    }
    
    public void setMarkIv(Double markIv) {
        this.markIv = markIv;
    }
    
    public Double getBidIv() {
        return bidIv;
    }
    
    public void setBidIv(Double bidIv) {
        this.bidIv = bidIv;
    }
    
    public Double getAskIv() {
        return askIv;
    }
    
    public void setAskIv(Double askIv) {
        this.askIv = askIv;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}