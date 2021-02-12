package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransfersServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountsService accountsService;


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

    }

    @Test
    public void createTransfer() throws Exception {

        Transfer transfer = new Transfer("Id-123", "Id-124", new BigDecimal(1000));
        this.transferService.createTransfer(transfer);

        assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualTo(new BigDecimal(0));
        assertThat(this.accountsService.getAccount("Id-124").getBalance()).isEqualTo(new BigDecimal(1000));
    }

    @Test
    public void createTransferDeadlock() throws Exception {
        Transfer transferA = new Transfer("Id-123", "Id-124", new BigDecimal(20));
        Transfer transferB = new Transfer("Id-124", "Id-123", new BigDecimal(15));


        CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; ++i) {
            Runnable runnableTask = () -> {
                try {
                    this.transferService.createTransfer(transferA);
                    this.transferService.createTransfer(transferB);
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            new Thread(runnableTask, "Test Thread " + i).start();
        }
        // all threads are now waiting on the latch.
        latch.countDown(); // release the latch
        // all threads are now running the test method at the same time.
        assertThat(this.accountsService.getAccount("Id-124").getBalance()).isGreaterThanOrEqualTo(new BigDecimal(0));
        assertThat(this.accountsService.getAccount("Id-123").getBalance()).isLessThanOrEqualTo(new BigDecimal(1000));

//        int numberOfThreads = 2;
//        ExecutorService service = Executors.newFixedThreadPool(10);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        Transfer transferA = new Transfer("Id-123", "Id-124", new BigDecimal(20));
//        Transfer transferB = new Transfer("Id-124", "Id-123", new BigDecimal(15));
//        for (int i = 0; i < numberOfThreads; i++) {
//            service.submit(() -> {
//                    this.transferService.createTransfer(transferA);
//                    this.transferService.createTransfer(transferB);
//                latch.countDown();
//            });
//        }
//        latch.await();
//        assertThat(this.accountsService.getAccount("Id-124").getBalance()).isGreaterThan(new BigDecimal(0));
  //  }


}

    @Test
    public void createTransfer_failsOnInvalidAmount() throws Exception {

        Transfer transfer = new Transfer("Id-125", "Id-124", new BigDecimal(-1000));

        try {
            this.transferService.createTransfer(transfer);
            fail("Should have failed when passing negative amount");
        } catch (InvalidAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("The amount must be positive!!");
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
