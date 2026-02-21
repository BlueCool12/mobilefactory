package mobilefactory.infrastructure.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import mobilefactory.domain.model.LottoEvent;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventEntity {

    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime announcementStart;
    private LocalDateTime announcementEnd;
    private Integer maxParticipants;
    private String winningNumbers;
    private String preAssignedPhone;

    @Builder
    public EventEntity(Long id, String name, LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime announcementStart, LocalDateTime announcementEnd,
            Integer maxParticipants, String winningNumbers, String preAssignedPhone) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.announcementStart = announcementStart;
        this.announcementEnd = announcementEnd;
        this.maxParticipants = maxParticipants;
        this.winningNumbers = winningNumbers;
        this.preAssignedPhone = preAssignedPhone;
    }

    public LottoEvent toDomain() {
        return LottoEvent.builder()
                .id(id)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .announcementStart(announcementStart)
                .announcementEnd(announcementEnd)
                .maxParticipants(maxParticipants)
                .winningNumbers(winningNumbers)
                .preAssignedPhone(preAssignedPhone)
                .build();
    }
}
