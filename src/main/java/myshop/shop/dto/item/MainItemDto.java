package myshop.shop.dto.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemImage;

import java.math.BigDecimal;

@Getter @Setter
@ToString(of = {"mainImagePath", "name", "price", "discountPer", "viewCount", "reviewScore", "reviewCount"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MainItemDto {
    private Long itemNo;
    private String mainImagePath;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPer;
    private BigDecimal discountedPrice; // 부동소수점 오류 방지
    private Long viewCount;
    private Double reviewScore;
    private Long reviewCount;

}
