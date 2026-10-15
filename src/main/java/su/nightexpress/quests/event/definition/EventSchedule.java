package su.nightexpress.quests.event.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventSchedule {

    public enum Mode {
        CALENDAR,
        DURATION
    }

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Mode mode;
    private long startTimestamp;
    private long endTimestamp;
    private int durationHours;
    private boolean manuallyStopped;

    public EventSchedule(@NotNull Mode mode, long startTimestamp, long endTimestamp, int durationHours) {
        this.mode = mode;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.durationHours = durationHours;
        this.manuallyStopped = false;
    }

    @NotNull
    public static EventSchedule createCalendar(@NotNull String startDateStr, @NotNull String endDateStr) {
        long start = parseDateTime(startDateStr);
        long end = parseDateTime(endDateStr);
        return new EventSchedule(Mode.CALENDAR, start, end, 0);
    }

    @NotNull
    public static EventSchedule createDuration(int durationHours) {
        return new EventSchedule(Mode.DURATION, 0L, 0L, durationHours);
    }

    public static long parseDateTime(@NotNull String dateTimeStr) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(dateTimeStr.trim(), DATE_FORMATTER);
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }

    public void start(long startTime, int durationHours) {
        this.startTimestamp = startTime;
        this.durationHours = durationHours;
        this.endTimestamp = startTime + (durationHours * 3600_000L);
        this.manuallyStopped = false;
    }

    public void stop() {
        this.manuallyStopped = true;
        this.endTimestamp = Math.min(this.endTimestamp, System.currentTimeMillis());
    }

    public boolean hasStarted() {
        if (this.startTimestamp == 0) return false;
        return System.currentTimeMillis() >= this.startTimestamp;
    }

    public boolean isExpired() {
        if (this.manuallyStopped) return true;
        if (this.endTimestamp == 0) return false;
        return System.currentTimeMillis() >= this.endTimestamp;
    }

    public boolean isActive() {
        if (this.manuallyStopped) return false;
        if (!hasStarted()) return false;
        return !isExpired();
    }

    public long getTimeLeftMillis() {
        if (!isActive()) return 0L;
        return Math.max(0L, this.endTimestamp - System.currentTimeMillis());
    }

    @NotNull
    public String getTimeLeftFormatted() {
        long millis = getTimeLeftMillis();
        if (millis <= 0) return "Terminé";

        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("j ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");

        return sb.toString().trim();
    }

    @NotNull
    public Mode getMode() {
        return mode;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public boolean isManuallyStopped() {
        return manuallyStopped;
    }
}
