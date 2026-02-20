package com.showoff.incidentops.springboot.async.dto;

public record ImpactScoreResponse(
    String incidentId,
    int severity,
    int impactScore,
    String calculatedAt,
    String calculatedByThread
) {}
