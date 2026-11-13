package myshop.shop.dto.cart;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter @Setter
@ToString(of = {"cartNo", "itemNo", "itemOptionNo", "name", "imagePath", "count", "totalStock", "optionStock", "originalPrice", "optionPrice", "discountPer", "optionName", "price"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManageCartDto {
    private Long cartNo;            // Cart
    private Long itemNo;            // Item
    private Long itemOptionNo;      // ItemOption
    private String name;            // Item
    private String imagePath;       // ItemImage
    private int count;              // Cart
    private int totalStock;         // Item
    private int optionStock;        // ItemOption
    private BigDecimal originalPrice;      // Item
    private BigDecimal optionPrice;        // ItemOption, null존재
    private BigDecimal discountPer;        // Item
    private String optionName;      // ItemOption, null존재

    private BigDecimal price;       // 계산된 개당가격(할인된 가격 + 옵션 추가금액)

}
