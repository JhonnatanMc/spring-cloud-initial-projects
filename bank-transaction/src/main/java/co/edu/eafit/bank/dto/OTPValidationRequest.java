package co.edu.eafit.bank.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OTPValidationRequest {
	
	@NotNull
	private String user;
	
	@NotNull
	private String otp;

}
