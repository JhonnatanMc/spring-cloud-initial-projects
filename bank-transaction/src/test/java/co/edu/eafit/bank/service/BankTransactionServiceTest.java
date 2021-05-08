package co.edu.eafit.bank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import co.edu.eafit.bank.builder.AccountBuilder;
import co.edu.eafit.bank.builder.TransactionTypeBuilder;
import co.edu.eafit.bank.builder.UsersBuilder;
import co.edu.eafit.bank.domain.Account;
import co.edu.eafit.bank.domain.Transaction;
import co.edu.eafit.bank.domain.TransactionType;
import co.edu.eafit.bank.domain.Users;
import co.edu.eafit.bank.dto.DepositDTO;
import co.edu.eafit.bank.dto.TransactionResultDTO;
import co.edu.eafit.bank.dto.TransferDTO;
import co.edu.eafit.bank.dto.WithdrawDTO;
import co.edu.eafit.bank.entityservice.AccountServiceImpl;
import co.edu.eafit.bank.entityservice.TransactionServiceImpl;
import co.edu.eafit.bank.entityservice.TransactionTypeServiceImpl;
import co.edu.eafit.bank.entityservice.UsersServiceImpl;
import co.edu.eafit.bank.exception.ZMessManager.AccountNotEnableException;
import co.edu.eafit.bank.exception.ZMessManager.AccountNotFoundException;
import co.edu.eafit.bank.exception.ZMessManager.UserDisableException;
import co.edu.eafit.bank.exception.ZMessManager.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class BankTransactionServiceTest {

	@InjectMocks
	private BankTransactionServiceImpl bankTransactionService;

	@Mock
	AccountServiceImpl accountService;

	@Mock
	UsersServiceImpl userService;

	@Mock
	TransactionTypeServiceImpl transactionTypeService;

	@Mock
	TransactionServiceImpl transactionService;

	@Nested
	class BankTransactionServiceDepositTest {

		@Test
		void debeLanzarExceptionDepositDTONull() throws Exception {
			// Arrange
			DepositDTO depositDTO = null;
			String messageExpected = "El depositDTO es nulo";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountIdNull() throws Exception {
			// Arrange
			DepositDTO depositDTO = new DepositDTO(null, 15000.0, "vondrusek1@wisc.edu");
			String messageExpected = "El AccoId es obligatorio";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAmountMenorACero() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, -1.0, "vondrusek1@wisc.edu");
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAmountNull() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, null, "vondrusek1@wisc.edu");
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserEmailNull() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, 1500000.0, null);
			String messageExpected = "El UserEmail es obligatorio";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountNotFound() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, 15000.0, "vondrusek1@wisc.edu");
			String messageExpected = "The account with id " + accountId + " was not found";
			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(null));

			// Act
			Exception exception = assertThrows(AccountNotFoundException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountNoActiva() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			DepositDTO depositDTO = new DepositDTO(accountId, 15000.0, "vondrusek1@wisc.edu");
			String messageExpected = "La cuenta con id " + accountId + " no esta activa";
			Account account = AccountBuilder.getAccountDisable();
			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));

			// Act
			Exception exception = assertThrows(AccountNotEnableException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserNotFound() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);
			String messageExpected = "La user con Email " + userEmail + " no esta existe";
			Account account = AccountBuilder.getAccount();

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(null));

			// Act
			Exception exception = assertThrows(UserNotFoundException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserDisable() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);
			String messageExpected = "El user con Email " + userEmail + " no esta activo";
			Account account = AccountBuilder.getAccount();
			Users user = UsersBuilder.getUsersDisable();

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));

			// Act
			Exception exception = assertThrows(UserDisableException.class, () -> {
				bankTransactionService.deposit(depositDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeDepositar() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			TransactionResultDTO transactionResultDTO = null;

			DepositDTO depositDTO = new DepositDTO(accountId, amount, userEmail);

			Account account = AccountBuilder.getAccount();
			Users user = UsersBuilder.getUsers();
			TransactionType transactionType = TransactionTypeBuilder.getTransactionTypeDeposit();

			Double amountExpected = account.getBalance() + amount;

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));
			when(transactionTypeService.findById(2)).thenReturn(Optional.ofNullable(transactionType));

			when(transactionService.save(any(Transaction.class))).then(new Answer<Transaction>() {
				int sequence = 1;

				@Override
				public Transaction answer(InvocationOnMock invocation) throws Throwable {
					Transaction transaction = invocation.getArgument(0);
					transaction.setTranId(sequence);
					return transaction;
				}

			});

			transactionResultDTO = bankTransactionService.deposit(depositDTO);

			// Assert
			assertEquals(amountExpected, transactionResultDTO.getBalance());
		}

	}

	@Nested
	class BankTransactionServiceTransferTest {

		@Test
		void debeHacerUnTransfer() throws Exception {
			// Arrange
			String accoIdOrigin = "4640-0341-9387-5781";
			String accoIdDestination = "6592-7866-3024-5314";
			String accoIdBank = "9999-9999-9999-9999";
			Double amount = 15000.0;
			String userEmail = "vondrusek1@wisc.edu";
			TransactionResultDTO transactionResultDTO = null;
			String token = "1234";
			TransferDTO transferDTO = new TransferDTO(accoIdOrigin, accoIdDestination, amount, userEmail, token);

			Account accountOrigin = AccountBuilder.getAccount();
			accountOrigin.setAccoId(accoIdOrigin);

			Account accountDestination = AccountBuilder.getAccount();
			accountDestination.setAccoId(accoIdDestination);

			Account accountBank = AccountBuilder.getAccount();
			accountBank.setAccoId(accoIdBank);

			Users user = UsersBuilder.getUsers();

			TransactionType transactionTypeWithdraw = TransactionTypeBuilder.getTransactionTypeWithdraw();
			TransactionType transactionTypeDeposit = TransactionTypeBuilder.getTransactionTypeDeposit();
			TransactionType transactionTypeTransfer = TransactionTypeBuilder.getTransactionTypeTransfer();

			Double amountExpected = accountOrigin.getBalance() - amount - 2000.0;

			when(accountService.findById(accoIdOrigin)).thenReturn(Optional.ofNullable(accountOrigin));
			when(accountService.findById(accoIdDestination)).thenReturn(Optional.ofNullable(accountDestination));
			when(accountService.findById(accoIdBank)).thenReturn(Optional.ofNullable(accountBank));

			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));

			when(transactionTypeService.findById(1)).thenReturn(Optional.ofNullable(transactionTypeWithdraw));
			when(transactionTypeService.findById(2)).thenReturn(Optional.ofNullable(transactionTypeDeposit));
			when(transactionTypeService.findById(3)).thenReturn(Optional.ofNullable(transactionTypeTransfer));

			when(transactionService.save(any(Transaction.class))).then(new Answer<Transaction>() {
				int sequence = 1;

				@Override
				public Transaction answer(InvocationOnMock invocation) throws Throwable {
					Transaction transaction = invocation.getArgument(0);
					transaction.setTranId(sequence);
					return transaction;
				}

			});

			transactionResultDTO = bankTransactionService.transfer(transferDTO);

			// Assert
			assertEquals(amountExpected, transactionResultDTO.getBalance());
		}

	}

	@Nested
	class BankTransactionServiceWithdrawTest {

		@Test
		void debeLanzarExceptionWithdrawDTONull() throws Exception {
			// Arrange
			WithdrawDTO withdrawDTO = null;
			String messageExpected = "El WithdrawDTO es nulo";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountIdNull() throws Exception {
			// Arrange

			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 15000.0;

			WithdrawDTO withdrawDTO = new WithdrawDTO(null, amount, userEmail);
			String messageExpected = "El AccoId es obligatorio";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAmountMenorACero() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = -1.0;

			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAmountNull() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = null;

			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
			String messageExpected = "El Amount es obligatorio y debe ser mayor que cero";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserEmailNull() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = null;
			Double amount = 1500000.0;

			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
			String messageExpected = "El UserEmail es obligatorio";

			// Act
			Exception exception = assertThrows(Exception.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});

			// Assert
			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountNotFound() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 1500000.0;
			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
			String messageExpected = "The account with id " + accountId + " was not found";
			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(null));

			// Act
			Exception exception = assertThrows(AccountNotFoundException.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionAccountNoActiva() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 1500000.0;
			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
			String messageExpected = "La cuenta con id " + accountId + " no esta activa";
			Account account = AccountBuilder.getAccountDisable();
			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));

			// Act
			Exception exception = assertThrows(AccountNotEnableException.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserNotFound() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 1500000.0;
			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);
			String messageExpected = "La user con Email " + userEmail + " no esta existe";
			Account account = AccountBuilder.getAccount();

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(null));

			// Act
			Exception exception = assertThrows(UserNotFoundException.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeLanzarExceptionUserDisable() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 1500000.0;
			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);

			String messageExpected = "El user con Email " + userEmail + " no esta activo";
			Account account = AccountBuilder.getAccount();
			Users user = UsersBuilder.getUsersDisable();

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));

			// Act
			Exception exception = assertThrows(UserDisableException.class, () -> {
				bankTransactionService.withdraw(withdrawDTO);
			});
			// Assert

			assertEquals(messageExpected, exception.getMessage());
		}

		@Test
		void debeWithdraw() throws Exception {
			// Arrange
			String accountId = "4640-0341-9387-5781";
			String userEmail = "vondrusek1@wisc.edu";
			Double amount = 1500000.0;
			WithdrawDTO withdrawDTO = new WithdrawDTO(accountId, amount, userEmail);

			TransactionResultDTO transactionResultDTO = null;

			Account account = AccountBuilder.getAccount();
			Users user = UsersBuilder.getUsers();
			TransactionType transactionType = TransactionTypeBuilder.getTransactionTypeWithdraw();

			Double amountExpected = account.getBalance() - amount;

			when(accountService.findById(accountId)).thenReturn(Optional.ofNullable(account));
			when(userService.findById(userEmail)).thenReturn(Optional.ofNullable(user));
			when(transactionTypeService.findById(1)).thenReturn(Optional.ofNullable(transactionType));

			when(transactionService.save(any(Transaction.class))).then(new Answer<Transaction>() {
				int sequence = 1;

				@Override
				public Transaction answer(InvocationOnMock invocation) throws Throwable {
					Transaction transaction = invocation.getArgument(0);
					transaction.setTranId(sequence);
					return transaction;
				}

			});

			transactionResultDTO = bankTransactionService.withdraw(withdrawDTO);

			// Assert
			assertEquals(amountExpected, transactionResultDTO.getBalance());
		}

	}

}
