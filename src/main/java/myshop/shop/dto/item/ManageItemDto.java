package myshop.shop.dto.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@ToString(of = {"itemNo", "mainImagePath", "name", "price", "totalStock", "itemStatus", "createdDate"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManageItemDto {
    private Long itemNo;
    private String mainImagePath;
    private String name;
    private BigDecimal price;
    private int totalStock;
    private ItemStatus itemStatus;
    private LocalDateTime createdDate;

}
