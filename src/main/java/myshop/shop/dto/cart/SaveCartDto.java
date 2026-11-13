package myshop.shop.dto.cart;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = {"itemNo", "memberNo", "itemOptionNo", "count"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SaveCartDto {
    private Long itemNo;
    private Long memberNo;
    private Long itemOptionNo;
    private int count;

}