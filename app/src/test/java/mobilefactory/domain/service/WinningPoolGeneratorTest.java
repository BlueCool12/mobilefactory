package mobilefactory.domain.service;

import mobilefactory.config.LottoEventProperties;
import mobilefactory.domain.model.PrizeRank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mobilefactory.infrastructure.persistence.mapper.WinningSlotMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import org.mockito.Captor;

@ExtendWith(MockitoExtension.class)
class WinningPoolGeneratorTest {

  private WinningPoolGenerator generator;

  @Mock
  private WinningSlotMapper winningSlotMapper;

  @Captor
  private ArgumentCaptor<List<WinningPoolGenerator.WinningSlot>> poolCaptor;

  @BeforeEach
  void setUp() {
    LottoEventProperties properties = new LottoEventProperties();
    properties.setMaxParticipants(10000);

    LottoEventProperties.Prizes prizes = new LottoEventProperties.Prizes();

    LottoEventProperties.PrizeDetails first = new LottoEventProperties.PrizeDetails();
    // 1등은 범위 제약이 사실상 없으므로

    LottoEventProperties.PrizeDetails second = new LottoEventProperties.PrizeDetails();
    second.setRangeStart(2000); // 사용자 표기는 2000, 0-based idx는 1999
    second.setRangeEnd(7000); // 사용자 표기는 7000, 0-based idx는 6999

    LottoEventProperties.PrizeDetails third = new LottoEventProperties.PrizeDetails();
    third.setRangeStart(1000);
    third.setRangeEnd(8000);

    prizes.setFirst(first);
    prizes.setSecond(second);
    prizes.setThird(third);
    properties.setPrizes(prizes);

    generator = new WinningPoolGenerator(properties, winningSlotMapper);
  }

  @Test
  @DisplayName("설정된 당첨 등수별 슬롯들이 모두 생성되어 DB에 저장되어야 한다")
  void generatePool_totalCount() {
    generator.generateAndSavePool(1L);

    verify(winningSlotMapper).insertBatch(eq(1L), poolCaptor.capture());

    List<WinningPoolGenerator.WinningSlot> pool = poolCaptor.getValue();
    int expectedSize = PrizeRank.SECOND.getTotalWinners()
        + PrizeRank.THIRD.getTotalWinners()
        + PrizeRank.FOURTH.getTotalWinners();
    assertEquals(expectedSize, pool.size());
  }

  @Test
  @DisplayName("각 등수별 당첨자 수가 정확히 일치해야 한다")
  void generatePool_prizeCounts() {
    generator.generateAndSavePool(1L);

    verify(winningSlotMapper).insertBatch(eq(1L), poolCaptor.capture());
    List<WinningPoolGenerator.WinningSlot> pool = poolCaptor.getValue();

    long secondCount = pool.stream().filter(s -> s.prizeRank() == PrizeRank.SECOND.getRank()).count();
    long thirdCount = pool.stream().filter(s -> s.prizeRank() == PrizeRank.THIRD.getRank()).count();
    long fourthCount = pool.stream().filter(s -> s.prizeRank() == PrizeRank.FOURTH.getRank()).count();

    assertEquals(PrizeRank.SECOND.getTotalWinners(), secondCount);
    assertEquals(PrizeRank.THIRD.getTotalWinners(), thirdCount);
    assertEquals(PrizeRank.FOURTH.getTotalWinners(), fourthCount);
  }

  @Test
  @DisplayName("2등과 3등은 지정된 구간 내에만 존재해야 한다")
  void generatePool_rangeConstraints() {
    generator.generateAndSavePool(1L);

    verify(winningSlotMapper).insertBatch(eq(1L), poolCaptor.capture());
    List<WinningPoolGenerator.WinningSlot> pool = poolCaptor.getValue();

    for (WinningPoolGenerator.WinningSlot slot : pool) {
      int seq = slot.sequenceNo() + 1; // 1-based 순번 (코드에선 0-based 저장)

      if (slot.prizeRank() == PrizeRank.SECOND.getRank()) {
        assertTrue(seq >= 2000 && seq <= 7000, "2등이 범위(2000~7000)를 벗어났습니다: " + seq);
      }
      if (slot.prizeRank() == PrizeRank.THIRD.getRank()) {
        assertTrue(seq >= 1000 && seq <= 8000, "3등이 범위(1000~8000)를 벗어났습니다: " + seq);
      }
    }
  }
}
