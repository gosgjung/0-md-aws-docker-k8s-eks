package io.study.gosgjung.docker_k8s_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/")
    public String index(){
        return "Hello World!";
    }

}
