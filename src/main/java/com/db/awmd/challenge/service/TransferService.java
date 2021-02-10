package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.repository.TransferRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    @Getter
    private final TransferRepository transferRepository;

    @Autowired
    private NotificationService notificationService;

    private final AccountsService accountsService;

    public TransferService(TransferRepository transferRepository,AccountsService accountsService) {
        this.transferRepository = transferRepository;
        this.accountsService = accountsService;
    }

    public void createTransfer(Transfer transfer) {

        this.transferRepository.createTransfer(transfer);
        this.notificationService.notifyAboutTransfer(accountsService.getAccount(transfer.getAccountFromId()), "The amount of: " + transfer.getAmount() + " from your account was transferred to: " + transfer.getAccountToId());
        this.notificationService.notifyAboutTransfer(accountsService.getAccount(transfer.getAccountToId()), "The amount of: " + transfer.getAmount() + " was transferred to your account from: " + transfer.getAccountFromId());
    }

    public Transfer getTransfer(String accountFromId) {
        return this.getTransferRepository().getTransfer(accountFromId);
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
