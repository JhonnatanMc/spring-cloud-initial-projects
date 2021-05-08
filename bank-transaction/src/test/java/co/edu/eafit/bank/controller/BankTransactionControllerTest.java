package co.edu.eafit.bank.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.eafit.bank.dto.DepositDTO;
import co.edu.eafit.bank.dto.TransactionResultDTO;
import co.edu.eafit.bank.dto.TransferDTO;
import co.edu.eafit.bank.dto.WithdrawDTO;
import co.edu.eafit.bank.service.BankTransactionService;

@WebMvcTest(BankTransactionController.class)
@AutoConfigureMockMvc(addFilters = false) //Se salta los filtros de seguridad
class BankTransactionControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	BankTransactionService bankTransactionService;
	
	@Test
	void debeRetornar200EnDeposit() throws Exception {
		
		// Arrange
		String accountId = "4640-0341-9387-5781";
		String userEmail = "vondrusek1@wisc.edu";
		Double amount = 15000.0;
		MvcResult mvcResult = null;

		DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);
		String jsonDepositDTO = objectMapper.writeValueAsString(depositDTO);

		TransactionResultDTO transactionResultDTO = new TransactionResultDTO(32, 85000.0);

		when(bankTransactionService.deposit(any(DepositDTO.class))).thenReturn(transactionResultDTO);

		mvcResult = mockMvc
				.perform(post("/api/v1/transactions/deposit")
						.contentType("application/json")
						.content(jsonDepositDTO))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.balance").value(85000.0))
						.andReturn();

		assertEquals("application/json", mvcResult.getResponse().getContentType());

	}

	@Test
	void debeRetornar200EnWithdraw() throws Exception {
		// Arrange
		String accountId = "4640-0341-9387-5781";
		String userEmail = "vondrusek1@wisc.edu";
		Double amount = 15000.0;

		WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
		String jsonWithdrawDTO = objectMapper.writeValueAsString(withdrawDTO);

		mockMvc.perform(post("/api/v1/transactions/withdraw").contentType("application/json").content(jsonWithdrawDTO))
				.andExpect(status().isOk())
				.andReturn();

	}

	@Test
	void debeRetornar200EnTransfer() throws Exception {
		String accoIdOrigin = "4640-0341-9387-5781";
		String accoIdDestination = "6592-7866-3024-5314";
		Double amount = 15000.0;
		String userEmail = "vondrusek1@wisc.edu";
		String token = "1234";
		TransferDTO transferDTO = new TransferDTO(accoIdOrigin, accoIdDestination, amount, userEmail, token);
		
		

		String jsonTransferDTO = objectMapper.writeValueAsString(transferDTO);

		mockMvc.perform(post("/api/v1/transactions/transfer").contentType("application/json").content(jsonTransferDTO))
				.andExpect(status().isOk()).andReturn();

	}

	@Test
	void debeRetornar400EnDeposit() throws Exception {
		// Arrange
		DepositDTO depositDTO = new DepositDTO(null, null, null);
		String jsonDepositDTO = objectMapper.writeValueAsString(depositDTO);

		mockMvc.perform(post("/api/v1/transactions/deposit").contentType("application/json").content(jsonDepositDTO))
				.andExpect(status().isBadRequest()).andReturn();
	}

}
