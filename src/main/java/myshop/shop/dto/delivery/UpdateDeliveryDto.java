package myshop.shop.dto.delivery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.delivery.DeliveryStatus;

@Getter @Setter
@ToString(of = {"orderItemNo", "deliveryStatus", "courier", "trackingNumber"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateDeliveryDto {
    private Long orderItemNo;
    private DeliveryStatus deliveryStatus;
    private String courier;
    private String trackingNumber;

}
