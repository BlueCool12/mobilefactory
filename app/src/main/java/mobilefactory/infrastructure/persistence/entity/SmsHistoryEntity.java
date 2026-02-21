package mobilefactory.infrastructure.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsHistoryEntity {

    private Long id;
    private Long participantId;
    private String targetPhoneNumber;
    private String category;
    private String content;
    private LocalDateTime sentAt;

    @Builder
    public SmsHistoryEntity(Long id, Long participantId, String targetPhoneNumber, String category, String content,
            LocalDateTime sentAt) {
        this.id = id;
        this.participantId = participantId;
        this.targetPhoneNumber = targetPhoneNumber;
        this.category = category;
        this.content = content;
        this.sentAt = sentAt;
    }
}
