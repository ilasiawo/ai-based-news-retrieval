package com.example.demo.service;

import com.example.demo.entity.UserEvent;
import com.example.demo.repository.UserEventRepository;
import org.springframework.stereotype.Service;


@Service
public class UserEventService {

    private final UserEventRepository userEventRepository;
    private final RedisTrendingService redisTrendingService;

    public UserEventService(UserEventRepository userEventRepository,
                           RedisTrendingService redisTrendingService) {
        this.userEventRepository = userEventRepository;
        this.redisTrendingService = redisTrendingService;
    }

    public UserEvent saveEvent(UserEvent event) {
        // Save to database
        UserEvent savedEvent = userEventRepository.save(event);
        
        // Update Redis trending scores
        if (event.getUserLatitude() != null && event.getUserLongitude() != null) {
            redisTrendingService.updateTrendingScore(
                event.getArticleId(), 
                event.getEventType(), 
                event.getUserLatitude(), 
                event.getUserLongitude()
            );
        }
        
        return savedEvent;
    }
}
