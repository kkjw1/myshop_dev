package myshop.shop.llm.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.review.ReviewScoreDto;
import myshop.shop.entity.review.Review;
import myshop.shop.llm.review.dto.ReviewSummaryDto;
import myshop.shop.repository.review.ReviewRepository;
import myshop.shop.service.ReviewService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 상품 리뷰 요약. 최신 리뷰 최대 50건 + 별점 분포를 LLM에 넘겨 장단점을 정리한다.
 * LLM 호출 비용을 줄이기 위해 (상품별) 리뷰 수가 바뀌지 않으면 캐시를 재사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

    private static final int MIN_REVIEWS = 3;

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final ChatClient reviewSummaryClient;

    private record Cached(long reviewCount, ReviewSummaryDto summary) {
    }

    private final Map<Long, Cached> cache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public ReviewSummaryDto summarize(Long itemNo) {
        long count = reviewRepository.countByItem_No(itemNo);
        if (count < MIN_REVIEWS) {
            return ReviewSummaryDto.unavailable(count);
        }

        Cached cached = cache.get(itemNo);
        if (cached != null && cached.reviewCount() == count) {
            return cached.summary();
        }

        try {
            List<Review> reviews = reviewRepository.findTop50ByItem_NoOrderByCreatedDateDesc(itemNo);
            ReviewScoreDto score = reviewService.itemDetailReviewScore(itemNo);

            ReviewSummaryDto result = reviewSummaryClient.prompt()
                    .user(u -> u.text("""
                            전체 리뷰 {total}건, 별점 분포: 5점 {s5} / 4점 {s4} / 3점 {s3} / 2점 {s2} / 1점 {s1}
                            아래는 최신 리뷰 {sampled}건입니다.

                            <reviews>
                            {reviews}
                            </reviews>
                            """)
                            .param("total", score.getTotal())
                            .param("s5", score.getScore5())
                            .param("s4", score.getScore4())
                            .param("s3", score.getScore3())
                            .param("s2", score.getScore2())
                            .param("s1", score.getScore1())
                            .param("sampled", reviews.size())
                            .param("reviews", toContext(reviews)))
                    .call()
                    .entity(ReviewSummaryDto.class);

            if (result == null) {
                return ReviewSummaryDto.unavailable(count);
            }
            // available/reviewCount는 모델 출력을 믿지 않고 서버 값으로 확정
            ReviewSummaryDto summary = new ReviewSummaryDto(true, result.overview(),
                    result.pros() == null ? List.of() : result.pros(),
                    result.cons() == null ? List.of() : result.cons(), count);
            cache.put(itemNo, new Cached(count, summary));
            return summary;
        } catch (Exception e) {
            log.error("리뷰 요약 실패, itemNo={}", itemNo, e);
            return new ReviewSummaryDto(false, "지금은 리뷰 요약을 불러올 수 없습니다.", List.of(), List.of(), count);
        }
    }

    private String toContext(List<Review> reviews) {
        StringBuilder sb = new StringBuilder();
        for (Review r : reviews) {
            sb.append("- [").append(r.getScore()).append("점/").append(r.getOptionName()).append("] ")
                    .append(r.getContent()).append('\n');
        }
        return sb.toString();
    }
}
