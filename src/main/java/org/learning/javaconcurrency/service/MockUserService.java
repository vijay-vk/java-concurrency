package org.learning.javaconcurrency.service;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
public class MockUserService {

	public static String getUserDetails() {
		long startTime = System.currentTimeMillis();
		RandomOperation.getNewIntList();
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		System.out.println("Time taken to get UserDetails is :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());

		return "User Details - ";
	}
}
