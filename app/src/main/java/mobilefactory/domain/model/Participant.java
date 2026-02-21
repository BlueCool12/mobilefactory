package mobilefactory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Participant {
    private final Long id;
    private final Long eventId;
    private final String phoneNumber;
    private final LocalDateTime registeredAt;
    private boolean isVerified;
    private int checkCount;

    public boolean check() {
        boolean first = (this.checkCount == 0);
        this.checkCount++;
        return first;
    }

    public void verify() {
        this.isVerified = true;
    }
}
