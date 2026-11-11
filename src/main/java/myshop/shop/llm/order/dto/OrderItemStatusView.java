package myshop.shop.llm.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import myshop.shop.entity.delivery.DeliveryStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemStatusView {
    private Long orderItemNo;

    // 배송 상태, 택배사, 운송장번호, 취소 가능 여부
    private DeliveryStatus deliveryStatus;
    private String courier;
    private String trackingNumber;
    private boolean cancellable;
    private String errorMessage;
}




