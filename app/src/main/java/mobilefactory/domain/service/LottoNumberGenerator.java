package mobilefactory.domain.service;

import mobilefactory.domain.model.PrizeRank;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LottoNumberGenerator {

    public String generate(PrizeRank targetRank, String winningNumbers) {
        if (targetRank == PrizeRank.FIRST) {
            return winningNumbers;
        }

        int matchCount = (targetRank == PrizeRank.NONE) ? 0 : targetRank.getMatchCount();

        String generated;
        do {
            generated = mutate(winningNumbers, matchCount);
        } while (targetRank == PrizeRank.NONE && calculateMatchCount(winningNumbers, generated) >= 3);

        return generated;
    }

    private String mutate(String origin, int keepCount) {
        char[] result = origin.toCharArray();
        List<Integer> indexes = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5));

        int changeCount = 6 - keepCount;
        Collections.shuffle(indexes);

        for (int i = 0; i < changeCount; i++) {
            int targetIdx = indexes.get(i);
            char originChar = result[targetIdx];
            char newChar;
            do {
                newChar = (char) ('0' + ThreadLocalRandom.current().nextInt(10));
            } while (newChar == originChar);

            result[targetIdx] = newChar;
        }

        return new String(result);
    }

    private int calculateMatchCount(String origin, String target) {
        int count = 0;
        for (int i = 0; i < 6; i++) {
            if (origin.charAt(i) == target.charAt(i)) {
                count++;
            }
        }

        return count;
    }
}
