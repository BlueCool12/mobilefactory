package mobilefactory.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mobilefactory.domain.model.PrizeRank;

@Getter
@AllArgsConstructor
public class ResultResponse {
    private String numbers;
    private PrizeRank rank;
    private boolean isFirstCheck;
}
