package myshop.shop.dto.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemOption;
import myshop.shop.entity.item.ItemStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString(of = {"itemNo", "name", "price", "totalStock", "itemStatus", "discountPer", "modifyItemOptionDtoList", "mainImage", "subImages"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifyItemDto {
    private Long itemNo;
    private String name;
    private BigDecimal price;

    private int totalStock;
    private ItemStatus itemStatus;
    private BigDecimal discountPer;

    @Builder.Default
    private List<ModifyItemOptionDto> modifyItemOptionDtoList = new ArrayList<>();

    private MultipartFile mainImage;
    private String mainImagePath;
    @Builder.Default
    private List<MultipartFile> subImages = new ArrayList<>();
    @Builder.Default
    private List<String> subImagesPath = new ArrayList<>();
    private String content;

}
