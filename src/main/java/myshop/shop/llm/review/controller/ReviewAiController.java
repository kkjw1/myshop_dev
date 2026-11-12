package myshop.shop.llm.review.controller;

import lombok.RequiredArgsConstructor;
import myshop.shop.llm.review.dto.ReviewQaRequestDto;
import myshop.shop.llm.review.dto.ReviewQaResponseDto;
import myshop.shop.llm.review.dto.ReviewSummaryDto;
import myshop.shop.llm.review.service.ReviewQaService;
import myshop.shop.llm.review.service.ReviewSummaryService;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 상세 화면의 리뷰 AI 기능. 리뷰는 공개 정보이므로 로그인 없이 사용 가능하다.
 */
@RestController
@RequestMapping("/item/{itemNo}/reviews/ai")
@RequiredArgsConstructor
public class ReviewAiController {

    private final ReviewSummaryService reviewSummaryService;
    private final ReviewQaService reviewQaService;

    @GetMapping("/summary")
    public ReviewSummaryDto summary(@PathVariable Long itemNo) {
        return reviewSummaryService.summarize(itemNo);
    }

    @PostMapping("/qa")
    public ReviewQaResponseDto qa(@PathVariable Long itemNo, @RequestBody ReviewQaRequestDto request) {
        return reviewQaService.ask(itemNo, request.getQuestion());
    }
}
