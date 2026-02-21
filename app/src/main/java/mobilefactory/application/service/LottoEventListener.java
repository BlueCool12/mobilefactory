package mobilefactory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mobilefactory.application.port.out.SmsPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LottoEventListener {

    private final SmsPort smsPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipation(LottoParticipationEvent event) {
        String smsContent = String.format("축하합니다! 고객님의 로또 번호는 [%s] 입니다. 당첨일에 꼭 확인해보세요!", event.getLottoNum());
        smsPort.sendSms(event.getParticipant(), event.getPhoneNumber(), "LOTTO_ISSUED", smsContent);
        log.info("[Async-SMS] 로또 발급 문자 전송 완료. 수신자: {}", event.getPhoneNumber());
    }

    @Async
    @org.springframework.context.event.EventListener
    public void handleVerificationCode(VerificationCodeEvent event) {
        String content = String.format("[MobileFactory] 인증번호는 [%s] 입니다. 3분 이내에 입력해주세요.", event.getCode());
        smsPort.sendSms(null, event.getPhoneNumber(), "VERIFICATION", content);
        log.info("[Async-SMS] 인증번호 문자 전송 완료. 수신자: {}", event.getPhoneNumber());
    }

    @Async
    @org.springframework.context.event.EventListener
    public void handleReminder(ReminderEvent event) {
        String msg = "고객님, 진행된 이벤트에 당첨되셨습니다. 서둘러 홈페이지에 접속하여 결과를 확인해주세요!";
        smsPort.sendSms(event.getParticipant(), event.getParticipant().getPhoneNumber(), "REMINDER", msg);
        log.info("[Async-SMS] 미확인 당첨자 알림 문자 전송 완료. 수신자: {}", event.getParticipant().getPhoneNumber());
    }
}
