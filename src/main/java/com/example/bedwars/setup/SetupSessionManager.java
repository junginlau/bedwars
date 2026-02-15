package com.example.bedwars.setup;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetupSessionManager {
    private final Map<UUID, SetupSession> sessions = new HashMap<>();

    public SetupSession getSession(UUID uuid) {
        return sessions.computeIfAbsent(uuid, key -> new SetupSession());
    }

    public void clear(UUID uuid) {
        sessions.remove(uuid);
    }
}
