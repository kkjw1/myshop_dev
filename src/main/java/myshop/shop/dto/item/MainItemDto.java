package myshop.shop.dto.item;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemImage;

import java.math.BigDecimal;

@Getter @Setter
@ToString(of = {"mainImagePath", "name", "price", "discountPer", "viewCount", "reviewScore", "reviewCount"})
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

    public MainItemDto() {
    }

    public MainItemDto(Long itemNo, String mainImagePath, String name, int price, int discountPer, Long viewCount) {
        this.itemNo = itemNo;
        this.mainImagePath = mainImagePath;
        this.name = name;
        this.price = BigDecimal.valueOf(price);
        this.discountPer = BigDecimal.valueOf(discountPer);
        this.viewCount = viewCount;
    }
    public MainItemDto(Long itemNo, String mainImagePath, String name, BigDecimal price, BigDecimal discountPer, Long viewCount) {
        this.itemNo = itemNo;
        this.mainImagePath = mainImagePath;
        this.name = name;
        this.price = price;
        this.discountPer = discountPer;
        this.viewCount = viewCount;
    }
}
