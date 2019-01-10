package org.learning.javaconcurrency.disruptor;

import org.learning.javaconcurrency.service.UserService;

import com.lmax.disruptor.EventHandler;

public class UserServiceConsumer implements EventHandler<Event> {

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		
		event.setUserDetails(UserService.getUserDetails());
	}
}
