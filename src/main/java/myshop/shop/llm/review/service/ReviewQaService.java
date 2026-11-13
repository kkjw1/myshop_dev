package myshop.shop.llm.review.service;

import lombok.extern.slf4j.Slf4j;
import myshop.shop.llm.review.dto.ReviewQaResponseDto;
import myshop.shop.llm.review.dto.ReviewQaResponseDto.Source;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 리뷰 RAG 기반 상품 Q&A.
 * 질문 임베딩 → 해당 상품(itemNo) 리뷰만 유사도 검색 → 검색된 리뷰를 근거로 답변 생성.
 */
@Slf4j
@Service
public class ReviewQaService {

    static final int MAX_QUESTION_LENGTH = 200;
    private static final int TOP_K = 8;
    private static final double SIMILARITY_THRESHOLD = 0.3;

    private final VectorStore reviewVectorStore;
    private final ChatClient reviewQaClient;

    public ReviewQaService(VectorStore reviewVectorStore, ChatClient reviewQaClient) {
        this.reviewVectorStore = reviewVectorStore;
        this.reviewQaClient = reviewQaClient;
    }

    public ReviewQaResponseDto ask(Long itemNo, String question) {
        if (question == null || question.isBlank()) {
            return new ReviewQaResponseDto("질문을 입력해 주세요.", List.of());
        }
        if (question.length() > MAX_QUESTION_LENGTH) {
            question = question.substring(0, MAX_QUESTION_LENGTH);
        }

        try {
            // 다른 상품 리뷰가 섞이지 않도록 itemNo로 반드시 필터링
            List<Document> docs = reviewVectorStore.similaritySearch(SearchRequest.builder()
                    .query(question)
                    .topK(TOP_K)
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .filterExpression(new FilterExpressionBuilder().eq("itemNo", itemNo).build())
                    .build());

            if (docs == null || docs.isEmpty()) {
                return new ReviewQaResponseDto("질문과 관련된 리뷰를 찾지 못했습니다. 상품 상세 설명이나 1:1 문의를 이용해 주세요.", List.of());
            }

            String finalQuestion = question;
            String answer = reviewQaClient.prompt()
                    .user(u -> u.text("""
                            <reviews>
                            {reviews}
                            </reviews>

                            고객 질문: {question}
                            """)
                            .param("reviews", toContext(docs))
                            .param("question", finalQuestion))
                    .call()
                    .content();

            return new ReviewQaResponseDto(answer, docs.stream().map(this::toSource).toList());
        } catch (Exception e) {
            log.error("리뷰 Q&A 실패, itemNo={}", itemNo, e);
            return new ReviewQaResponseDto("죄송합니다. 지금은 답변을 만들 수 없습니다. 잠시 후 다시 시도해 주세요.", List.of());
        }
    }

    private String toContext(List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            sb.append(i + 1).append(". ").append(docs.get(i).getText()).append('\n');
        }
        return sb.toString();
    }

    private Source toSource(Document doc) {
        var m = doc.getMetadata();
        return new Source(
                ((Number) m.get("reviewNo")).longValue(),
                ((Number) m.get("score")).longValue(),
                (String) m.get("optionName"),
                (String) m.get("content"));
    }
}
