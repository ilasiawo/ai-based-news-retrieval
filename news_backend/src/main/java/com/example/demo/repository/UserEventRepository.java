package com.example.demo.repository;

import com.example.demo.entity.UserEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, UUID> {
    
    List<UserEvent> findByArticleId(UUID articleId);

}
