package org.learning.javaconcurrency.sequential;

import java.util.concurrent.TimeUnit;

import org.learning.javaconcurrency.service.ActivityService;
import org.learning.javaconcurrency.service.UserService;
import org.springframework.stereotype.Component;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class SequentialService {

    public String getResponse() {
    	// time to build response
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return UserService.getUserDetails() + ActivityService.getActivityDetails();
    }
}
