package mobilefactory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "event.lotto")
public class LottoEventProperties {

    private int maxParticipants;
    private Prizes prizes;

    @Getter
    @Setter
    public static class Prizes {
        private PrizeDetails first;
        private PrizeDetails second;
        private PrizeDetails third;
    }

    @Getter
    @Setter
    public static class PrizeDetails {
        private Integer rangeStart;
        private Integer rangeEnd;
    }
}
