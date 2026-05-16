package com.example.demo.repository;

import com.example.demo.entity.TransactionEntity;
import com.example.demo.entity.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	Optional<TransactionEntity> findByIdAndUserId(Long id, Long userId);

	Optional<TransactionEntity> findTopByUserAndCreatedAtBeforeOrderByCreatedAtDesc(User user, Instant before);

	long countByUserAndCreatedAtAfter(User user, Instant createdAt);

	@Query("select coalesce(sum(t.amount), 0) from TransactionEntity t where t.user = :user and t.createdAt >= :since")
	BigDecimal sumAmountSince(@Param("user") User user, @Param("since") Instant since);
}