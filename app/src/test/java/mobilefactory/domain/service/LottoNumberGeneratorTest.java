package mobilefactory.domain.service;

import mobilefactory.domain.model.PrizeRank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LottoNumberGeneratorTest {

  private final LottoNumberGenerator generator = new LottoNumberGenerator();
  private final String winningNumbers = "123456";

  @Test
  @DisplayName("1등은 기준 번호와 100% 동일해야 한다")
  void firstPrize_ExactMatch() {
    String result = generator.generate(PrizeRank.FIRST, winningNumbers);
    assertEquals(winningNumbers, result);
  }

  @Test
  @DisplayName("2등은 기준 번호와 5자리가 일치해야 한다")
  void secondPrize_5Match() {
    String result = generator.generate(PrizeRank.SECOND, winningNumbers);
    assertEquals(6, result.length());
    assertEquals(5, countMatches(result, winningNumbers));
  }

  @Test
  @DisplayName("3등은 기준 번호와 4자리가 일치해야 한다")
  void thirdPrize_4Match() {
    String result = generator.generate(PrizeRank.THIRD, winningNumbers);
    assertEquals(4, countMatches(result, winningNumbers));
  }

  @Test
  @DisplayName("4등은 기준 번호와 3자리가 일치해야 한다")
  void fourthPrize_3Match() {
    String result = generator.generate(PrizeRank.FOURTH, winningNumbers);
    assertEquals(3, countMatches(result, winningNumbers));
  }

  @Test
  @DisplayName("미당첨은 기준 번호와 2자리 이하로 일치해야 한다")
  void nonePrize_2OrLessMatch() {
    String result = generator.generate(PrizeRank.NONE, winningNumbers);
    assertTrue(countMatches(result, winningNumbers) <= 2);
  }

  private int countMatches(String generated, String target) {
    int count = 0;
    for (int i = 0; i < 6; i++) {
      if (generated.charAt(i) == target.charAt(i)) {
        count++;
      }
    }
    return count;
  }
}
