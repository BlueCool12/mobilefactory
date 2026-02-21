package mobilefactory.application.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VerificationCodeEvent {
    private final String phoneNumber;
    private final String code;
}
