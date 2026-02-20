package com.showoff.incidentops.springboot.persistence.repository;

import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IncidentTicketRepository extends JpaRepository<IncidentTicketEntity, Long> {
    Optional<IncidentTicketEntity> findByTicketId(String ticketId);

    Page<IncidentTicketEntity> findByStatusOrderBySeverityDescTicketIdAsc(String status, Pageable pageable);

    @Query("""
        select t
        from IncidentTicketEntity t
        where lower(t.serviceId) = lower(:serviceId)
          and t.severity >= :minSeverity
        order by t.severity desc, t.ticketId asc
        """)
    Page<IncidentTicketEntity> findByServiceAndMinSeverity(
        @Param("serviceId") String serviceId,
        @Param("minSeverity") int minSeverity,
        Pageable pageable
    );
}
