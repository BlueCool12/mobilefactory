package mobilefactory.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WinningSlotEntity {
    private Long id;
    private Long eventId;
    private int sequenceNo;
    private int prizeRank;
}
