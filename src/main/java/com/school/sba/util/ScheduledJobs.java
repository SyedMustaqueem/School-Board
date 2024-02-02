package com.school.sba.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.school.sba.service.AcademicProgramService;
import com.school.sba.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduledJobs {

	@Autowired
	private UserService userServ;
	
	@Autowired
	private AcademicProgramService programServ;

	@Scheduled(fixedDelay = 10000L)	
	public void delete() {
		log.info("Executing Scheduler jobs");
		String deleteUser =userServ.deleteUser();
		log.info(deleteUser);
		
		log.info(programServ.deleteAcademicProgram());
	}

}
