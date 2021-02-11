package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransferService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
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

}
