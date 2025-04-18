package com.moetawol.book.feedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FeedBackRepository extends JpaRepository<Feedback, UUID> {
    @Query("""
            SELECT feedback
            FROM Feedback  feedback
            WHERE feedback.book.id = :bookId
""")
    Page<Feedback> findAllByBookId(@Param("bookId") UUID bookId, Pageable pageable);
}
