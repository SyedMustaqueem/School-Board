package com.school.sba.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.school.sba.entity.Subject;
import com.school.sba.entity.User;
import com.school.sba.requestdto.SubjectRequest;
import com.school.sba.responsedto.AcademicProgramResponse;
import com.school.sba.util.ResponseStructure;

public interface SubjectService {

	ResponseEntity<ResponseStructure<AcademicProgramResponse>> insertSubjectList(SubjectRequest subjectRequests,
			int programId);

	ResponseEntity<ResponseStructure<List<Subject>>> fetchAllSubjects();

	ResponseEntity<ResponseStructure<User>> mapSubjectToTeacher(int subjectId, int userId);

}
