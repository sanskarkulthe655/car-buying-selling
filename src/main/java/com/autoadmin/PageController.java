package com.autoadmin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping({"/", "/services", "/contact", "/new-cars", "/used-cars", "/admin"})
    public String app() {
        return "forward:/index.html";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "forward:/admin-login.html";
    }
}
