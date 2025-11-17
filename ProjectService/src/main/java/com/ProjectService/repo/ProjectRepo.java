package com.ProjectService.repo;

import com.ProjectService.entity.Project;
import com.ProjectService.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long> {

    // find single project by title (titles might not be unique — change to Optional<Project> or List<Project> per your domain)
    Optional<Project> findByTitle(String title);

    // find all projects by status
    List<Project> findByStatus(ProjectStatus status);

    // paged find of projects for a client (we store clientId in Project)
    Page<Project> findByClientId(Long clientId, Pageable pageable);

    // list find of projects for a client (non-paged)
    List<Project> findByClientId(Long clientId);

    // count projects managed by a particular manager (managerId stored as Long)
    int countByManagerId(Long managerId);

    // find projects by manager id (returns list)
    List<Project> findByManagerId(Long managerId);

	Page<Project> findByManagerId(Long managerId, Pageable pageable);
}
