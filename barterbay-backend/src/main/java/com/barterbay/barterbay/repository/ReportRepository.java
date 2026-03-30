package com.barterbay.barterbay.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.barterbay.barterbay.model.Report;

public interface ReportRepository extends MongoRepository<Report, String> {

    List<Report> findByReportedUser(String reportedUser);
}