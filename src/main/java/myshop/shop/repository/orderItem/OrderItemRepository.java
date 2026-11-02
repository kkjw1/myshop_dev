package myshop.shop.repository.orderItem;

import myshop.shop.entity.orderItem.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepositoryCustom {
    /**
     * 소유권 검증
     */
    @Query("select oi from OrderItem oi where oi.no =: orderItemNo and oi.order.member.no =: memberNo")
    Optional<OrderItem> findByNoAndMemberNo(@Param("orderItemNo") Long orderItemNo, @Param("memberNo") Long memberNo);
}
