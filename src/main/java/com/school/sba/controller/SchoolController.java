package com.school.sba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.service.SchoolService;

@RestController
public class SchoolController {
	
	@Autowired
    private	SchoolService schoolService;

	@PreAuthorize(value = "hasAuthority('ADMIN')")
	@PostMapping(path = "/schools")
	public Object registerSchool(@RequestBody SchoolRequest schoolRequest) {
		return schoolService.registerSchool(schoolRequest);
	}
	
	@PreAuthorize(value = "hasAuthority('ADMIN')")
	@DeleteMapping(path = "/schools/{schoolId}")
	public String deleteSchool(@PathVariable int schoolId) {
		return schoolService.deleteSchool();
	}
}
