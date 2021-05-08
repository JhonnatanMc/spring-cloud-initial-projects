package co.edu.eafit.bank.service;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.eafit.bank.domain.Account;
import co.edu.eafit.bank.domain.Transaction;
import co.edu.eafit.bank.domain.TransactionType;
import co.edu.eafit.bank.domain.Users;
import co.edu.eafit.bank.dto.DepositDTO;
import co.edu.eafit.bank.dto.OTPValidationRequest;
import co.edu.eafit.bank.dto.OTPValidationResponse;
import co.edu.eafit.bank.dto.TransactionResultDTO;
import co.edu.eafit.bank.dto.TransferDTO;
import co.edu.eafit.bank.dto.WithdrawDTO;
import co.edu.eafit.bank.entityservice.AccountService;
import co.edu.eafit.bank.entityservice.TransactionService;
import co.edu.eafit.bank.entityservice.TransactionTypeService;
import co.edu.eafit.bank.entityservice.UsersService;
import co.edu.eafit.bank.exception.ZMessManager;
import co.edu.eafit.bank.openfeignClients.OTPServiceClient;
import reactor.core.publisher.Mono;


@Service
@Scope("singleton")
public class BankTransactionServiceImpl implements BankTransactionService {

	private final static Double COSTO = 2000.0;

	@Autowired
	AccountService accountService;

	@Autowired
	UsersService userService;

	@Autowired
	TransactionTypeService transactionTypeService;

	@Autowired
	TransactionService transactionService;
	
	@Autowired
	OTPServiceClient otpServiceClient;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public TransactionResultDTO transfer(TransferDTO transferDTO) throws Exception {

		WithdrawDTO withdrawDTO = new WithdrawDTO(transferDTO.getAccoIdOrigin(), transferDTO.getAmount(),
				transferDTO.getUserEmail());
		withdraw(withdrawDTO);

		DepositDTO depositDTO = new DepositDTO(transferDTO.getAccoIdDestination(), transferDTO.getAmount(),
				transferDTO.getUserEmail());
		deposit(depositDTO);

		withdrawDTO = new WithdrawDTO(transferDTO.getAccoIdOrigin(), COSTO, transferDTO.getUserEmail());
		TransactionResultDTO withdrawResult = withdraw(withdrawDTO);

		depositDTO = new DepositDTO("9999-9999-9999-9999", COSTO, transferDTO.getUserEmail());
		deposit(depositDTO);

		Optional<TransactionType> transactionType3 = transactionTypeService.findById(3);
		if (!transactionType3.isPresent()) {
			throw (new ZMessManager()).new FindingException("tipo de transacción 3");
		}
		TransactionType transactionType = transactionType3.get();

		Optional<Account> accountOptional = accountService.findById(transferDTO.getAccoIdOrigin());
		if (!accountOptional.isPresent()) {
			throw (new ZMessManager()).new FindingException("cuenta con id " + transferDTO.getAccoIdOrigin());
		}

		Account account = accountOptional.get();

		Optional<Users> userOptional = userService.findById(transferDTO.getUserEmail());
		if (!userOptional.isPresent()) {
			throw (new ZMessManager()).new FindingException("Usuario con id " + transferDTO.getUserEmail());
		}
		

		Users user = userOptional.get();
		
		// OTP Validation
		validateToken(user.getToken(), transferDTO.getToken());
		
		OTPValidationResponse oTPValidationResponse = validateToken(user.getToken(), transferDTO.getToken());
		
		if (oTPValidationResponse == null || !oTPValidationResponse.getValid()) {
			throw new Exception("No es un OTP valido");
		}

		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setAmount(transferDTO.getAmount());
		transaction.setDate(new Timestamp(System.currentTimeMillis()));
		transaction.setTranId(null);
		transaction.setTransactionType(transactionType);
		transaction.setUsers(user);

		transactionService.save(transaction);

		return new TransactionResultDTO(transaction.getTranId(), withdrawResult.getBalance());

	}
	
