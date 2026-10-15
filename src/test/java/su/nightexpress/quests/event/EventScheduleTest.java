package su.nightexpress.quests.event;

import org.junit.jupiter.api.Test;
import su.nightexpress.quests.event.definition.EventSchedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class EventScheduleTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testCalendarScheduleActive() {
        LocalDateTime now = LocalDateTime.now();
        String start = now.minusDays(1).format(FORMATTER);
        String end = now.plusDays(2).format(FORMATTER);

        EventSchedule schedule = EventSchedule.createCalendar(start, end);

        assertTrue(schedule.hasStarted());
        assertFalse(schedule.isExpired());
        assertTrue(schedule.isActive());
        assertTrue(schedule.getTimeLeftMillis() > 0);
        assertNotNull(schedule.getTimeLeftFormatted());
    }

    @Test
    public void testCalendarScheduleFuture() {
        LocalDateTime now = LocalDateTime.now();
        String start = now.plusDays(1).format(FORMATTER);
        String end = now.plusDays(3).format(FORMATTER);

        EventSchedule schedule = EventSchedule.createCalendar(start, end);

        assertFalse(schedule.hasStarted());
        assertFalse(schedule.isExpired());
        assertFalse(schedule.isActive());
    }

    @Test
    public void testCalendarScheduleExpired() {
        LocalDateTime now = LocalDateTime.now();
        String start = now.minusDays(5).format(FORMATTER);
        String end = now.minusDays(1).format(FORMATTER);

        EventSchedule schedule = EventSchedule.createCalendar(start, end);

        assertTrue(schedule.hasStarted());
        assertTrue(schedule.isExpired());
        assertFalse(schedule.isActive());
        assertEquals(0, schedule.getTimeLeftMillis());
    }

    @Test
    public void testDurationScheduleDynamicStartAndStop() {
        EventSchedule schedule = EventSchedule.createDuration(24);

        assertFalse(schedule.isActive());
        assertFalse(schedule.hasStarted());

        long now = System.currentTimeMillis();
        schedule.start(now, 48); // 48 hours

        assertTrue(schedule.hasStarted());
        assertTrue(schedule.isActive());
        assertFalse(schedule.isExpired());
        assertTrue(schedule.getTimeLeftMillis() > 47 * 3600 * 1000L);

        schedule.stop();
        assertTrue(schedule.isExpired());
        assertFalse(schedule.isActive());
    }
}
