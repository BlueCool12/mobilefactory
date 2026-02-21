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
public class LottoNumberEntity {

    private Long id;
    private Long participantId;
    private String numbers;
    private int rank;
    private LocalDateTime issuedAt;

    @Builder
    public LottoNumberEntity(Long id, Long participantId, String numbers, int rank, LocalDateTime issuedAt) {
        this.id = id;
        this.participantId = participantId;
        this.numbers = numbers;
        this.rank = rank;
        this.issuedAt = issuedAt;
    }
}
