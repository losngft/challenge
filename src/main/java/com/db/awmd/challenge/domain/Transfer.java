package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Transfer {

    @NotNull
    @NotEmpty
    private final String accountFromId;

    @NotNull
    @NotEmpty
    private final String accountToId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "The amount must be positive!")
    private BigDecimal amount;

    @JsonCreator
    public Transfer(@JsonProperty("accountFromId") String accountFromId,
                    @JsonProperty("accountToId") String accountToId,
                    @JsonProperty("amount") BigDecimal amount) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }

    public String getAccountFromId() {
        return accountFromId;
    }

    public String getAccountToId() {
        return accountToId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
