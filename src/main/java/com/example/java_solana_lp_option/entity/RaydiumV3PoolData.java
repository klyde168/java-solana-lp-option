package com.example.java_solana_lp_option.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "raydium_v3_pool_data")
public class RaydiumV3PoolData {

    @Id
    @Column(name = "pool_id", nullable = false, length = 100) // 池子ID，通常是字串，例如 Base58 編碼的地址
    private String poolId;

    @Column(name = "mint_a_symbol", length = 20)
    private String mintASymbol;

    @Column(name = "mint_b_symbol", length = 20)
    private String mintBSymbol;

    @Column(name = "price")
    private Double price;

    @Column(name = "mint_amount_a")
    private Double mintAmountA;

    @Column(name = "mint_amount_b")
    private Double mintAmountB;

    @Column(name = "fee_rate") // 儲存原始的小數值，例如 0.0001
    private Double feeRate;

    @Column(name = "tvl")
    private Double tvl;

    @Column(name = "day_volume")
    private Double dayVolume;

    @Column(name = "day_volume_fee")
    private Double dayVolumeFee;

    @Column(name = "day_apr") // 儲存原始的百分比值，例如 26.53 (代表 26.53%)
    private Double dayApr;

    @Column(name = "day_fee_apr") // 儲存原始的百分比值，例如 24.72 (代表 24.72%)
    private Double dayFeeApr;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    // Constructors
    public RaydiumV3PoolData() {
        this.fetchedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    public String getMintASymbol() {
        return mintASymbol;
    }

    public void setMintASymbol(String mintASymbol) {
        this.mintASymbol = mintASymbol;
    }

    public String getMintBSymbol() {
        return mintBSymbol;
    }

    public void setMintBSymbol(String mintBSymbol) {
        this.mintBSymbol = mintBSymbol;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getMintAmountA() {
        return mintAmountA;
    }

    public void setMintAmountA(Double mintAmountA) {
        this.mintAmountA = mintAmountA;
    }

    public Double getMintAmountB() {
        return mintAmountB;
    }

    public void setMintAmountB(Double mintAmountB) {
        this.mintAmountB = mintAmountB;
    }

    public Double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Double feeRate) {
        this.feeRate = feeRate;
    }

    public Double getTvl() {
        return tvl;
    }

    public void setTvl(Double tvl) {
        this.tvl = tvl;
    }

    public Double getDayVolume() {
        return dayVolume;
    }

    public void setDayVolume(Double dayVolume) {
        this.dayVolume = dayVolume;
    }

    public Double getDayVolumeFee() {
        return dayVolumeFee;
    }

    public void setDayVolumeFee(Double dayVolumeFee) {
        this.dayVolumeFee = dayVolumeFee;
    }

    public Double getDayApr() {
        return dayApr;
    }

    public void setDayApr(Double dayApr) {
        this.dayApr = dayApr;
    }

    public Double getDayFeeApr() {
        return dayFeeApr;
    }

    public void setDayFeeApr(Double dayFeeApr) {
        this.dayFeeApr = dayFeeApr;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    @Override
    public String toString() {
        return "RaydiumV3PoolData{" +
                "poolId='" + poolId + '\'' +
                ", mintASymbol='" + mintASymbol + '\'' +
                ", mintBSymbol='" + mintBSymbol + '\'' +
                ", price=" + price +
                ", fetchedAt=" + fetchedAt +
                '}';
    }
}
