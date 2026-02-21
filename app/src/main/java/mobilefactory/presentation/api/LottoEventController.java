package mobilefactory.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mobilefactory.application.service.LottoEventService;
import mobilefactory.presentation.dto.EventStatusResponse;
import mobilefactory.presentation.dto.ParticipateRequest;
import mobilefactory.presentation.dto.ParticipateResponse;
import mobilefactory.presentation.dto.ResultResponse;
import mobilefactory.presentation.dto.SmsRequest;
import mobilefactory.presentation.dto.SmsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class LottoEventController {

    private final LottoEventService lottoEventService;

    @GetMapping("/status")
    public ResponseEntity<EventStatusResponse> getStatus() {
        return ResponseEntity.ok(lottoEventService.getStatus());
    }

    @PostMapping("/otp")
    public ResponseEntity<SmsResponse> requestSmsAuth(@Valid @RequestBody SmsRequest request) {
        lottoEventService.sendVerificationCode(request.getPhoneNumber());
        return ResponseEntity.ok(new SmsResponse("인증번호가 발송되었습니다."));
    }

    @PostMapping("/participate")
    public ResponseEntity<ParticipateResponse> participate(@Valid @RequestBody ParticipateRequest request) {
        String issuedNumber = lottoEventService.participate(request.getPhoneNumber(), request.getVerificationCode());
        return ResponseEntity.ok(new ParticipateResponse("참여 완료 및 문자 발송", issuedNumber));
    }

    @PostMapping("/result")
    public ResponseEntity<ResultResponse> checkResult(@Valid @RequestBody SmsRequest request) {
        ResultResponse result = lottoEventService.checkResult(request.getPhoneNumber());
        return ResponseEntity.ok(result);
    }
}
