package org.learning.javaconcurrency.service;

import java.util.concurrent.TimeUnit;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
public class ActivityService {

	public static String getActivityDetails() {

		try {
			System.out.println("Activity Details - " + Thread.currentThread().getName());
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
		return "Activity Details - ";
	}
}
