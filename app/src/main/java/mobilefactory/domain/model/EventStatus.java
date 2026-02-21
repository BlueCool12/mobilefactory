package mobilefactory.domain.model;

public enum EventStatus {
    ONGOING("진행 중"),
    CLOSED("참여 마감"),
    ANNOUNCEMENT("결과 발표");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
