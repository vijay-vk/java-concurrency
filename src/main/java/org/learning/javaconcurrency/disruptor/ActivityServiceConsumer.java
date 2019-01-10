package org.learning.javaconcurrency.disruptor;

import org.learning.javaconcurrency.service.ActivityService;

import com.lmax.disruptor.EventHandler;

public class ActivityServiceConsumer implements EventHandler<Event> {

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		event.setActivityDetails(ActivityService.getActivityDetails());
	}
}
