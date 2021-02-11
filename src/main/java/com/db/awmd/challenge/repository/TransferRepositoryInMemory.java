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

        String uniqueTransferId = "Id-" + System.currentTimeMillis();
        Transfer currentTransfer = transferMap.computeIfAbsent(uniqueTransferId, k->transfer);
        if (transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be positive!");
        }

        Account accountFrom = accountsRepository.getAccount(transfer.getAccountFromId());
        Account accountTo = accountsRepository.getAccount(transfer.getAccountToId());

        if(accountFrom == null){
            throw new NonexistentAccountException("The account with Id: "+transfer.getAccountFromId()+" does not exist!!");
        }

        if(accountTo==null){
            throw new NonexistentAccountException("The account with Id: "+transfer.getAccountToId()+" does not exist!!");
        }

        BigDecimal balanceFrom = accountFrom.getBalance().subtract(transfer.getAmount());
        if (balanceFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeBalanceException(
                    "Account id " + currentTransfer.getAccountFromId() + " must not end up with negative balance!");
        }

        accountFrom.setBalance(balanceFrom);
        BigDecimal balanceTo = accountTo.getBalance().add(transfer.getAmount());
        accountTo.setBalance(balanceTo);
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
