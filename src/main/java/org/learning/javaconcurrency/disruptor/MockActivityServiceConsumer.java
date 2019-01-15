package org.learning.javaconcurrency.disruptor;

import org.learning.javaconcurrency.service.MockActivityService;

import com.lmax.disruptor.EventHandler;

public class MockActivityServiceConsumer implements EventHandler<MockEvent> {

	private int n;

	public MockActivityServiceConsumer(int n) {
		this.n = n;
	}

	@Override
	public void onEvent(MockEvent event, long sequence, boolean endOfBatch) throws Exception {

		if (sequence % 2 == n) {
			event.setActivityDetails(MockActivityService.getActivityDetails());
		}
		System.out.println("Activity n : " + n);
	}
}
