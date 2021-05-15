package co.edu.eafit.bank.openfeignClients;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import co.edu.eafit.bank.dto.OTPValidationRequest;
import co.edu.eafit.bank.dto.OTPValidationResponse;

@FeignClient(
		value="api-gateway"
		)
public interface FeignClients {

	@PostMapping("/bank-otp/api/v1/otp/validate")
	public OTPValidationResponse validateOTP(
			@Valid @RequestBody
			OTPValidationRequest otpValidationRequest
			);
	
}
