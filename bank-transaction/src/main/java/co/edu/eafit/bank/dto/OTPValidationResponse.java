package co.edu.eafit.bank.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OTPValidationResponse {
	
	@NotNull
	private Boolean valid;
	
	@NotNull
	private Integer doubleCheckCode;
	
	@NotNull
	private String errorMessage;

}
