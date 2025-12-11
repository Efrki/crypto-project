package com.example.crypto_project.controller;

import com.example.crypto_project.model.Contact;
import com.example.crypto_project.model.User;
import com.example.crypto_project.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/request")
    public ResponseEntity<String> sendContactRequest(@RequestParam String toUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String fromUsername = authentication.getName();

        contactService.sendContactRequest(fromUsername, toUsername);
        return ResponseEntity.ok("Contact request sent to " + toUsername);
    }

    @PostMapping("/accept/{contactId}")
    public ResponseEntity<String> acceptContactRequest(@PathVariable Long contactId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        contactService.acceptContactRequest(contactId, currentUsername);
        return ResponseEntity.ok("Contact request accepted.");
    }

    @DeleteMapping("/decline-or-delete/{contactId}")
    public ResponseEntity<String> declineOrDeleteContact(@PathVariable Long contactId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        contactService.declineOrDeleteContact(contactId, currentUsername);
        return ResponseEntity.ok("Contact request declined or contact deleted.");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<String>> getPendingRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Contact> pendingRequests = contactService.getPendingRequests(authentication.getName());
        List<String> requestSenders = pendingRequests.stream()
                .map(contact -> "Request ID: " + contact.getId() + ", From: " + contact.getUser().getLogin())
                .collect(Collectors.toList());
        return ResponseEntity.ok(requestSenders);
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<String>> getAcceptedContacts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<User> contacts = contactService.getAcceptedContacts(authentication.getName());
        List<String> contactLogins = contacts.stream().map(User::getLogin).collect(Collectors.toList());
        return ResponseEntity.ok(contactLogins);
    }
}