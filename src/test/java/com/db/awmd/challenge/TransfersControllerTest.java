package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@Slf4j
public class TransfersControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
        accountsService.getAccountsRepository().clearAccounts();
        Account accountId123 = new Account("Id-123");
        accountId123.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(accountId123);

        Account accountTo = new Account("Id-124");
        accountTo.setBalance(new BigDecimal(0));
        this.accountsService.createAccount(accountTo);
    }

    @Test
    public void createTransfer() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":1000}")).andExpect(status().isCreated());
    }


    @Test
    public void createTransferNegativeAmount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":-1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createTransferNegativeBalance() throws Exception {

        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":-2000}")).andExpect(status().isBadRequest());
    }


    @Test
    public void testDeadlockHandlingMultithread() throws InterruptedException {
        String accountId1 = "Id-1235";
        Account account1 = new Account(accountId1, new BigDecimal("100"));
        this.accountsService.createAccount(account1);
        String accountId2 = "Id-1234";
        Account account2 = new Account(accountId2, new BigDecimal("20"));
        this.accountsService.createAccount(account2);
        int numberOfThreads = 1;
        ExecutorService service = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        service.submit(() -> {
            try {
                this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"Id-1235\",\"accountToId\":\"Id-1234\",\"amount\":30}")).andExpect(status().isCreated());
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        service.submit(() -> {
            try {
                this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountFromId\":\"Id-1234\",\"accountToId\":\"Id-1235\",\"amount\":10}")).andExpect(status().isCreated());
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        latch.await();

        Account accountFrom = accountsService.getAccount("Id-1235");
        Account accountTo = accountsService.getAccount("Id-1234");
        assertThat(accountFrom.getBalance()).isEqualByComparingTo("80");
        assertThat(accountTo.getBalance()).isEqualByComparingTo("40");

    }

}
