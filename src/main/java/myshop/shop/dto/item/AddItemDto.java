package myshop.shop.dto.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.item.ItemCategory;
import myshop.shop.entity.item.ItemStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString(of = {"sellerNo", "name", "itemCategory", "price", "totalStock", "discountPer", "addItemOptionDtoList", "mainImage", "mainImagePath",
"subImages", "subImagesPath", "content", "itemStatus", "useOptions"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddItemDto {
    private Long sellerNo;

    private String name;
    private ItemCategory itemCategory;

    private BigDecimal price;
    private int totalStock;
    private BigDecimal discountPer;
    @Builder.Default
    private List<AddItemOptionDto> addItemOptionDtoList = new ArrayList<>();

    private MultipartFile mainImage;
    private String mainImagePath;
    @Builder.Default
    private List<MultipartFile> subImages = new ArrayList<>();
    @Builder.Default
    private List<String> subImagesPath = new ArrayList<>();
    private String content;

    private ItemStatus itemStatus;
    private boolean useOptions;     //옵션 사용 체크박스

    private Long viewCount;     // 테스트 전용(완성 후 삭제)

    public void updateAddItemOptionDtoList(AddItemOptionDto addItemOptionDto) {
        this.addItemOptionDtoList.add(addItemOptionDto);
    }

    public void addTotalStock(int optionStock) {
        this.totalStock += optionStock;
    }
}
