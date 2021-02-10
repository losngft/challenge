package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransfersControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TransferService transferService;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Mock
    private NotificationService notificationService;

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
        transferService.getTransferRepository().clearTransfers();
    }

    @Test
    public void createTransfer() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":1000}")).andExpect(status().isCreated());

        Transfer transfer = transferService.getTransfer("Id-123");
        assertThat(transfer.getAccountFromId()).isEqualTo("Id-123");
        assertThat(transfer.getAccountToId()).isEqualTo("Id-124");
        assertThat(transfer.getAmount()).isEqualByComparingTo("1000");
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
