package mobilefactory.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import java.util.function.Supplier;

@Getter
public class LottoEvent {

    private final Long id;
    private final String name;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LocalDateTime announcementStart;
    private final LocalDateTime announcementEnd;
    private final int maxParticipants;
    private final String winningNumbers;
    private final String preAssignedPhone;

    @Builder
    public LottoEvent(Long id, String name, LocalDateTime startDate, LocalDateTime endDate,
            LocalDateTime announcementStart, LocalDateTime announcementEnd,
            int maxParticipants, String winningNumbers, String preAssignedPhone) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.announcementStart = announcementStart;
        this.announcementEnd = announcementEnd;
        this.maxParticipants = maxParticipants;
        this.winningNumbers = winningNumbers;
        this.preAssignedPhone = preAssignedPhone;
    }

    public EventStatus getStatus(LocalDateTime currentTime, int currentParticipants) {
        if (isBetween(currentTime, announcementStart, announcementEnd)) {
            return EventStatus.ANNOUNCEMENT;
        }

        if (isBetween(currentTime, startDate, endDate)) {
            return (currentParticipants >= maxParticipants) ? EventStatus.CLOSED : EventStatus.ONGOING;
        }

        return EventStatus.CLOSED;
    }

    public void validateStatus(LocalDateTime now, int currentCount, EventStatus expectedStatus) {
        EventStatus actualStatus = getStatus(now, currentCount);
        if (actualStatus != expectedStatus) {
            throw new IllegalStateException("이벤트가 " + actualStatus.getDescription() + " 상태입니다.");
        }
    }

    public void validateCanParticipate(LocalDateTime now, int currentCount) {
        validateStatus(now, currentCount, EventStatus.ONGOING);
        if (currentCount >= maxParticipants) {
            throw new IllegalStateException("이벤트가 마감되었습니다.");
        }
    }

    public boolean isPreAssigned(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }

        String target = preAssignedPhone != null ? preAssignedPhone.replace("-", "") : "";
        return target.equals(phoneNumber.replace("-", ""));
    }

    public PrizeRank assignRank(String phoneNumber, Supplier<Integer> slotRankProvider) {
        if (isPreAssigned(phoneNumber)) {
            return PrizeRank.FIRST;
        }

        Integer rankValue = slotRankProvider.get();
        return PrizeRank.valueOf(rankValue != null ? rankValue : 0);
    }

    private boolean isBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        return !target.isBefore(start) && !target.isAfter(end);
    }
}
