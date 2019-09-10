package com.kris.intro.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class DemoController {

    @PostMapping("/out")
    public ResponseEntity outDemo(@RequestBody String body) {
        System.out.println("Out handler: [" + body + "]");

        return ResponseEntity
                .ok()
                .build();
    }
}
