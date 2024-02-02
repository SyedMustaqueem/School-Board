package com.school.sba.serviceImpl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.school.sba.entity.AcademicProgram;
import com.school.sba.entity.ClassHour;
import com.school.sba.entity.Schedule;
import com.school.sba.entity.School;
import com.school.sba.entity.Subject;
import com.school.sba.entity.User;
import com.school.sba.enums.ClassStatus;
import com.school.sba.enums.UserRole;
import com.school.sba.exception.AcademinProgrammeException;
import com.school.sba.exception.ClassHoursNotFoundException;
import com.school.sba.exception.ConstraintViolationException;
import com.school.sba.exception.SchoolNotFoundException;
import com.school.sba.exception.UserNotFoundException;
import com.school.sba.repository.AcademicProgramRepo;
import com.school.sba.repository.ClassHourRepo;
import com.school.sba.repository.SubjectRepo;
import com.school.sba.repository.UserRepo;
import com.school.sba.requestdto.ClassHourUpdateRequest;
import com.school.sba.service.ClassHourService;
import com.school.sba.util.ResponseStructure;

@Service
public class ClassHourServiceImpl implements ClassHourService {

	@Autowired
	AcademicProgramRepo academicProgramRepository;

	@Autowired
	ClassHourRepo classHourRepo;

	@Autowired
	SubjectRepo subjectRepo;

	@Autowired
	UserRepo userRepo;

	@Autowired
	ResponseStructure<List<ClassHour>> hourStructure;
	
	@Autowired
	ResponseStructure<String> structure;

	private boolean isBreakTime(LocalDateTime beginsAt, LocalDateTime endsAt, Schedule schedule) {
		LocalTime breakTimeStart = schedule.getBreakTime();

		return ((breakTimeStart.isAfter(beginsAt.toLocalTime()) && breakTimeStart.isBefore(endsAt.toLocalTime()))
				|| breakTimeStart.equals(beginsAt.toLocalTime()));
	}

	private boolean isLunchTime(LocalDateTime beginsAt, LocalDateTime endsAt, Schedule schedule) {
		LocalTime lunchTimeStart = schedule.getLunchTime();

		return ((lunchTimeStart.isAfter(beginsAt.toLocalTime()) && lunchTimeStart.isBefore(endsAt.toLocalTime()))
				|| lunchTimeStart.equals(beginsAt.toLocalTime()));
	}

	//  if (classHour.getBeginsAt().getDayOfWeek() != DayOfWeek.SUNDAY) {	
	
	@Override
	public ResponseEntity<ResponseStructure<String>> registerClassHour(int programId) {
        return academicProgramRepository.findById(programId).map(academicProgram -> {
            School school = academicProgram.getSchool();
            Schedule schedule = school.getSchedule();
            if (schedule != null) {
                int classHourPerDay = schedule.getClassHoursPerDay();
                int classHourLength = (int) schedule.getClassHourInMinutes().toMinutes();

                LocalDateTime currentTime = LocalDateTime.now().with(schedule.getOpensAt());

                LocalDateTime lunchTimeStart = LocalDateTime.now().with(schedule.getLunchTime());
                LocalDateTime lunchTimeEnd = lunchTimeStart.plusMinutes(schedule.getLunchLengthInMinutes().toMinutes());
                LocalDateTime breakTimeStart = LocalDateTime.now().with(schedule.getBreakTime());
                LocalDateTime breakTimeEnd = breakTimeStart.plusMinutes(schedule.getBreakLengthInMinutes().toMinutes());


                int remainingDays = 7 - currentTime.getDayOfWeek().getValue();

                for (int day = 1; day <= remainingDays; day++) {
                    if (currentTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        currentTime = currentTime.plusDays(1);
                        continue; // Skip generating class hours on Sundays
                    }

                    for (int hour = 1; hour <= classHourPerDay + 2; hour++) {
                        ClassHour classHour = new ClassHour();
                        LocalDateTime beginsAt = currentTime;
                        LocalDateTime endsAt = beginsAt.plusMinutes(classHourLength);
			
			if (!isLunchTime(beginsAt, endsAt, schedule)) {
                            if (!isBreakTime(beginsAt, endsAt, schedule)) {
                                classHour.setBeginsAt(beginsAt);
                                classHour.setEndsAt(endsAt);
                                classHour.setClassStatus(ClassStatus.NOT_SCHEDULED);

                                currentTime = endsAt;
                            } else {
                                classHour.setBeginsAt(breakTimeStart);
                                classHour.setEndsAt(breakTimeEnd);
                                classHour.setClassStatus(ClassStatus.BREAK_TIME);
                                currentTime = breakTimeEnd;
                            }
                        } else {
                            classHour.setBeginsAt(lunchTimeStart);
                            classHour.setEndsAt(lunchTimeEnd);
                            classHour.setClassStatus(ClassStatus.LUNCH_TIME);
                            currentTime = lunchTimeEnd;
                        }
                       
                        System.out.println("Skipping class hour generation for day: " + currentTime.getDayOfWeek());

                        currentTime = endsAt; // Update currentTime for further days
                    }
                    currentTime = currentTime.plusDays(1).with(schedule.getOpensAt());
                }

                structure.setData("ClassHour generated successfully for the academic program");
                structure.setMessage("Class Hour generated for the current week successfully");
                structure.setStatus(HttpStatus.CREATED.value());
                return new ResponseEntity<>(structure, HttpStatus.CREATED);
            } else {
                throw new SchoolNotFoundException(
                        "The school does not contain any schedule, please provide a schedule to the school", null,
                        null);
            }
        }).orElseThrow(() -> new AcademinProgrammeException("Invalid Program Id", HttpStatus.BAD_REQUEST, ""));
    }



