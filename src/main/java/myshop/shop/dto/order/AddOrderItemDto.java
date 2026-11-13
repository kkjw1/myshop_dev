package myshop.shop.dto.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter @Setter
@ToString(of = {"cartNo", "optionNo", "itemNo", "count", "discountedPrice", "totalPrice", "imageUrl", "itemName", "optionName"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddOrderItemDto {
    private Long cartNo;
    private Long optionNo;
    private Long itemNo;
    private int count;
    private BigDecimal discountedPrice;     // 할인된 가격
    private BigDecimal totalPrice;     // count * price
    private String imageUrl;
    private String itemName;
    private String optionName;

}
