package myshop.shop.llm.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.entity.review.Review;
import myshop.shop.repository.review.ReviewRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 리뷰를 벡터 저장소에 색인한다.
 * - 기동 시 전체 재색인 (벡터 저장소가 인메모리이므로)
 * - 리뷰 등록 시 1건 추가 색인 (ReviewIndexListener)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewIndexService {

    private static final int BATCH_SIZE = 100;

    private final VectorStore reviewVectorStore;
    private final ReviewRepository reviewRepository;

    /** 임베딩 API 장애가 서버 기동을 막으면 안 되므로 예외는 로그만 남긴다. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void indexAllOnStartup() {
        try {
            List<Review> reviews = reviewRepository.findAll();
            for (int i = 0; i < reviews.size(); i += BATCH_SIZE) {
                List<Review> batch = reviews.subList(i, Math.min(i + BATCH_SIZE, reviews.size()));
                reviewVectorStore.add(batch.stream().map(this::toDocument).toList());
            }
            log.info("리뷰 RAG 색인 완료, count={}", reviews.size());
        } catch (Exception e) {
            log.error("리뷰 RAG 초기 색인 실패 (Q&A는 색인 전까지 답변하지 못함)", e);
        }
    }

    @Transactional(readOnly = true)
    public void index(Long reviewNo) {
        reviewRepository.findById(reviewNo).ifPresent(r -> reviewVectorStore.add(List.of(toDocument(r))));
    }

    private Document toDocument(Review review) {
        // 같은 리뷰를 다시 색인해도 중복되지 않도록 reviewNo 기반의 고정 id 사용
        String id = UUID.nameUUIDFromBytes(("review:" + review.getNo()).getBytes(StandardCharsets.UTF_8)).toString();
        String text = "[옵션: " + nullToEmpty(review.getOptionName()) + " / 별점: " + review.getScore() + "] "
                + nullToEmpty(review.getContent());
        return Document.builder()
                .id(id)
                .text(text)
                .metadata(Map.of(
                        "itemNo", review.getItem().getNo(),
                        "reviewNo", review.getNo(),
                        "score", review.getScore(),
                        "optionName", nullToEmpty(review.getOptionName()),
                        "content", nullToEmpty(review.getContent())))
                .build();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
