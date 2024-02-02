package com.school.sba.requestdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SchoolRequest {

	private String schoolName;
	private long contactNo;
	private String emailId;
	private String address;
	
}