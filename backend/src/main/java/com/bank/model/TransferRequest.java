package com.bank.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TransferRequest {

    @NotNull(message = "From account ID is required")
    private Integer fromId;

    @NotNull(message = "To account ID is required")
    private Integer toId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Transfer amount must be positive")
    private Double amount;

    public TransferRequest() {}

    public TransferRequest(Integer fromId, Integer toId, Double amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }

    public Integer getFromId() { return fromId; }
    public void setFromId(Integer fromId) { this.fromId = fromId; }

    public Integer getToId() { return toId; }
    public void setToId(Integer toId) { this.toId = toId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}