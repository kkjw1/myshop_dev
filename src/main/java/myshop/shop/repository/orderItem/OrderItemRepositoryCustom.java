package myshop.shop.repository.orderItem;

import myshop.shop.dto.order.DetailOrderDto;
import myshop.shop.dto.order.ManageOrderDto;

import java.util.List;

public interface OrderItemRepositoryCustom {
    /**
     * 주문 목록 상세
     */
    DetailOrderDto getDetailOrder(Long orderNo, Long memberNo);

    /**
     * 주문 목록/배송 조회
     */
    List<ManageOrderDto> getManageOrder(Long memberNo);
}
