package myshop.shop.service;

import lombok.RequiredArgsConstructor;
import myshop.shop.controller.HomeItemController;
import myshop.shop.controller.HomeItemController.DetailItemReviewDto;
import myshop.shop.dto.review.ReviewScoreDto;
import myshop.shop.dto.review.SaveReviewDto;
import myshop.shop.dto.review.SearchReviewDto;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.orderItem.OrderItem;
import myshop.shop.entity.review.Review;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.repository.orderItem.OrderItemRepository;
import myshop.shop.repository.review.ReviewRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 리뷰 등록
     * 주문 목록/배송 조회 -> 리뷰 작성하기
     */
    public void saveReview(SaveReviewDto saveReviewDto) {
        Item itemProxy = itemRepository.getReferenceById(saveReviewDto.getItemNo());
        Review saved = reviewRepository.save(new Review(itemProxy,
                saveReviewDto.getMemberNo(),
                saveReviewDto.getOptionName(),
                saveReviewDto.getCount(),
                saveReviewDto.getScore(),
                saveReviewDto.getContent()));

        OrderItem orderItem = orderItemRepository.findById(saveReviewDto.getOrderItemNo()).orElse(null);
        if (orderItem != null) {
            orderItem.updateReview(true);
        }
    }


    /**
     * 상품 상세 -> 상품 리뷰
     */
    public Page<DetailItemReviewDto> itemDetailReview(Pageable pageable, Long itemNo, SearchReviewDto searchReviewDto) {
        return reviewRepository.findDetailItemReview(pageable, itemNo, searchReviewDto);
    }


    public ReviewScoreDto itemDetailReviewScore(Long itemNo) {
        return reviewRepository.findReviewScore(itemNo);
    }


    /**
     * 도움이 돼요 버튼
     */
    public Long toggleGoodCount(Long reviewNo, boolean like) {
        Review review = reviewRepository.findById(reviewNo).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 리뷰입니다. reviewNo=" + reviewNo));
        if (like) {
            review.addGoodCount();
        } else {
            review.subGoodCount();
        }
        return review.getGoodCount();
    }
}
