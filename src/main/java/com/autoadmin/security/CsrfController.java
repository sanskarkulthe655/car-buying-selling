package com.autoadmin.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
    @GetMapping("/api/csrf")
    public CsrfInfo token(CsrfToken token) {
        return new CsrfInfo(token.getHeaderName(), token.getToken());
    }

    public record CsrfInfo(String headerName, String token) { }
}
