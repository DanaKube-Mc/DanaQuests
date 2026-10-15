package su.nightexpress.quests.event.data;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record LeaderboardEntry(
    @NotNull UUID playerUuid,
    long score
) {}
