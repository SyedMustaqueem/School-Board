package com.school.sba.service;

import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;

public interface UserService {

	Object registerAdmin(UserRequest userRequest);

	String deleteUser();

	Object findUserById(int userId);

	Object registerUser(UserRequest userRequest);

	UserResponse softDeleteUserById(int userId);

}
