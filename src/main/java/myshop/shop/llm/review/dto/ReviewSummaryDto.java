package myshop.shop.llm.review.dto;

import java.util.List;

/**
 * 리뷰 요약 결과. LLM 구조화 출력(JSON) 대상이기도 하다.
 * available=false 이면 리뷰가 부족해 요약하지 않은 것이다.
 */
public record ReviewSummaryDto(boolean available,
                               String overview,
                               List<String> pros,
                               List<String> cons,
                               Long reviewCount) {

    public static ReviewSummaryDto unavailable(long reviewCount) {
        return new ReviewSummaryDto(false, "요약할 리뷰가 아직 충분하지 않습니다.", List.of(), List.of(), reviewCount);
    }
}
