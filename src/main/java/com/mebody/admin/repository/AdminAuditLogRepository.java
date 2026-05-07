package com.mebody.admin.repository;

import com.mebody.admin.domain.AdminAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {
}
