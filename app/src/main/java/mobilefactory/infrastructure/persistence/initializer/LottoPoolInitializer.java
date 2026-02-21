package mobilefactory.infrastructure.persistence.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mobilefactory.infrastructure.persistence.entity.EventEntity;
import mobilefactory.infrastructure.persistence.entity.LottoNumberEntity;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;
import mobilefactory.infrastructure.persistence.mapper.EventMapper;
import mobilefactory.infrastructure.persistence.mapper.LottoNumberMapper;
import mobilefactory.infrastructure.persistence.mapper.ParticipantMapper;
import mobilefactory.infrastructure.persistence.mapper.WinningSlotMapper;
import mobilefactory.domain.service.WinningPoolGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LottoPoolInitializer implements CommandLineRunner {

    private final EventMapper eventMapper;
    private final ParticipantMapper participantMapper;
    private final LottoNumberMapper lottoNumberMapper;
    private final WinningSlotMapper winningSlotMapper;
    private final WinningPoolGenerator winningPoolGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (eventMapper.findActiveEvent(now).isPresent()) {
            log.info("이미 활성화된 이벤트가 존재하여 초기화를 건너뜀");
            return;
        }

        log.info("테스트용 로또 데이터 초기화 시작 (원하는 시나리오의 주석을 해제하여 사용하세요)");

        /*
         * =====================================================================
         * [시나리오 A] 참여(Ongoing) 테스트 모드
         * - 현재 참여 가능한 이벤트 하나를 생성합니다.
         * =====================================================================
         */
        initScenarioA(now);

        /*
         * =====================================================================
         * [시나리오 B] 결과 확인(Announcement) 테스트 모드
         * - 이미 종료되어 결과를 확인할 수 있는 이벤트를 생성하고 테스트용 당첨자를 심습니다.
         * - 사용법: 하단의 initScenarioA와 initScenarioB의 주석을 교체하세요.
         * =====================================================================
         */
        // initScenarioB(now);
    }

    private void initScenarioA(LocalDateTime now) {
        EventEntity ongoingEvent = EventEntity.builder()
                .name("참여 테스트용 이벤트 (진행 중)")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(7))
                .announcementStart(now.plusDays(8))
                .announcementEnd(now.plusDays(15))
                .maxParticipants(10000)
                .winningNumbers("123456")
                .preAssignedPhone("01000000000")
                .build();

        eventMapper.insert(ongoingEvent);
        winningPoolGenerator.generateAndSavePool(ongoingEvent.getId());
        log.info("시나리오 A 초기화 완료: 참여 가능한 이벤트가 생성되었습니다. (ID: {})", ongoingEvent.getId());
    }

    private void initScenarioB(LocalDateTime now) {
        EventEntity announcementEvent = EventEntity.builder()
                .name("결과 확인용 이벤트 (발표 중)")
                .startDate(now.minusDays(5))
                .endDate(now.minusDays(1))
                .announcementStart(now.minusHours(1))
                .announcementEnd(now.plusDays(7))
                .maxParticipants(10000)
                .winningNumbers("654321")
                .preAssignedPhone("01000000000")
                .build();

        eventMapper.insert(announcementEvent);
        Long eventId = announcementEvent.getId();
        winningPoolGenerator.generateAndSavePool(eventId);

        // 시나리오별 테스트 참여자 등록 (실제 생성된 슬롯 테이블의 0~4번 시퀀스 결과를 가져와 매칭)
        createParticipantWithRealSlot(eventId, "01011111111", 0);
        createParticipantWithRealSlot(eventId, "01022222222", 1);
        createParticipantWithRealSlot(eventId, "01033333333", 2);
        createParticipantWithRealSlot(eventId, "01044444444", 3);
        createParticipantWithRealSlot(eventId, "01055555555", 4);

        log.info("시나리오 B 초기화 완료: 실제 당첨 슬롯과 매칭된 데이터가 생성되었습니다. (ID: {})", eventId);
    }

    private void createParticipantWithRealSlot(Long eventId, String phone, int seq) {
        Integer actualRank = winningSlotMapper.findRankBySequence(eventId, seq);
        if (actualRank == null)
            actualRank = 0;

        createParticipantWithResult(eventId, phone, "12345" + seq, actualRank);
    }

    private void createParticipantWithResult(Long eventId, String phone, String numbers, int rank) {
        LocalDateTime now = LocalDateTime.now(clock);
        ParticipantEntity participant = ParticipantEntity.builder()
                .eventId(eventId)
                .phoneNumber(phone)
                .registeredAt(now.minusHours(1))
                .isVerified(true)
                .checkCount(0)
                .build();
        participantMapper.save(participant);

        LottoNumberEntity lottoNumber = LottoNumberEntity.builder()
                .participantId(participant.getId())
                .numbers(numbers)
                .rank(rank)
                .issuedAt(now.minusHours(1))
                .build();
        lottoNumberMapper.save(lottoNumber);
    }
}
