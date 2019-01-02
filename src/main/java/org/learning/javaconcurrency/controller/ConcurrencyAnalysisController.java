package org.learning.javaconcurrency.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by vkasiviswanathan on 1/2/19.
 */
@RestController
public class ConcurrencyAnalysisController {

    @RequestMapping("/executor-service")
    public String analyseExecutorService() {
        return "executor-service";
    }
}
