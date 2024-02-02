package com.school.sba.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.school.sba.entity.AcademicProgram;
import com.school.sba.entity.School;
import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.SchoolNotFoundException;
import com.school.sba.exception.UnauthorizedException;
import com.school.sba.exception.UserNotFoundException;
import com.school.sba.repository.AcademicProgramRepo;
import com.school.sba.repository.ClassHourRepo;
import com.school.sba.repository.SchoolRepo;
import com.school.sba.repository.UserRepo;
import com.school.sba.requestdto.SchoolRequest;
import com.school.sba.responsedto.SchoolResponse;
import com.school.sba.service.SchoolService;
import com.school.sba.util.ResponseStructure;

@Service
public class SchoolServiceImpl implements SchoolService {

	@Autowired
	SchoolRepo schoolRepo;

	@Autowired
	UserRepo userRepo;
	
	@Autowired
	AcademicProgramRepo programRepo;
	
	@Autowired
	ClassHourRepo classHourRepo;

	@Autowired
	ResponseStructure<SchoolResponse> responseStructure;

	public School mapToSchool(SchoolRequest schoolRequest) {
		return School.builder().schoolName(schoolRequest.getSchoolName()).emailId(schoolRequest.getEmailId())
				.contactNo(schoolRequest.getContactNo()).address(schoolRequest.getAddress()).build();
	}

	public SchoolResponse mapToSchoolResponse(School school) {
		return SchoolResponse.builder().shoolName(school.getSchoolName()).emailId(school.getEmailId())
				.contactNo(school.getContactNo()).address(school.getAddress()).build();
	}

	@Override
	public ResponseEntity<ResponseStructure<SchoolResponse>> registerSchool(SchoolRequest schoolRequest) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = new User();
		user = userRepo.findByUsername(name)
				.orElseThrow(() -> new UserNotFoundException("User with given ID is not registered in the database",
						HttpStatus.NOT_FOUND, "No such user in database"));
		if (user.getUserRole().equals(UserRole.ADMIN)) {
			if (user.getSchool() == null) {
				School school = schoolRepo.save(mapToSchool(schoolRequest));
				user.setSchool(school);
				userRepo.save(user);
				responseStructure.setStatus(HttpStatus.CREATED.value());
				responseStructure.setData(mapToSchoolResponse(school));
				responseStructure.setMessage("School data has been registered successfully");
			} else {
				throw new SchoolNotFoundException("1 School is already present ", HttpStatus.FORBIDDEN,
						"More than one school cannot be created");
			}
		} else {
			throw new UnauthorizedException("User with given Id is not authorized to add school",
					HttpStatus.UNAUTHORIZED, "Only Admin has access");
		}
		return new ResponseEntity<ResponseStructure<SchoolResponse>>(responseStructure, HttpStatus.CREATED);
	}

	@Override
	public String deleteSchool() {
		List<School> schools = schoolRepo.findByIsDeleted(true);
		schools.forEach((school) -> {
			List<AcademicProgram> programs = school.getAcademicPrograms();
			programs.forEach((program) -> {
				classHourRepo.deleteAll(program.getClassHours());
			});
			programRepo.deleteAll(programs);
			List<User> users = userRepo.findBySchool(school);
			users.forEach((u) ->{
				if (u.getUserRole().equals(UserRole.ADMIN)) {
					users.remove(u);
				}
			});
			userRepo.deleteAll(users);
		});
		schoolRepo.deleteAll(schools);
		return "School Deleted";
	}
}