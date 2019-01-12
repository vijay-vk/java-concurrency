package org.learning.javaconcurrency.disruptor;

import java.util.concurrent.Executors;

import javax.ws.rs.container.AsyncResponse;

import org.springframework.stereotype.Component;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class DisruptorService {

	private static final Disruptor<Event> DISRUPTOR = new Disruptor<>(Event::new, 1024,
			Executors.newFixedThreadPool(3));
	
	private Producer producer = new Producer();

	static {
		DISRUPTOR.handleEventsWith(new UserServiceConsumer(), new ActivityServiceConsumer())
				.then(new ResponseBuilder());
		DISRUPTOR.start();
	}

	public void sendAsyncResponse(AsyncResponse response) {

		producer.publish(DISRUPTOR, response);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("inside finalize");
		DISRUPTOR.shutdown();
	}
}
