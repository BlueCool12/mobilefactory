package mobilefactory.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipateResponse {
    private final String message;
    private final String issuedNumber;
}
