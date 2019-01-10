package org.learning.javaconcurrency;

import org.glassfish.jersey.server.ResourceConfig;
import org.learning.javaconcurrency.controller.ConcurrencyAnalysisController;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		register(ConcurrencyAnalysisController.class);
	}
}
