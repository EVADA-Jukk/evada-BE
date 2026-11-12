package com.evada.feedback.repository;

import com.evada.feedback.entity.Feedback;
import com.evada.feedback.entity.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findByType(FeedbackType type, Pageable pageable);
}
