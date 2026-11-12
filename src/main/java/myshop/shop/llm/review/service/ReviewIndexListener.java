package myshop.shop.llm.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 리뷰 저장 트랜잭션이 커밋된 뒤에만 색인한다.
 * 임베딩 실패가 리뷰 등록 자체를 롤백시키지 않도록 예외는 삼킨다. (누락분은 재기동 시 재색인)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewIndexListener {

    private final ReviewIndexService reviewIndexService;

    public record ReviewSavedEvent(Long reviewNo) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewSaved(ReviewSavedEvent event) {
        try {
            reviewIndexService.index(event.reviewNo());
        } catch (Exception e) {
            log.error("리뷰 색인 실패, reviewNo={}", event.reviewNo(), e);
        }
    }
}
