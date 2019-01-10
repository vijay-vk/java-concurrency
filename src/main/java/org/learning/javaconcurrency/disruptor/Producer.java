package org.learning.javaconcurrency.disruptor;

import javax.ws.rs.container.AsyncResponse;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class Producer {

	public void publish(Disruptor<Event> disruptor, AsyncResponse response) {

		RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();
		long sequence = ringBuffer.next();

		try {
			Event event = ringBuffer.get(sequence);
			StringBuilder sb = new StringBuilder();
			event.setResponse(sb);
			event.setHttpResponse(response);
		} finally {
			ringBuffer.publish(sequence);
		}
	}
}
