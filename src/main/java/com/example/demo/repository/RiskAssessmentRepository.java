package com.example.demo.repository;

import com.example.demo.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
}