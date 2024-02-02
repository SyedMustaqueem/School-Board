package com.school.sba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.school.sba.requestdto.ScheduleRequest;
import com.school.sba.responsedto.ScheduleResponse;
import com.school.sba.service.ScheduleService;
import com.school.sba.util.ResponseStructure;

@RestController
public class ScheduleConroller {

	@Autowired
	private ScheduleService service;
	
	@PostMapping(path = "/schools/{schoolId}/schedules")
	public ResponseEntity<ResponseStructure<ScheduleResponse>> saveSchedule(@PathVariable int schoolId,@RequestBody ScheduleRequest scheduleRequest) {
		return service.saveSchedule(scheduleRequest,schoolId);
	}
	
	@GetMapping(path = "/schools/{schoolId}/schedules")
	public ResponseEntity<ResponseStructure<ScheduleResponse>> getSchedule(@PathVariable int schoolId){
		return service.getSchedule(schoolId);
	}
	
	@PutMapping(path = "/schedules/{scheduleId}")
	public ResponseEntity<ResponseStructure<ScheduleResponse>> updateSchedule(@PathVariable int scheduleId,@RequestBody ScheduleRequest scheduleRequest) {
		return service.updateSchedule(scheduleId,scheduleRequest);
	}
}