	//Método que se encarga de validar el Token TOTP
		private OTPValidationResponse validateToken(String user, String otp) {
		
			OTPValidationRequest otpValidationRequest = new OTPValidationRequest(user, otp);
			return otpServiceClient.validateOTP(otpValidationRequest);
		}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public TransactionResultDTO withdraw(WithdrawDTO withdrawDTO) throws Exception {

		if (withdrawDTO == null) {
			throw new Exception("El WithdrawDTO es nulo");
		}

		if (withdrawDTO.getAccoId() == null || withdrawDTO.getAccoId().trim().isEmpty() == true) {
			throw new Exception("El AccoId es obligatorio");
		}

		if (withdrawDTO.getAmount() == null || withdrawDTO.getAmount() <= 0) {
			throw new Exception("El Amount es obligatorio y debe ser mayor que cero");
		}

		if (withdrawDTO.getUserEmail() == null || withdrawDTO.getUserEmail().trim().isEmpty() == true) {
			throw new Exception("El UserEmail es obligatorio");
		}

		if (accountService.findById(withdrawDTO.getAccoId()).isPresent() == false) {
			throw new ZMessManager().new AccountNotFoundException(withdrawDTO.getAccoId());
		}

		Account account = accountService.findById(withdrawDTO.getAccoId()).get();

		if (account.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new AccountNotEnableException(withdrawDTO.getAccoId());
		}

		if (userService.findById(withdrawDTO.getUserEmail()).isPresent() == false) {
			throw new ZMessManager().new UserNotFoundException(withdrawDTO.getUserEmail());
		}

		Users user = userService.findById(withdrawDTO.getUserEmail()).get();

		if (user.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new UserDisableException(withdrawDTO.getUserEmail());
		}

		TransactionType transactionType = transactionTypeService.findById(1).get();

		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setAmount(withdrawDTO.getAmount());
		transaction.setDate(new Timestamp(System.currentTimeMillis()));
		transaction.setTranId(null);
		transaction.setTransactionType(transactionType);
		transaction.setUsers(user);

		Double nuevoSaldo = account.getBalance() - withdrawDTO.getAmount();
		account.setBalance(nuevoSaldo);

		transaction = transactionService.save(transaction);
		accountService.update(account);

		TransactionResultDTO transactionResultDTO = new TransactionResultDTO(transaction.getTranId(), nuevoSaldo);

		return transactionResultDTO;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public TransactionResultDTO deposit(DepositDTO depositDTO) throws Exception {
		if (depositDTO == null) {
			throw new Exception("El depositDTO es nulo");
		}

		if (depositDTO.getAccoId() == null || depositDTO.getAccoId().trim().isEmpty() == true) {
			throw new Exception("El AccoId es obligatorio");
		}

		if (depositDTO.getAmount() == null || depositDTO.getAmount() < 0) {
			throw new Exception("El Amount es obligatorio y debe ser mayor que cero");
		}

		if (depositDTO.getUserEmail() == null || depositDTO.getUserEmail().trim().isEmpty() == true) {
			throw new Exception("El UserEmail es obligatorio");
		}

		if (accountService.findById(depositDTO.getAccoId()).isPresent() == false) {
			throw new ZMessManager().new AccountNotFoundException(depositDTO.getAccoId());
		}

		Account account = accountService.findById(depositDTO.getAccoId()).get();

		if (account.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new AccountNotEnableException(depositDTO.getAccoId());
		}

		if (userService.findById(depositDTO.getUserEmail()).isPresent() == false) {
			throw new ZMessManager().new UserNotFoundException(depositDTO.getUserEmail());
		}

		Users user = userService.findById(depositDTO.getUserEmail()).get();

		if (user.getEnable().trim().equals("N") == true) {
			throw new ZMessManager().new UserDisableException(depositDTO.getUserEmail());
		}

		TransactionType transactionType = transactionTypeService.findById(2).get();

		Transaction transaction = new Transaction();
		transaction.setAccount(account);
		transaction.setAmount(depositDTO.getAmount());
		transaction.setDate(new Timestamp(System.currentTimeMillis()));
		transaction.setTranId(null);
		transaction.setTransactionType(transactionType);
		transaction.setUsers(user);

		Double nuevoSaldo = account.getBalance() + depositDTO.getAmount();
		account.setBalance(nuevoSaldo);

		transaction = transactionService.save(transaction);
		accountService.update(account);

		TransactionResultDTO transactionResultDTO = new TransactionResultDTO(transaction.getTranId(), nuevoSaldo);

		return transactionResultDTO;
	}

}
