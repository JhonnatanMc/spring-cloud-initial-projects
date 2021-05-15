package co.edu.eafit.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import co.edu.eafit.bank.dto.OTPValidationResponse;
import co.edu.eafit.bank.dto.OTPValidationRequest;
import co.edu.eafit.bank.openfeignClients.FeignClients;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class OTPServiceCircuitBreakerImpl implements OTPServiceCircuitBreaker {
	
	@Autowired
	FeignClients feignClients;
	
	
	@CircuitBreaker(
			name = "bank-otp",
			fallbackMethod = "fallbackValidateToken")
	@Override
	public OTPValidationResponse validateToken(String user, String otp) throws Exception {
		OTPValidationRequest otpValidationRequest = new OTPValidationRequest(user, otp);
		return feignClients.validateOTP(otpValidationRequest);
	}

	public OTPValidationResponse fallbackValidateToken(String user, String otp, Throwable e) throws Exception {
		throw new Exception("El servicio de OTP no está disponible en este momento");
	}
}

