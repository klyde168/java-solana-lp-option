package com.example.java_solana_lp_option.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "taifex_option_delta")
public class TaifexOptionDelta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract")
    private String contract;

    @Column(name = "call_put")
    private String callPut;

    @Column(name = "contract_month_week")
    private String contractMonthWeek;

    @Column(name = "strike_price")
    private String strikePrice;

    @Column(name = "delta_value")
    private String deltaValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 預設建構子
    public TaifexOptionDelta() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getCallPut() {
        return callPut;
    }

    public void setCallPut(String callPut) {
        this.callPut = callPut;
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

    public String getDeltaValue() {
        return deltaValue;
    }

    public void setDeltaValue(String deltaValue) {
        this.deltaValue = deltaValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TaifexOptionDelta{" +
                "id=" + id +
                ", contract='" + contract + '\'' +
                ", callPut='" + callPut + '\'' +
                ", contractMonthWeek='" + contractMonthWeek + '\'' +
                ", strikePrice='" + strikePrice + '\'' +
                ", deltaValue='" + deltaValue + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}