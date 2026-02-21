package mobilefactory.application.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mobilefactory.infrastructure.persistence.entity.ParticipantEntity;

@Getter
@RequiredArgsConstructor
public class LottoParticipationEvent {
    private final ParticipantEntity participant;
    private final String phoneNumber;
    private final String lottoNum;
}
