package com.school.sba.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.sba.entity.AcademicProgram;
import java.util.List;


public interface AcademicProgramRepo extends JpaRepository<AcademicProgram, Integer> {

//	@Query("select users from AcademicProgram where programId=?1 and users=(select * from User where userRole=?2)")
//	List<User> findByProgramIdAndUserRole(int programId,UserRole userRole);
	
	List<AcademicProgram> findByIsDeleted(boolean deleted);
	
}