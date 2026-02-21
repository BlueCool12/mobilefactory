package mobilefactory.application.port.out;

import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;

public interface SmsPort {
    void sendSms(ParticipantEntity participant, String phoneNumber, String category, String content);
}
