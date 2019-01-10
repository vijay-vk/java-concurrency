package org.learning.javaconcurrency.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.learning.javaconcurrency.disruptor.DisruptorService;
import org.learning.javaconcurrency.executor.BasicExecutorService;
import org.learning.javaconcurrency.sequential.SequentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Created by vkasiviswanathan on 1/2/19.
 */
@Controller
@Path("")
public class ConcurrencyAnalysisController {

    @Autowired
    SequentialService sequentialService;

    @Autowired
    BasicExecutorService basicExecutorService;

    @Autowired
    DisruptorService disruptorService;

    @GET
	@Path("/sequential-processing")
    public String analyseSequentialProcessing() {
        return sequentialService.getResponse();
    }

    @GET
	@Path("/executor-service")
    public String analyseExecutorService() {
        return basicExecutorService.getResponse();
    }
    
	@GET
	@Path("/disruptor")
    public void analyseDisruptor(@Suspended AsyncResponse response) {
        disruptorService.sendAsyncResponse(response);
    }
}
