package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.exception.NonexistentAccountException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TransferRepositoryInMemory implements TransferRepository {

    private final Map<String, Transfer> transferMap = new ConcurrentHashMap<>();
    private final AccountsRepository accountsRepository;

    public TransferRepositoryInMemory(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @Override
    public void createTransfer(Transfer transfer) throws NegativeBalanceException, InvalidAmountException {

        Transfer currentTransfer = transferMap.computeIfAbsent(transfer.getAccountFromId(), k->transfer);
        if (transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be positive!");
        }

        Account accountFrom = accountsRepository.getAccount(currentTransfer.getAccountFromId());
        Account accountTo = accountsRepository.getAccount(currentTransfer.getAccountToId());

        if(accountFrom == null){
            throw new NonexistentAccountException("The account with Id: "+currentTransfer.getAccountFromId()+" does not exist!!");
        }

        if(accountTo==null){
            throw new NonexistentAccountException("The account with Id: "+currentTransfer.getAccountToId()+" does not exist!!");
        }

        BigDecimal balance = accountFrom.getBalance().subtract(transfer.getAmount());
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeBalanceException(
                    "Account id " + currentTransfer.getAccountFromId() + " must not end up with negative balance!");
        }

        accountFrom.setBalance(balance);
        accountTo.getBalance().add(transfer.getAmount());
    }

    @Override
    public Transfer getTransfer(String accountFromId) {
        return transferMap.get(accountFromId);
    }

    @Override
    public void clearTransfers() {
        transferMap.clear();
    }
}
