package com.school.sba.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.sba.entity.School;
import java.util.List;


public interface SchoolRepo extends JpaRepository<School, Integer> {

	boolean existsBySchoolId(int schoolId);
	
	List<School> findByIsDeleted(boolean deleted);
}