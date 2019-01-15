package org.learning.javaconcurrency.disruptor;

import java.util.concurrent.Executors;

import javax.ws.rs.container.AsyncResponse;

import org.learning.javaconcurrency.service.JsonService;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by vkasiviswanathan on 1/6/19.
 */
@Component
public class DisruptorService {

	@SuppressWarnings("deprecation")
	private static final Disruptor<Event> DISRUPTOR = new Disruptor<>(Event::new, 1024,
			Executors.newFixedThreadPool(5, new CustomizableThreadFactory("Disruptor-Service-Pool-Size-5-")));

	private Producer producer = new Producer();

	static {
		DISRUPTOR
				.handleEventsWith((event, sequence, endOfBatch) -> event.posts = JsonService.getPosts(),
						(event, sequence, endOfBatch) -> event.comments = JsonService.getComments(),
						(event, sequence, endOfBatch) -> event.albums = JsonService.getAlbums(),
						(event, sequence, endOfBatch) -> event.photos = JsonService.getPhotos())
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
