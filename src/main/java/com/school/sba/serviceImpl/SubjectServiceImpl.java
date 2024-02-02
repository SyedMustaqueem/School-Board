package com.school.sba.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.Subject;
import com.school.sba.entity.User;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.UnauthorizedException;
import com.school.sba.exception.UserNotFoundException;
import com.school.sba.repository.AcademicProgramRepo;
import com.school.sba.repository.SubjectRepo;
import com.school.sba.repository.UserRepo;
import com.school.sba.requestdto.SubjectRequest;
import com.school.sba.responsedto.AcademicProgramResponse;
import com.school.sba.service.SubjectService;
import com.school.sba.util.ResponseStructure;

@Service
public class SubjectServiceImpl implements SubjectService {

	@Autowired
	SubjectRepo subjectRepo;

	@Autowired
	AcademicProgramRepo programRepo;

	@Autowired
	UserRepo userRepo;

	@Autowired
	ResponseStructure<AcademicProgramResponse> programStructure;

	@Autowired
	AcademicProgramServiceImpl academicProgramServiceImpl;

	@Autowired
	ResponseStructure<List<Subject>> subjectStructure;
	
	@Autowired
	ResponseStructure<User> userStructure;

	@Override
	public ResponseEntity<ResponseStructure<AcademicProgramResponse>> insertSubjectList(SubjectRequest subjectRequests,
			int programId) {
		return programRepo.findById(programId).map(program -> {
			List<Subject> subjects = new ArrayList<>();
			subjectRequests.getSubjectNames().forEach(name -> {
				Subject subject = subjectRepo.findBySubjectName(name.toLowerCase()).map(s -> s).orElseGet(() -> {
					Subject subject2 = new Subject();
					subject2.setSubjectName(name);
					subjectRepo.save(subject2);
					return subject2;
				});
				subjects.add(subject);
			});
			program.setSubjects(subjects);
			programRepo.save(program);
			programStructure.setData(academicProgramServiceImpl.mapToAcademicResponseProgram(program));
			programStructure.setMessage("Updated the subject list to academic program");
			programStructure.setStatus(HttpStatus.CREATED.value());
			return new ResponseEntity<ResponseStructure<AcademicProgramResponse>>(programStructure, HttpStatus.CREATED);
		}).orElseThrow(() -> new UserNotFoundException(null, null, null));
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Subject>>> fetchAllSubjects() {
		List<Subject> findAll = subjectRepo.findAll();
		subjectStructure.setData(findAll);
		subjectStructure.setStatus(HttpStatus.FOUND.value());
		subjectStructure.setMessage("List of all subjects");
		return new ResponseEntity<ResponseStructure<List<Subject>>>(subjectStructure, HttpStatus.FOUND);
	}

	@Override
	public ResponseEntity<ResponseStructure<User>> mapSubjectToTeacher(int subjectId, int userId) {
		User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("User with given ID is not registered in the database",
				HttpStatus.NOT_FOUND, "No such user in database"));
		Subject subject = subjectRepo.findById(subjectId)
				.orElseThrow(() -> new UserNotFoundException("Subject with given ID is not registered in the database",
						HttpStatus.NOT_FOUND, "No such Subject in database"));
		if (user.getUserRole().equals(UserRole.TEACHER)) {
			user.setSubject(subject);
			userRepo.save(user);
			userStructure.setData(user);
			userStructure.setStatus(HttpStatus.ACCEPTED.value());
			userStructure.setMessage("Assigned to the subject");
		} else {
			throw new UnauthorizedException(null, null, null);
		}
		return null;
	}

}