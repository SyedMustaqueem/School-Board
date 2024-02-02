package com.school.sba.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.school.sba.entity.ClassHour;
import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.ConstraintViolationException;
import com.school.sba.exception.UserNotFoundException;
import com.school.sba.repository.ClassHourRepo;
import com.school.sba.repository.UserRepo;
import com.school.sba.requestdto.UserRequest;
import com.school.sba.responsedto.UserResponse;
import com.school.sba.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private ClassHourRepo classHourRepo;

	public User mapToUser(UserRequest userRequest) {
		return User.builder().username(userRequest.getUsername())
				.password(passwordEncoder.encode(userRequest.getPassword())).firstName(userRequest.getFirstName())
				.lastName(userRequest.getLastName()).contactNo(userRequest.getContactNo()).email(userRequest.getEmail())
				.userRole(userRequest.getUserRole()).build();
	}

	public UserResponse mapToUserResponse(User user) {
		return UserResponse.builder().username(user.getUsername()).firstName(user.getFirstName())
				.lastName(user.getLastName()).email(user.getEmail()).userRole(user.getUserRole()).build();
	}

	@Override
	public Object registerUser(UserRequest userRequest) {
		User user2 = userRepo.findByUserRole(UserRole.ADMIN).get();
		User user = mapToUser(userRequest);
		user.setDeleted(false);
		user.setSchool(user2.getSchool());
		if (user.getUserRole() != UserRole.ADMIN) {
			try {
				user = userRepo.save(user);
			} catch (Exception e) {
				throw new ConstraintViolationException("Duplicate Entry made", HttpStatus.IM_USED,
						"No duplicate entries are allowed");
			}
		} else {
			throw new UserNotFoundException("User with given ID cannot be registered as admin in the database",
					HttpStatus.NOT_FOUND, "Admin is already present in database");
		}
		return "User Saved Successfully";
	}

	@Override
	public String deleteUser() {
		List<User> user = userRepo.findByIsDeleted(true);
		user.forEach((u) -> {
			if (!(u.getUserRole().equals(UserRole.ADMIN))) {
				u.setDeleted(true);
				u.setAcademicPrograms(null);
				List<ClassHour> hours = classHourRepo.findByUser(u);
				hours.forEach((ch)->{
					ch.setUser(null);
					classHourRepo.save(ch);
				});
				userRepo.save(u);
				userRepo.delete(u);
			}
		});
		System.out.println(user);
		return "User Deleted Successfully";
	}
	
	@Override
	public UserResponse softDeleteUserById(int userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with given ID is not registered in the database",
						HttpStatus.NOT_FOUND, "No such user in database"));
		user.setDeleted(false);
		userRepo.save(user);
		return mapToUserResponse(user);
	}

	@Override
	public UserResponse findUserById(int userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with given ID is not registered in the database",
						HttpStatus.NOT_FOUND, "No such user in database"));
		return mapToUserResponse(user);
	}

	@Override
	public Object registerAdmin(UserRequest userRequest) {
		User user = mapToUser(userRequest);
		user.setDeleted(false);
		if (!(userRepo.existsByUserRole(UserRole.ADMIN)) && user.getUserRole() == UserRole.ADMIN) {
			try {
				user = userRepo.save(user);
			} catch (Exception e) {
				throw new ConstraintViolationException("Duplicate Entry made", HttpStatus.IM_USED,
						"No duplicate entries are allowed");
			}
		} else {
			throw new ConstraintViolationException("There is already an admin", HttpStatus.IM_USED,
					"More than 1 admin is not allowed");
		}
		return "User Saved Successfully";
	}
}
