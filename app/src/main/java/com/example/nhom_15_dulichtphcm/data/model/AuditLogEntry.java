package com.example.nhom_15_dulichtphcm.data.model;

public final class AuditLogEntry {
    public final String id;
    public final String actorUsername;
    public final UserRole actorRole;
    public final String actionVi;
    public final String actionEn;
    public final long timestamp;

    public AuditLogEntry(
            String id,
            String actorUsername,
            UserRole actorRole,
            String actionVi,
            String actionEn,
            long timestamp
    ) {
        this.id = id != null ? id : "";
        this.actorUsername = actorUsername != null ? actorUsername : "guest";
        this.actorRole = actorRole != null ? actorRole : UserRole.GUEST;
        this.actionVi = actionVi != null ? actionVi : "";
        this.actionEn = actionEn != null ? actionEn : "";
        this.timestamp = timestamp;
    }
}
