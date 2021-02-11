package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.exception.NonexistentAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    @Getter
    private final AccountsRepository accountsRepository;

    private final NotificationService notificationService;

    private final AccountsService accountsService;

    public TransferService(AccountsRepository accountsRepository, NotificationService notificationService, AccountsService accountsService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
        this.accountsService = accountsService;
    }

    public synchronized void createTransfer(Transfer transfer) {

        if (transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("The amount must be positive!!");
        }

        Account accountFrom = accountsRepository.getAccount(transfer.getAccountFromId());
        Account accountTo = accountsRepository.getAccount(transfer.getAccountToId());

        if (accountFrom == null) {
            throw new NonexistentAccountException("The account with Id: " + transfer.getAccountFromId() + " does not exist!!");
        }

        if (accountTo == null) {
            throw new NonexistentAccountException("The account with Id: " + transfer.getAccountToId() + " does not exist!!");
        }

        BigDecimal balanceFrom = accountFrom.getBalance().subtract(transfer.getAmount());
        if (balanceFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeBalanceException(
                    "Account id " + transfer.getAccountFromId() + " must not end up with negative balance!");
        }

        accountFrom.setBalance(balanceFrom);
        BigDecimal balanceTo = accountTo.getBalance().add(transfer.getAmount());
        accountTo.setBalance(balanceTo);

        this.notificationService.notifyAboutTransfer(accountsService.getAccount(transfer.getAccountFromId()), "The amount of: " + transfer.getAmount() + " from your account was transferred to: " + transfer.getAccountToId());
        this.notificationService.notifyAboutTransfer(accountsService.getAccount(transfer.getAccountToId()), "The amount of: " + transfer.getAmount() + " was transferred to your account from: " + transfer.getAccountFromId());
    }


}
