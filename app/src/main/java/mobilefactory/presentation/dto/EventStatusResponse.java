package mobilefactory.presentation.dto;

import lombok.Getter;
import java.time.LocalDateTime;
import mobilefactory.domain.model.EventStatus;
import mobilefactory.domain.model.LottoEvent;

@Getter
public class EventStatusResponse {
    private final String status;
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final int currentParticipants;
    private final int maxParticipants;

    private EventStatusResponse(String status, String description, LocalDateTime startDate,
            LocalDateTime endDate, int currentParticipants, int maxParticipants) {
        this.status = status;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentParticipants = currentParticipants;
        this.maxParticipants = maxParticipants;
    }

    public static EventStatusResponse from(LottoEvent event, EventStatus status, int currentParticipants) {
        return new EventStatusResponse(
                status.name(),
                status.getDescription(),
                event.getStartDate(),
                event.getEndDate(),
                currentParticipants,
                event.getMaxParticipants());
    }

    public static EventStatusResponse empty() {
        return new EventStatusResponse(
                EventStatus.CLOSED.name(),
                EventStatus.CLOSED.getDescription(),
                null, null, 0, 0);
    }
}
