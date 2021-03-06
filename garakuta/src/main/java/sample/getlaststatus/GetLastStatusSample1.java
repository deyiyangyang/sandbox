package sample.getlaststatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * instanceofバージョン
 *
 */
public class GetLastStatusSample1 {

    public static void main(String[] args) {

        List<Event> events = new ArrayList<>();
        events.add(createStatusEvent(LocalDateTime.of(2015, 8, 11, 17, 27, 00),
                Status.TODO));
        events.add(createOtherEvent(LocalDateTime.of(2015, 8, 11, 17, 28, 00)));
        events.add(createStatusEvent(LocalDateTime.of(2015, 8, 11, 17, 29, 00),
                Status.DOING));
        events.add(createOtherEvent(LocalDateTime.of(2015, 8, 11, 17, 30, 00)));
        events.add(createStatusEvent(LocalDateTime.of(2015, 8, 11, 17, 31, 00),
                Status.DONE));

        Todo todo = new Todo();
        todo.events = events;

        System.out.println(todo.isDone());
    }

    static StatusEvent createStatusEvent(LocalDateTime timestamp, Status status) {
        StatusEvent event = new StatusEvent();
        event.timestamp = timestamp;
        event.status = status;
        return event;
    }

    static OtherEvent createOtherEvent(LocalDateTime timestamp) {
        OtherEvent event = new OtherEvent();
        event.timestamp = timestamp;
        return event;
    }

    static class Todo {
        List<Event> events;

        boolean isDone() {
            Comparator<Event> comparator = Comparator
                    .comparing(event -> event.timestamp);
            return events.stream().sorted(comparator.reversed())
                    .filter(event -> event instanceof StatusEvent)
                    .map(StatusEvent.class::cast).findFirst()
                    .map(event -> event.status == Status.DONE).orElse(false);
        }
    }

    static abstract class Event {
        LocalDateTime timestamp;
    }

    static class StatusEvent extends Event {
        Status status;
    }

    static class OtherEvent extends Event {
    }

    enum Status {
        TODO, DOING, DONE
    }
}
