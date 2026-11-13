package com.horizonbank.homebanking.shared.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogger {

    private final JdbcClient jdbc;

    @Transactional(propagation = Propagation.MANDATORY)
    public void log(UUID actorUserId, String action, String entityType,
                    String entityId, Map<String, String> details) {
        jdbc.sql("""
                        insert into audit_log (actor_user_id, action, entity_type, entity_id, payload)
                        values (:actor, :action, :entityType, :entityId, cast(:payload as jsonb))
                        """)
                .param("actor", actorUserId)
                .param("action", action)
                .param("entityType", entityType)
                .param("entityId", entityId)
                .param("payload", toJson(details))
                .update();
    }

    private String toJson(Map<String, String> details) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : new TreeMap<>(details).entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(entry.getKey())).append("\":\"")
                    .append(escape(entry.getValue())).append('"');
        }
        return json.append('}').toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}