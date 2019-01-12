package org.learning.javaconcurrency.sequential;

import org.learning.javaconcurrency.service.ActivityService;
import org.learning.javaconcurrency.service.UserService;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class SequentialService {

	public String getResponse() {
		long startTime = System.currentTimeMillis();
		String response = UserService.getUserDetails() + ActivityService.getActivityDetails();
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		System.out.println("Time taken to build response from SequentialService :: " + timeTaken + " - in Thread "
				+ Thread.currentThread().getName());

		return response;
	}
}
