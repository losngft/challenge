package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransfersServiceTest {

    @InjectMocks
    private TransferService transferService;

    @Autowired
    private AccountsService accountsService;

    @Mock
    private NotificationService notificationService;


    @Before
    public void prepareMockMvc() {
        accountsService.getAccountsRepository().clearAccounts();
        Account accountId123 = new Account("Id-123");
        accountId123.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(accountId123);

        Account accountTo = new Account("Id-124");
        accountTo.setBalance(new BigDecimal(0));
        this.accountsService.createAccount(accountTo);

        Account accountId125 = new Account("Id-125");
        accountId125.setBalance(new BigDecimal(0));
        this.accountsService.createAccount(accountId125);

        transferService.getTransferRepository().clearTransfers();
    }

    @Test
    public void createTransfer() throws Exception {

        Transfer transfer = new Transfer("Id-123", "Id-124", new BigDecimal(1000));
        this.transferService.createTransfer(transfer);

        assertThat(this.transferService.getTransfer("Id-123")).isEqualTo(transfer);
    }

    @Test
    public void createTransfer_failsOnInvalidAmount() throws Exception {

        Transfer transfer = new Transfer("Id-125", "Id-124", new BigDecimal(-1000));

        try {
            this.transferService.createTransfer(transfer);
            fail("Should have failed when passing negative amount");
        } catch (InvalidAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Amount must be positive!");
        }

    }

    @Test
    public void createTransfer_failsOnNegativeBalance() throws Exception {

        Transfer transfer = new Transfer("Id-125", "Id-124", new BigDecimal(1000));

        try {
            this.transferService.createTransfer(transfer);
            fail("Should have failed when the account has no enough money!");
        } catch (NegativeBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + transfer.getAccountFromId() + " must not end up with negative balance!");
        }

    }
}
