package org.learning.javaconcurrency.service;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
public class ActivityService {

	public static String getActivityDetails() {

		long startTime = System.currentTimeMillis();
		RandomOperation.sortLong();
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		System.out.println("Time taken to get ActivityDetails is :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());
		return "Activity Details - ";
	}
}
