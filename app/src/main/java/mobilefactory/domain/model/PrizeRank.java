package mobilefactory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrizeRank {
    FIRST(1, 1, 6),
    SECOND(2, 5, 5),
    THIRD(3, 44, 4),
    FOURTH(4, 950, 3),
    NONE(0, 9000, 2);

    private final int rank;
    private final int totalWinners;
    private final int matchCount;

    public static PrizeRank valueOf(int rank) {
        for (PrizeRank prize : values()) {
            if (prize.getRank() == rank) {
                return prize;
            }
        }
        return NONE;
    }
}
