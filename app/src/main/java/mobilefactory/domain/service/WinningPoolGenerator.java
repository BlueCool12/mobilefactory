package mobilefactory.domain.service;

import lombok.RequiredArgsConstructor;
import mobilefactory.config.LottoEventProperties;
import mobilefactory.domain.model.PrizeRank;
import mobilefactory.infrastructure.persistence.mapper.WinningSlotMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WinningPoolGenerator {

    private final LottoEventProperties properties;
    private final WinningSlotMapper winningSlotMapper;

    public void generateAndSavePool(Long eventId) {
        int poolSize = properties.getMaxParticipants() - 1;
        List<WinningSlot> winningSlots = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        int secondStart = properties.getPrizes().getSecond().getRangeStart() - 1;
        int secondEnd = properties.getPrizes().getSecond().getRangeEnd() - 1;
        drawAndAddSlots(winningSlots, usedIndices, PrizeRank.SECOND, PrizeRank.SECOND.getTotalWinners(), secondStart,
                secondEnd);

        int thirdStart = properties.getPrizes().getThird().getRangeStart() - 1;
        int thirdEnd = properties.getPrizes().getThird().getRangeEnd() - 1;
        drawAndAddSlots(winningSlots, usedIndices, PrizeRank.THIRD, PrizeRank.THIRD.getTotalWinners(), thirdStart,
                thirdEnd);

        drawAndAddSlots(winningSlots, usedIndices, PrizeRank.FOURTH, PrizeRank.FOURTH.getTotalWinners(), 0,
                poolSize - 1);

        winningSlotMapper.insertBatch(eventId, winningSlots);
    }

    private void drawAndAddSlots(List<WinningSlot> winningSlots, Set<Integer> usedIndices, PrizeRank targetRank,
            int count, int minIdx, int maxIdx) {
        List<Integer> availableIndices = new ArrayList<>();

        for (int i = minIdx; i <= maxIdx; i++) {
            if (!usedIndices.contains(i)) {
                availableIndices.add(i);
            }
        }

        if (availableIndices.size() < count) {
            throw new IllegalStateException(targetRank.name() + " 당첨자를 할당하기 위한 가용 슬롯이 부족합니다.");
        }

        Collections.shuffle(availableIndices);

        for (int i = 0; i < count; i++) {
            int selectedIdx = availableIndices.get(i);
            winningSlots.add(new WinningSlot(selectedIdx, targetRank.getRank()));
            usedIndices.add(selectedIdx);
        }
    }

    public record WinningSlot(int sequenceNo, int prizeRank) {
    }
}
