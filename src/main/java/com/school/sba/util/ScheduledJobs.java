package com.school.sba.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.school.sba.entity.AcademicProgram;
import com.school.sba.entity.ClassHour;
import com.school.sba.repository.AcademicProgramRepo;
import com.school.sba.service.AcademicProgramService;
import com.school.sba.service.ClassHourService;
import com.school.sba.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduledJobs {

	@Autowired
	private UserService userServ;
	
	@Autowired
	private AcademicProgramRepo programRepo;
	
	@Autowired
	ClassHourService classHourService;
	
	@Autowired
	 AcademicProgramService programServ;

	@Scheduled(fixedDelay = 10000L)	
	public void delete() {
		log.info("Executing Scheduler jobs");
		String deleteUser =userServ.deleteUser();
		log.info(deleteUser);
		
		log.info(programServ.deleteAcademicProgram());
	}
	// <sec> <MIn> <Hour> <day Of Month> <month> <day Of Week>
	@Scheduled(cron = " 0 0 0 * * MON")
	public void generateClassHours() {
		
		List<AcademicProgram> findAll = programRepo.findAll();
		findAll.forEach((ac)-> {
			if(ac.isAutoRepeat()) {
				classHourService.craeteClassHoursForNextWeek(ac.getProgramId());
			}
		});
		
	}
}
