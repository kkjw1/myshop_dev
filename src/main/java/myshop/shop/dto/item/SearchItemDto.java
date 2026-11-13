package myshop.shop.dto.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import myshop.shop.entity.item.ItemStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchItemDto {
    private Long sellerNo;
    private Long itemNo;
    private String name;
    private ItemStatus itemStatus;

}
