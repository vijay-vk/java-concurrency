package org.learning.javaconcurrency.disruptor;

import org.learning.javaconcurrency.service.JsonService;

import com.lmax.disruptor.EventHandler;

public class JsonPostServiceConsumer implements EventHandler<Event> {

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {

		event.posts = JsonService.getPosts();
	}
}
