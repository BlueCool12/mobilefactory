package mobilefactory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;
import mobilefactory.infrastructure.persistence.mapper.ParticipantMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationScheduler {

    private final ParticipantMapper participantMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void notifyUncheckedWinners() {
        LocalDateTime todayStart = LocalDateTime.now(clock).toLocalDate().atStartOfDay();
        LocalDateTime targetDate = todayStart.minusDays(10);
        LocalDateTime targetDateEnd = targetDate.plusDays(1);

        List<ParticipantEntity> uncheckedWinners = participantMapper.findUncheckedWinnersByAnnouncementDate(
                targetDate, targetDateEnd);

        for (ParticipantEntity participant : uncheckedWinners) {
            eventPublisher.publishEvent(new ReminderEvent(participant));
        }

        log.info("[Scheduler] 미확인 당첨자 알림 문자 발송 완료. 대상: {} 명", uncheckedWinners.size());
    }
}