	@Override
	public Object updateClassHour(List<ClassHourUpdateRequest> updateRequests) {
		updateRequests.forEach((req) -> {
			int userId = req.getUserId();
			User user = userRepo.findById(userId)
					.orElseThrow(() -> new UserNotFoundException("User with given ID is not registered in the database",
							HttpStatus.NOT_FOUND, "No such user in database"));
			int roomNo = req.getRoomNo();
			int hourId = req.getClassHourId();
			ClassHour classHour = classHourRepo.findById(hourId).orElseThrow(
					() -> new UserNotFoundException("ClassHour with given ID is not registered in the database",
							HttpStatus.NOT_FOUND, "No such ClassHour in database"));
			int subjectId = req.getSubjectId();
			Subject subject = subjectRepo.findById(subjectId).orElseThrow(
					() -> new UserNotFoundException("Subject with given ID is not registered in the database",
							HttpStatus.NOT_FOUND, "No such Subject in database"));
			if (!classHourRepo.existsByRoomNoAndBeginsAtBetween(roomNo, classHour.getBeginsAt().minusMinutes(1),
					classHour.getEndsAt().plusMinutes(1))) {
				if (user.getUserRole().equals(UserRole.TEACHER)) {
					classHour.setRoomNo(roomNo);
					classHour.setSubjects(subject);
					classHour.setUser(user);
					classHourRepo.save(classHour);
				} else {
					throw new ConstraintViolationException("Invalid User Id", HttpStatus.BAD_REQUEST, "");
				}
			} else {
				throw new ClassHoursNotFoundException("Class Hour already contains Room No", HttpStatus.BAD_REQUEST, "");
			}
		});
		return "ClassHour updated";
	}

	
	@Override
	public ResponseEntity<ResponseStructure<List<ClassHour>>> craeteClassHoursForNextWeek(int programId) {
		AcademicProgram academicProgram = academicProgramRepository.findById(programId).get();
		List<ClassHour> classHours = academicProgram.getClassHours();
		classHours.forEach((cl) -> {
			// createNewClassHour(ClassHour classHour) is down side we created
			ClassHour createNewClassHour = createNewClassHour(cl);
			classHours.add(createNewClassHour);
		});

		classHours.forEach((hour) -> {
			LocalDateTime plusDays = hour.getBeginsAt().plusDays(7);
			hour.setBeginsAt(plusDays);
			classHourRepo.save(hour);
		});
		hourStructure.setMessage("New Class Hour Created For Next Week");
		hourStructure.setStatus(HttpStatus.CREATED.value());
		structure.setData("Class Hour Generated");
		return new ResponseEntity<ResponseStructure<List<ClassHour>>>(hourStructure, HttpStatus.CREATED);	
	}

	private ClassHour createNewClassHour(ClassHour cl) {
		ClassHour classHour2 = new ClassHour();

		classHour2.setAcademicProgram(cl.getAcademicProgram());
		classHour2.setBeginsAt(cl.getBeginsAt());
		classHour2.setClassStatus(cl.getClassStatus());
		classHour2.setEndsAt(cl.getEndsAt());
		classHour2.setRoomNo(cl.getRoomNo());
		classHour2.setSubjects(cl.getSubjects());
		classHour2.setUser(cl.getUser());

		return classHour2;
	}

	@Override
	public Object deleteClassHours(List<ClassHour> classHours) {
		// TODO Auto-generated method stub
		return "soft Delete";
	}
	
	



}