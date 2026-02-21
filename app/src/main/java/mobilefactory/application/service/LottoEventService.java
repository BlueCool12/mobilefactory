package mobilefactory.application.service;

import lombok.RequiredArgsConstructor;
import mobilefactory.domain.model.EventStatus;
import mobilefactory.domain.model.LottoEvent;
import mobilefactory.domain.model.Participant;
import mobilefactory.domain.model.PrizeRank;
import mobilefactory.presentation.dto.EventStatusResponse;
import mobilefactory.presentation.dto.ResultResponse;
import mobilefactory.domain.service.LottoNumberGenerator;
import mobilefactory.infrastructure.persistence.entity.EventEntity;
import mobilefactory.infrastructure.persistence.entity.LottoNumberEntity;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;
import mobilefactory.infrastructure.persistence.mapper.EventMapper;
import mobilefactory.infrastructure.persistence.mapper.LottoNumberMapper;
import mobilefactory.infrastructure.persistence.mapper.ParticipantMapper;
import mobilefactory.infrastructure.persistence.mapper.WinningSlotMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LottoEventService {

    private final Clock clock;
    private final ParticipantMapper participantMapper;
    private final LottoNumberMapper lottoNumberMapper;
    private final EventMapper eventMapper;
    private final WinningSlotMapper winningSlotMapper;
    private final LottoNumberGenerator numberGenerator;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicInteger totalParticipants = new AtomicInteger(0);
    private final AtomicInteger poolSequence = new AtomicInteger(0);
    private final Map<String, String> verificationStorage = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now(clock);
        eventMapper.findActiveEvent(now).ifPresent(event -> {
            int totalCount = participantMapper.countByEventId(event.getId());
            totalParticipants.set(totalCount);

            int poolCount = participantMapper.countNonFirstPrizeParticipants(event.getId());
            poolSequence.set(poolCount);
        });
    }

    public EventStatusResponse getStatus() {
        LocalDateTime now = LocalDateTime.now(clock);

        return eventMapper.findActiveEvent(now)
                .map(entity -> {
                    LottoEvent event = entity.toDomain();
                    int currentCount = totalParticipants.get();
                    return EventStatusResponse.from(event, event.getStatus(now, currentCount), currentCount);
                })
                .orElseGet(EventStatusResponse::empty);
    }

    public void sendVerificationCode(String phoneNumber) {
        phoneNumber = normalizePhoneNumber(phoneNumber);
        LocalDateTime now = LocalDateTime.now(clock);
        LottoEvent event = getActiveEventOrThrow(now).toDomain();

        event.validateStatus(now, totalParticipants.get(), EventStatus.ONGOING);
        validateDuplicateParticipation(phoneNumber, event.getId());

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        verificationStorage.put(phoneNumber, code);

        eventPublisher.publishEvent(new VerificationCodeEvent(phoneNumber, code));
    }

    @Transactional
    public String participate(String phoneNumber, String verificationCode) {
        phoneNumber = normalizePhoneNumber(phoneNumber);
        verifyVerificationCode(phoneNumber, verificationCode);

        LocalDateTime now = LocalDateTime.now(clock);
        LottoEvent lottoEvent = getActiveEventOrThrow(now).toDomain();
        validateDuplicateParticipation(phoneNumber, lottoEvent.getId());

        int currentSequence = totalParticipants.getAndIncrement();
        boolean[] poolIncremented = { false };

        try {
            lottoEvent.validateCanParticipate(now, currentSequence);

            PrizeRank assignedRank = lottoEvent.assignRank(phoneNumber, () -> {
                int seq = poolSequence.getAndIncrement();
                poolIncremented[0] = true;
                return winningSlotMapper.findRankBySequence(lottoEvent.getId(), seq);
            });

            ParticipantEntity participant = createAndSaveParticipant(lottoEvent.getId(), phoneNumber, now);
            String generatedLottoNum = issueAndSaveLottoNumber(participant, assignedRank,
                    lottoEvent.getWinningNumbers(),
                    now);

            eventPublisher.publishEvent(new LottoParticipationEvent(participant, phoneNumber, generatedLottoNum));

            return generatedLottoNum;
        } catch (Exception e) {
            totalParticipants.decrementAndGet();
            if (poolIncremented[0]) {
                poolSequence.decrementAndGet();
            }
            throw e;
        }
    }

    @Transactional
    public ResultResponse checkResult(String phoneNumber) {
        phoneNumber = normalizePhoneNumber(phoneNumber);
        LocalDateTime now = LocalDateTime.now(clock);

        LottoEvent lottoEvent = getActiveEventOrThrow(now).toDomain();
        lottoEvent.validateStatus(now, 0, EventStatus.ANNOUNCEMENT);

        Participant participant = getParticipantOrThrow(phoneNumber, lottoEvent.getId()).toDomain();
        LottoNumberEntity lottoNumber = getLottoNumberOrThrow(participant.getId());

        boolean isFirstCheck = participant.check();
        participantMapper.update(ParticipantEntity.fromDomain(participant));

        return new ResultResponse(lottoNumber.getNumbers(), PrizeRank.valueOf(lottoNumber.getRank()), isFirstCheck);
    }

    private EventEntity getActiveEventOrThrow(LocalDateTime now) {
        return eventMapper.findActiveEvent(now)
                .orElseThrow(() -> new IllegalStateException("현재 진행 중인 이벤트가 없습니다."));
    }

    private ParticipantEntity getParticipantOrThrow(String phoneNumber, Long eventId) {
        return participantMapper.findByPhoneNumber(phoneNumber, eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 참여 이력이 없는 휴대폰 번호입니다."));
    }

    private LottoNumberEntity getLottoNumberOrThrow(Long participantId) {
        return lottoNumberMapper.findByParticipantId(participantId)
                .orElseThrow(() -> new IllegalStateException("발급된 로또 번호가 없습니다."));
    }

    private void validateDuplicateParticipation(String phoneNumber, Long eventId) {
        if (participantMapper.existsByPhoneNumber(phoneNumber, eventId)) {
            throw new IllegalArgumentException("이미 이벤트에 참여한 휴대폰 번호입니다.");
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "");
    }

    private void verifyVerificationCode(String phoneNumber, String code) {
        if (!verificationStorage.remove(phoneNumber, code)) {
            throw new IllegalArgumentException("인증번호가 존재하지 않거나 일치하지 않습니다.");
        }
    }

    private ParticipantEntity createAndSaveParticipant(Long eventId, String phoneNumber, LocalDateTime registeredAt) {
        Participant participant = Participant.builder()
                .eventId(eventId)
                .phoneNumber(phoneNumber)
                .registeredAt(registeredAt)
                .isVerified(false)
                .checkCount(0)
                .build();

        participant.verify();

        ParticipantEntity entity = ParticipantEntity.fromDomain(participant);
        participantMapper.save(entity);
        return entity;
    }

    private String issueAndSaveLottoNumber(ParticipantEntity participant, PrizeRank rank, String winningNumbers,
            LocalDateTime now) {
        String lottoNum = numberGenerator.generate(rank, winningNumbers);

        LottoNumberEntity lottoNumber = LottoNumberEntity.builder()
                .participantId(participant.getId())
                .numbers(lottoNum)
                .rank(rank.getRank())
                .issuedAt(now)
                .build();
        lottoNumberMapper.save(lottoNumber);
        return lottoNum;
    }
}
