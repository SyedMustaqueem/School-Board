package com.school.sba;

import java.time.Duration;
import java.time.LocalTime;

public class Dummy {

	public static void main(String[] args) {
		LocalTime opensAt = LocalTime.of(9, 0, 0); // 09:00
		LocalTime closesAt = LocalTime.of(16, 30, 0); // 16:30
		double numberOfHours = Duration.between(opensAt, closesAt)	.toMinutes();
		numberOfHours = numberOfHours / 60;
		System.out.println(numberOfHours);
	}
}
