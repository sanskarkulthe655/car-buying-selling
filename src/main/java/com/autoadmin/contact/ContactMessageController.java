package com.autoadmin.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactMessageController {
    private final ContactMessageRepository messages;

    public ContactMessageController(ContactMessageRepository messages) {
        this.messages = messages;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactMessage createMessage(@Valid @RequestBody ContactRequest request) {
        return messages.save(new ContactMessage(
            request.name().trim(), request.email().trim(), clean(request.phone()),
            request.subject().trim(), request.message().trim()
        ));
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record ContactRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Email @Size(max = 180) String email,
        @Size(max = 40) String phone,
        @NotBlank @Size(max = 160) String subject,
        @NotBlank @Size(max = 2000) String message
    ) { }
}
