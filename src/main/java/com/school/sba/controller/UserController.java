package com.school.sba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.school.sba.requestdto.UserRequest;
import com.school.sba.service.UserService;

import jakarta.validation.Valid;

@RestController
public class UserController {

	@Autowired
	private	UserService userService;

	@PreAuthorize(value = "hasAuthority('ADMIN')" )
	@PostMapping(path = "/user")
	public Object registerUser(@RequestBody @Valid UserRequest userRequest) {
		return userService.registerUser(userRequest);
	}
	
	@PostMapping(path = "/user/register")
	public Object registerUserAdmin(@RequestBody @Valid UserRequest userRequest) {
		return userService.registerAdmin(userRequest);
	}

	@PreAuthorize(value = "hasAuthority('ADMIN') OR hasAuthority('TEACHER')" )
	@GetMapping(path = "/users/{userId}")
	public Object findUserById(@PathVariable int userId) {
		return userService.findUserById(userId);
	}
}
