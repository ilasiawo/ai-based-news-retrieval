package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EventType;
import com.example.demo.entity.UserEvent;
import com.example.demo.service.UserEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-events")
public class UserEventController {

    private final UserEventService userEventService;

    public UserEventController(UserEventService userEventService) {
        this.userEventService = userEventService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserEvent>> registerEvent(@RequestParam UUID articleId,
                                                                @RequestParam EventType eventType,
                                                                @RequestParam(required = false) Double userLatitude,
                                                                @RequestParam(required = false) Double userLongitude) {
        try {
            UserEvent event = new UserEvent(articleId, eventType, userLatitude, userLongitude);
            UserEvent savedEvent = userEventService.saveEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Event registered successfully", savedEvent));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to register event: " + e.getMessage(), null));
        }
    }
}
