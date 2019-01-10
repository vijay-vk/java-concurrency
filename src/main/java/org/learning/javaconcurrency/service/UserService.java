package org.learning.javaconcurrency.service;

import java.util.concurrent.TimeUnit;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
public class UserService {

    public static String getUserDetails() {

        try {
        	System.out.println("User Details - " + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        return "User Details - ";
    }
}
