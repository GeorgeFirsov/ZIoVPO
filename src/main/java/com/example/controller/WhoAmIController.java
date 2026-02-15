package com.example.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhoAmIController {

    @GetMapping("/whoami")
    public String whoami(Authentication auth) {
        if (auth == null) {
            return "no auth";
        }
        return "username=" + auth.getName() + ", roles=" + auth.getAuthorities();
    }
}
