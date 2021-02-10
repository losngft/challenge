package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NegativeBalanceException;

public interface TransferRepository {

    void createTransfer(Transfer transfer) throws NegativeBalanceException, InvalidAmountException;
    Transfer getTransfer(String accountFromId);
    void clearTransfers();
}
