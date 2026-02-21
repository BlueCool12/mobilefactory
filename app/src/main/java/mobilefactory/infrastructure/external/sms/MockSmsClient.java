package mobilefactory.infrastructure.external.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Clock;
import java.time.LocalDateTime;
import mobilefactory.application.port.out.SmsPort;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;
import mobilefactory.infrastructure.persistence.entity.SmsHistoryEntity;
import mobilefactory.infrastructure.persistence.mapper.SmsHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockSmsClient implements SmsPort {

    private final SmsHistoryMapper smsHistoryMapper;
    private final Clock clock;

    @Override
    @Transactional
    public void sendSms(ParticipantEntity participant, String phoneNumber, String category, String content) {
        log.info("[SMS 전송 모의 실행] 수신번호: {}, 카테고리: {}, 내용: {}", phoneNumber, category, content);

        SmsHistoryEntity history = SmsHistoryEntity.builder()
                .participantId(participant != null ? participant.getId() : null)
                .targetPhoneNumber(phoneNumber)
                .category(category)
                .content(content)
                .sentAt(LocalDateTime.now(clock))
                .build();

        smsHistoryMapper.save(history);
    }
}
