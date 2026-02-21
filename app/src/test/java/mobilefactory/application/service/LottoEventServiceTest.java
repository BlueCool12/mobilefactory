package mobilefactory.application.service;

import mobilefactory.application.port.out.SmsPort;
import mobilefactory.config.LottoEventProperties;
import mobilefactory.domain.model.PrizeRank;
import mobilefactory.domain.service.LottoNumberGenerator;
import mobilefactory.infrastructure.persistence.entity.EventEntity;
import mobilefactory.infrastructure.persistence.entity.LottoNumberEntity;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;
import mobilefactory.infrastructure.persistence.mapper.EventMapper;
import mobilefactory.infrastructure.persistence.mapper.LottoNumberMapper;
import mobilefactory.infrastructure.persistence.mapper.ParticipantMapper;
import mobilefactory.infrastructure.persistence.mapper.WinningSlotMapper;
import mobilefactory.presentation.dto.ResultResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LottoEventServiceTest {

  @Mock
  private ParticipantMapper participantMapper;
  @Mock
  private LottoNumberMapper lottoNumberMapper;
  @Mock
  private EventMapper eventMapper;
  @Mock
  private WinningSlotMapper winningSlotMapper;
  @Mock
  private LottoNumberGenerator numberGenerator;
  @Mock
  private LottoEventProperties properties;
  @Mock
  private SmsPort smsPort;
  @Mock
  private Clock clock;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private LottoEventService lottoEventService;

  private EventEntity activeEvent;
  private LocalDateTime fixedNow;

  @BeforeEach
  void setUp() {

    // Set fixed clock time
    fixedNow = LocalDateTime.of(2026, 2, 21, 12, 0);
    Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    lenient().when(clock.instant()).thenReturn(fixedClock.instant());
    lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());

    // Mock active event using fixedNow
    activeEvent = EventEntity.builder()
        .name("Test Event")
        .startDate(fixedNow.minusDays(1))
        .endDate(fixedNow.plusDays(1))
        .announcementStart(fixedNow.plusDays(2))
        .announcementEnd(fixedNow.plusDays(3))
        .maxParticipants(10000)
        .winningNumbers("123456")
        .preAssignedPhone("010-9999-9999")
        .build();
    ReflectionTestUtils.setField(activeEvent, "id", 1L);

    lenient()
        .when(eventMapper.findActiveEvent(any(LocalDateTime.class)))
        .thenReturn(Optional.of(activeEvent));

    // mock pool
    lenient().when(winningSlotMapper.findRankBySequence(anyLong(), anyInt())).thenReturn(PrizeRank.NONE.getRank());
    lottoEventService.init();
  }

  @Test
  @DisplayName("정상적으로 참여하면 6자리 번호를 발급받고 DB에 저장된다")
  void participate_Success() {
    String testPhone = "010-1234-5678";
    String normalizedPhone = "01012345678";
    String code = "123456";
    // 인증번호 미리 세팅 (Reflection 사용)
    ((Map<String, String>) ReflectionTestUtils.getField(lottoEventService, "verificationStorage")).put(normalizedPhone,
        code);

    when(participantMapper.existsByPhoneNumber(normalizedPhone, 1L)).thenReturn(false);
    when(numberGenerator.generate(PrizeRank.NONE, "123456")).thenReturn("999999");

    String result = lottoEventService.participate(testPhone, code);

    assertEquals("999999", result);
    verify(participantMapper, times(1)).save(any(ParticipantEntity.class));
    verify(lottoNumberMapper, times(1)).save(any(LottoNumberEntity.class));
    verify(eventPublisher, times(1)).publishEvent(any(LottoParticipationEvent.class));
  }

  @Test
  @DisplayName("사전 지정된 휴대폰 번호로 참여하면 무조건 1등을 부여받는다")
  void participate_PreAssignedPhone_GetsFirstPrize() {
    String testPhone = "010-9999-9999"; // pre-assigned
    String normalizedPhone = "01099999999";
    String code = "111111";
    ((Map<String, String>) ReflectionTestUtils.getField(lottoEventService, "verificationStorage")).put(normalizedPhone,
        code);

    when(participantMapper.existsByPhoneNumber(normalizedPhone, 1L)).thenReturn(false);
    when(numberGenerator.generate(PrizeRank.FIRST, "123456")).thenReturn("123456");

    String result = lottoEventService.participate(testPhone, code);

    assertEquals("123456", result);
    verify(numberGenerator, times(1)).generate(PrizeRank.FIRST, "123456");
    verify(eventPublisher, times(1)).publishEvent(any(LottoParticipationEvent.class));
  }

  @Test
  @DisplayName("이미 참여한 번호는 예외가 발생한다 (중복참여 방지)")
  void participate_Duplicated_ThrowsException() {
    String testPhone = "010-1234-5678";
    String normalizedPhone = "01012345678";
    String code = "111111";
    ((Map<String, String>) ReflectionTestUtils.getField(lottoEventService, "verificationStorage")).put(normalizedPhone,
        code);
    when(participantMapper.existsByPhoneNumber(normalizedPhone, 1L)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> lottoEventService.participate(testPhone, code));
    verify(participantMapper, never()).save(any());
  }

  @Test
  @DisplayName("참여 기간이 아니면 예외가 발생한다")
  void participate_OutsidePeriod_ThrowsException() {
    String testPhone = "010-1234-5678";
    String normalizedPhone = "01012345678";
    String code = "123456";
    // 인증번호 미리 세팅
    ((Map<String, String>) ReflectionTestUtils.getField(lottoEventService, "verificationStorage")).put(normalizedPhone,
        code);

    // 이벤트를 하나 새로 만들어서 끝난 이벤트로 세팅
    EventEntity closedEvent = EventEntity.builder()
        .name("Closed Event")
        .startDate(fixedNow.minusDays(5))
        .endDate(fixedNow.minusDays(1))
        .announcementStart(fixedNow.minusDays(1))
        .announcementEnd(fixedNow.plusDays(1))
        .maxParticipants(10000)
        .winningNumbers("123456")
        .build();
    ReflectionTestUtils.setField(closedEvent, "id", 2L);

    when(eventMapper.findActiveEvent(any(LocalDateTime.class)))
        .thenReturn(Optional.of(closedEvent));

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> lottoEventService.participate(testPhone, code));
    assertTrue(exception.getMessage().contains("이벤트가 결과 발표 상태입니다."));
  }

  @Test
  @DisplayName("인증번호를 요청하면 SMS가 발송되고 저장소에 코드가 남아야 한다")
  void sendVerificationCode_Success() {
    String testPhone = "010-1111-2222";
    String normalizedPhone = "01011112222";
    when(participantMapper.existsByPhoneNumber(normalizedPhone, 1L)).thenReturn(false);

    lottoEventService.sendVerificationCode(testPhone);

    Map<String, String> storage = (Map<String, String>) ReflectionTestUtils.getField(lottoEventService,
        "verificationStorage");

    assertNotNull(storage.get(normalizedPhone));
    assertEquals(6, storage.get(normalizedPhone).length());
    verify(eventPublisher, times(1)).publishEvent(any(VerificationCodeEvent.class));
  }

  @Test
  @DisplayName("첫 결과 확인 시에는 정확한 등수가 노출된다")
  void checkResult_FirstTime_RevealsRank() {
    String testPhone = "010-1234-5678";
    String normalizedPhone = "01012345678";
    ParticipantEntity mockParticipant = ParticipantEntity.builder()
        .eventId(1L)
        .phoneNumber(normalizedPhone)
        .registeredAt(fixedNow)
        .build();
    ReflectionTestUtils.setField(mockParticipant, "id", 1L);

    LottoNumberEntity mockLotto = LottoNumberEntity.builder()
        .participantId(1L)
        .numbers("123459")
        .rank(PrizeRank.SECOND.getRank())
        .build();

    // 발표 기간인 이벤트로 모킹 재설정
    EventEntity announcementEvent = EventEntity.builder()
        .startDate(fixedNow.minusDays(5))
        .endDate(fixedNow.minusDays(3))
        .announcementStart(fixedNow.minusDays(1))
        .announcementEnd(fixedNow.plusDays(1))
        .maxParticipants(10000)
        .winningNumbers("123456")
        .build();
    ReflectionTestUtils.setField(announcementEvent, "id", 1L);
    when(eventMapper.findActiveEvent(any(LocalDateTime.class))).thenReturn(Optional.of(announcementEvent));

    when(participantMapper.findByPhoneNumber(normalizedPhone, 1L)).thenReturn(Optional.of(mockParticipant));
    when(lottoNumberMapper.findByParticipantId(1L)).thenReturn(Optional.of(mockLotto));

    ResultResponse result = lottoEventService.checkResult(testPhone);

    assertTrue(result.isFirstCheck());
    assertEquals(PrizeRank.SECOND, result.getRank());

    ArgumentCaptor<ParticipantEntity> captor = ArgumentCaptor.forClass(ParticipantEntity.class);
    verify(participantMapper).update(captor.capture());
    assertEquals(1, captor.getValue().getCheckCount());
  }

  @Test
  @DisplayName("두 번째 결과 확인 시에는 등수 대신 당첨 여부(1 또는 0)만 노출된다")
  void checkResult_SecondTime_HidesRank() {
    String testPhone = "010-1234-5678";
    String normalizedPhone = "01012345678";
    ParticipantEntity mockParticipant = ParticipantEntity.builder()
        .eventId(1L)
        .phoneNumber(normalizedPhone)
        .registeredAt(fixedNow)
        .build();
    ReflectionTestUtils.setField(mockParticipant, "id", 1L);
    ReflectionTestUtils.setField(mockParticipant, "checkCount", 1);

    LottoNumberEntity mockLotto = LottoNumberEntity.builder()
        .participantId(1L)
        .numbers("123456")
        .rank(PrizeRank.FIRST.getRank())
        .build();

    // 발표 기간인 이벤트로 모킹 재설정
    EventEntity announcementEvent = EventEntity.builder()
        .startDate(fixedNow.minusDays(5))
        .endDate(fixedNow.minusDays(3))
        .announcementStart(fixedNow.minusDays(1))
        .announcementEnd(fixedNow.plusDays(1))
        .maxParticipants(10000)
        .winningNumbers("123456")
        .build();
    ReflectionTestUtils.setField(announcementEvent, "id", 1L);
    when(eventMapper.findActiveEvent(any(LocalDateTime.class))).thenReturn(Optional.of(announcementEvent));

    when(participantMapper.findByPhoneNumber(normalizedPhone, 1L)).thenReturn(Optional.of(mockParticipant));
    when(lottoNumberMapper.findByParticipantId(1L)).thenReturn(Optional.of(mockLotto));

    ResultResponse result = lottoEventService.checkResult(testPhone);

    assertFalse(result.isFirstCheck());
    assertEquals(PrizeRank.FIRST, result.getRank());

    ArgumentCaptor<ParticipantEntity> captor = ArgumentCaptor.forClass(ParticipantEntity.class);
    verify(participantMapper).update(captor.capture());
    assertEquals(2, captor.getValue().getCheckCount());
  }
}
