package myshop.shop.repository.review;

import myshop.shop.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    /** 리뷰 요약용: 상품의 최신 리뷰 50건 */
    List<Review> findTop50ByItem_NoOrderByCreatedDateDesc(Long itemNo);

    long countByItem_No(Long itemNo);
}
