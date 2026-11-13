package myshop.shop.dto.delivery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.order.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString(of = {"orderNoList", "orderStatus"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchUpdateOrderDeliveryDto {
    @Builder.Default
    private List<Long> orderNoList = new ArrayList<>();
    private OrderStatus orderStatus;

}
