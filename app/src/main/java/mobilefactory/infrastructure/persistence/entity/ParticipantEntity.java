package mobilefactory.infrastructure.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mobilefactory.domain.model.Participant;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ParticipantEntity {

    private Long id;
    private Long eventId;
    private String phoneNumber;
    private LocalDateTime registeredAt;
    private boolean isVerified;
    private int checkCount;

    public Participant toDomain() {
        return Participant.builder()
                .id(id)
                .eventId(eventId)
                .phoneNumber(phoneNumber)
                .registeredAt(registeredAt)
                .isVerified(isVerified)
                .checkCount(checkCount)
                .build();
    }

    public static ParticipantEntity fromDomain(Participant participant) {
        return ParticipantEntity.builder()
                .id(participant.getId())
                .eventId(participant.getEventId())
                .phoneNumber(participant.getPhoneNumber())
                .registeredAt(participant.getRegisteredAt())
                .isVerified(participant.isVerified())
                .checkCount(participant.getCheckCount())
                .build();
    }
}
