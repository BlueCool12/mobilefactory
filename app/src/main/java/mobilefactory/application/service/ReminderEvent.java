package mobilefactory.application.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;

@Getter
@RequiredArgsConstructor
public class ReminderEvent {
    private final ParticipantEntity participant;
}
