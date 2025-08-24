package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.comment.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "WHERE c.event.id = :eventId " +
            "ORDER BY c.createdOn DESC")
    Page<Comment> findByEventIdOrderByCreatedOnDesc(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.author.id = :authorId " +
            "ORDER BY c.createdOn DESC")
    Page<Comment> findByAuthorIdOrderByCreatedOnDesc(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    boolean existsByIdAndAuthorId(Long id, Long authorId);
}
