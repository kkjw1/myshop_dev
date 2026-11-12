package myshop.shop.llm.review.dto;

import java.util.List;

/**
 * 리뷰 기반 Q&A 응답. sources는 답변의 근거가 된 리뷰(검증용).
 */
public record ReviewQaResponseDto(String answer, List<Source> sources) {

    public record Source(Long reviewNo, Long score, String optionName, String content) {
    }
}
