package com.devops.idcard.repository;

import com.devops.idcard.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    // For listing, searching, and checking existence of Templates in the DB
    List<Template> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);
}
