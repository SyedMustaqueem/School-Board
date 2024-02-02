package com.school.sba.serviceImpl;

import java.time.Duration;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.Schedule;
import com.school.sba.exception.ConstraintViolationException;
import com.school.sba.exception.ScheduleNotFoundException;
import com.school.sba.exception.SchoolNotFoundException;
import com.school.sba.repository.ScheduleRepo;
import com.school.sba.repository.SchoolRepo;
import com.school.sba.requestdto.ScheduleRequest;
import com.school.sba.responsedto.ScheduleResponse;
import com.school.sba.util.ResponseStructure;

@Service
public class ScheduleServiceImpl implements com.school.sba.service.ScheduleService {

	@Autowired
	SchoolRepo schoolRepo;

	@Autowired
	ScheduleRepo scheduleRepo;

	@Autowired
	ResponseStructure<ScheduleResponse> structure;

	@Override
	public ResponseEntity<ResponseStructure<ScheduleResponse>> saveSchedule(ScheduleRequest scheduleRequest,
			int schoolId) {
		return schoolRepo.findById(schoolId).map(school -> {
			if (school.getSchedule() == null) {
				Schedule schedule = scheduleRepo.save(mapToSchedule(scheduleRequest));
				school.setSchedule(schedule);
				
				schoolRepo.save(school);
				structure.setData(mapToScheduleResponse(schedule));
				structure.setMessage("Schedule saved to database");
				structure.setStatus(HttpStatus.CREATED.value());
				return new ResponseEntity<ResponseStructure<ScheduleResponse>>(structure, HttpStatus.CREATED);
			} else {
				throw new ScheduleNotFoundException("Schedule already present in database", HttpStatus.BAD_REQUEST,
						"More than 1 schedule not allowed in database");
			}

		}).orElseThrow(() -> new SchoolNotFoundException("School with given ID is not registered in the database",
				HttpStatus.NOT_FOUND, "No such School in database"));
	}

	
	public boolean validateSchdeule(Schedule schedule) {
		LocalTime opensAt = schedule.getOpensAt();
		LocalTime closesAt = schedule.getClosesAt();
		Duration classHourInMinutes = schedule.getClassHourInMinutes();
		LocalTime breakTime = schedule.getBreakTime();
		Duration breakLengthInMinutes = schedule.getBreakLengthInMinutes();
		LocalTime lunchTime = schedule.getLunchTime();
		Duration lunchLength = schedule.getLunchLengthInMinutes();

		// it is Showing total class per day
		int totalClassPerDay = schedule.getClassHoursPerDay();

		int count = 0;
		LocalTime classBeginAt = opensAt;
		LocalTime classEndsAt = opensAt;

		// finding total class[Note: 1 class hour timing is 1hr or 60 minute]
		if (((totalClassPerDay * classHourInMinutes.toMinutes()) / 60) + (breakLengthInMinutes.toMinutes() / 60)
				+ (lunchLength.toMinutes()) / 60 == (Duration.between(opensAt, closesAt).toMinutes()) / 60) {

			// +2 is for lunch time and break time,
			for (int i = 0; i < totalClassPerDay + 2; i++) {

				// We are adding 1hour
				classEndsAt = classEndsAt.plusHours(classHourInMinutes.toHours());
				if (classEndsAt.equals(breakTime)) {
					count++;
					classEndsAt = classEndsAt.plusMinutes(breakLengthInMinutes.toMinutes());
					
				} else if (classEndsAt.equals(lunchTime)) {
					count++;
					classEndsAt = classEndsAt.plusHours(lunchLength.toHours());
					
				} else {
					classBeginAt = classEndsAt;
				}
			}
			if (count == 2) {
				return true;
			} else {
				return false;
			}
		}

		else {
			throw new ConstraintViolationException("Wrong class hour", HttpStatus.NOT_ACCEPTABLE, null);
		}
	}
	
	@Override
	public ResponseEntity<ResponseStructure<ScheduleResponse>> getSchedule(int schoolId) {
		return schoolRepo.findById(schoolId).map(school -> {
			Schedule schedule = school.getSchedule();
			structure.setData(mapToScheduleResponse(schedule));
			structure.setMessage("Schedule fetched from database");
			structure.setStatus(HttpStatus.CREATED.value());
			return new ResponseEntity<ResponseStructure<ScheduleResponse>>(structure, HttpStatus.CREATED);
		}).orElseThrow(() -> new SchoolNotFoundException("School with given ID is not registered in the database",
				HttpStatus.NOT_FOUND, "No such School in database"));
	}



	@Override
	public ResponseEntity<ResponseStructure<ScheduleResponse>> updateSchedule(int schoolId,
			ScheduleRequest scheduleRequest) {
		return scheduleRepo.findById(schoolId).map(s -> {
			s = mapToSchedule(scheduleRequest);
			s.setScheduleId(1);
			Schedule schedule = scheduleRepo.save(s);
			structure.setData(mapToScheduleResponse(schedule));
			structure.setMessage("Schedule updated in database");
			structure.setStatus(HttpStatus.CREATED.value());
			
			return new ResponseEntity<ResponseStructure<ScheduleResponse>>(structure, HttpStatus.FOUND);
		}).orElseThrow(() -> new ScheduleNotFoundException("Schedule with given ID is not registered in the database",
				HttpStatus.NOT_FOUND, "No such schedule in database"));

	}

	private ScheduleResponse mapToScheduleResponse(Schedule schedule) {
		return ScheduleResponse.builder().scheduleId(schedule.getScheduleId()).opensAt(schedule.getOpensAt())
				.closesAt(schedule.getClosesAt()).classHoursPerDay(schedule.getClassHoursPerDay())
				.classHourInMinutes(schedule.getClassHourInMinutes()).breakTime(schedule.getBreakTime())
				.breakLengthInMinutes(schedule.getBreakLengthInMinutes())
				.lunchLengthInMinutes(schedule.getLunchLengthInMinutes()).lunchTime(schedule.getLunchTime()).build();
	}

	private Schedule mapToSchedule(ScheduleRequest scheduleRequest) {
		return Schedule.builder().opensAt(scheduleRequest.getOpensAt()).closesAt(scheduleRequest.getClosesAt())
				.classHoursPerDay(scheduleRequest.getClassHoursPerDay())
				.classHourInMinutes(Duration.ofMinutes(scheduleRequest.getClassHourInMinutes()))
				.breakTime(scheduleRequest.getBreakTime())
				.breakLengthInMinutes(Duration.ofMinutes(scheduleRequest.getBreakLengthInMinutes()))
				.lunchLengthInMinutes(Duration.ofMinutes(scheduleRequest.getLunchLengthInMinutes()))
				.lunchTime(scheduleRequest.getLunchTime()).lunchTime(scheduleRequest.getLunchTime()).build();
	}
	
	@Override
	public Object deleteSchedule(Schedule schedule) {
		scheduleRepo.delete(schedule);
		return "Schedule Deleted";
	}

}