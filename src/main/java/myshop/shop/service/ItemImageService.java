package myshop.shop.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.item.ModifyItemDto;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemImage;
import myshop.shop.repository.Item.ItemImageRepository;
import myshop.shop.repository.Item.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemImageService {
    private final ItemImageRepository itemImageRepository;
    private final ItemRepository itemRepository;


    /**
     * 이미지 주소 가져오기
     */
    public ImagePath getItemImageByIsMain(Long itemNo) {
        Item itemProxy = itemRepository.getReferenceById(itemNo);
        ItemImage main = itemImageRepository.findItemImageByIsMain(itemProxy, true).orElse(null);
        List<ItemImage> sub = itemImageRepository.findItemImageListByIsMain(itemProxy, false);
        return ImagePath.builder()
                .mainPath(main == null ? null : main.getImageUrl())
                .subPath(sub.stream().map(ItemImage::getImageUrl).collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ImagePath {
        String mainPath;
        @Builder.Default
        List<String> subPath = new ArrayList<>();
    }
}
