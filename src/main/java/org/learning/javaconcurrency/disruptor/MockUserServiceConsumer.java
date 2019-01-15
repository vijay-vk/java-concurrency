package org.learning.javaconcurrency.disruptor;

import org.learning.javaconcurrency.service.MockUserService;

import com.lmax.disruptor.EventHandler;

public class MockUserServiceConsumer implements EventHandler<MockEvent> {
	private int n;

	public MockUserServiceConsumer(int n) {
		this.n = n;
	}

	@Override
	public void onEvent(MockEvent event, long sequence, boolean endOfBatch) throws Exception {

		if (sequence % 2 == n) {
			event.setUserDetails(MockUserService.getUserDetails());
		}
		System.out.println("User n : " + n);
	}
}
