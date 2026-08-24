package com.bornfire.entities;

import java.math.BigDecimal;

public class OffsetUpdateRequest {
    private String tranId;
    private Long partTranId;
    private BigDecimal appliedOffsetAmount;

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public Long getPartTranId() {
        return partTranId;
    }

    public void setPartTranId(Long partTranId) {
        this.partTranId = partTranId;
    }

    public BigDecimal getAppliedOffsetAmount() {
        return appliedOffsetAmount;
    }

    public void setAppliedOffsetAmount(BigDecimal appliedOffsetAmount) {
        this.appliedOffsetAmount = appliedOffsetAmount;
    }
}
