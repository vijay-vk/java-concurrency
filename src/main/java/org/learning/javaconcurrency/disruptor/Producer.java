package org.learning.javaconcurrency.disruptor;

import javax.ws.rs.container.AsyncResponse;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class Producer {

	public void publish(Disruptor<Event> disruptor, AsyncResponse httpResponse) {

		RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();
		long sequence = ringBuffer.next();

		try {
			Event event = ringBuffer.get(sequence);
			event.startTime = System.currentTimeMillis();
			event.httpResponse = httpResponse;
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	public void publish(Disruptor<Event> disruptor, Event eventFromCompletableFuture) {

		RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();
		long sequence = ringBuffer.next();

		try {
			Event event = ringBuffer.get(sequence);
			event.httpResponse = eventFromCompletableFuture.httpResponse;
			event.posts = eventFromCompletableFuture.posts;
			event.comments = eventFromCompletableFuture.comments;
			event.albums = eventFromCompletableFuture.albums;
			event.photos = eventFromCompletableFuture.photos;
		} finally {
			ringBuffer.publish(sequence);
		}
	}
}
