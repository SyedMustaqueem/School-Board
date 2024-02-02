package com.school.sba.requestdto;

import com.school.sba.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

	@NotEmpty(message = "username cannot be empty and must be unique")
	private String username;

	@NotEmpty(message = "userRole cannot be empty")
	@Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must"
			+ " contain at least one letter, one number, one special character")
	private String password;

	@NotEmpty(message = "firstName cannot be empty")
	private String firstName;

	@NotEmpty(message = "lastName cannot be empty")
	private String lastName;

	@Max(value = 9999999999l)
	@Min(value = 6000000000l)
	private long contactNo;

	@NotEmpty(message = "email cannot be empty")
	@Email(regexp = "[a-zA-Z0-9+_.-]+@[g][m][a][i][l]+.[c][o][m]", message = "invalid email--Should be in the extension of '@gmail.com' ")
	private String email;
	
	private UserRole userRole;

}
