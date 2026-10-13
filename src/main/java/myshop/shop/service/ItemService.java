package myshop.shop.service;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.controller.memberWeb.OrderController;
import myshop.shop.controller.sellerWeb.ItemController.BulkModifyItemDto;
import myshop.shop.dto.item.*;
import myshop.shop.entity.Seller;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemImage;
import myshop.shop.entity.item.ItemOption;
import myshop.shop.entity.item.ItemStatus;
import myshop.shop.repository.Item.ItemImageRepository;
import myshop.shop.repository.Item.ItemOptionRepository;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.repository.cart.CartRepository;
import myshop.shop.repository.seller.SellerRepository;
import myshop.shop.service.ItemImageService.ImagePath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static myshop.shop.service.RedisService.RESERVE_KEY;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final SellerRepository sellerRepository;
    private final EntityManager em;
    private final ItemImageRepository itemImageRepository;
    private final FileService fileService;
    private final ItemImageService itemImageService;
    private final CartRepository cartRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.ttl.timeout}")
    private int timeoutSeconds;
    /**
     * 상품 등록
     */
    public void saveItem(AddItemDto addItemDto) {
        if (addItemDto.getItemStatus() == null) {
            addItemDto.setItemStatus(ItemStatus.승인대기);
        }

        Seller sellerProxy = sellerRepository.getReferenceById(addItemDto.getSellerNo());
        Item item = new Item(sellerProxy,
                addItemDto.getName(),
                addItemDto.getItemCategory(),
                addItemDto.getPrice(),
                addItemDto.getTotalStock(),
                addItemDto.getDiscountPer(),
                addItemDto.getContent(),
                addItemDto.getItemStatus());
        itemRepository.save(item);

        //상품 옵션 저장
        List<AddItemOptionDto> addItemOptionDtoList = addItemDto.getAddItemOptionDtoList();
        for (AddItemOptionDto addItemOptionDto : addItemOptionDtoList) {
            itemOptionRepository.save(new ItemOption(item, addItemOptionDto.getName(), addItemOptionDto.getAdditionalPrice(), addItemOptionDto.getOptionStock()));
        }

        //상품 이미지 저장
        int sortOrder = 1;
        String mainImagePath = addItemDto.getMainImagePath();
        itemImageRepository.save(new ItemImage(item, mainImagePath, true, sortOrder++));

        List<String> subImagePathList = addItemDto.getSubImagesPath();
        for (String subImagePath : subImagePathList) {
            itemImageRepository.save(new ItemImage(item, subImagePath, false, sortOrder++));
        }
    }


    /**
     * 상품 등록 테스트 전용
     */
    public void saveItemForTest(AddItemDto addItemDto) {
        if (addItemDto.getItemStatus() == null) {
            addItemDto.setItemStatus(ItemStatus.승인대기);
        }

        Seller sellerProxy = sellerRepository.getReferenceById(addItemDto.getSellerNo());
        Item item = new Item(sellerProxy,
                addItemDto.getName(),
                addItemDto.getItemCategory(),
                addItemDto.getPrice(),
                addItemDto.getTotalStock(),
                addItemDto.getDiscountPer(),
                addItemDto.getContent(),
                addItemDto.getItemStatus(),
                addItemDto.getViewCount());
        itemRepository.save(item);

        //상품 옵션 저장
        List<AddItemOptionDto> addItemOptionDtoList = addItemDto.getAddItemOptionDtoList();
        for (AddItemOptionDto addItemOptionDto : addItemOptionDtoList) {
            itemOptionRepository.save(new ItemOption(item, addItemOptionDto.getName(), addItemOptionDto.getAdditionalPrice(), addItemOptionDto.getOptionStock()));
        }

        //상품 이미지 저장
        int sortOrder = 1;
        String mainImagePath = addItemDto.getMainImagePath();
        itemImageRepository.save(new ItemImage(item, mainImagePath, true, sortOrder++));

        List<String> subImagePathList = addItemDto.getSubImagesPath();
        for (String subImagePath : subImagePathList) {
            itemImageRepository.save(new ItemImage(item, subImagePath, false, sortOrder++));
        }
    }




    /**
     * 판매자 상품 전체 조회
     */
    public List<ManageItemDto> findAllByNo(Long sellerNo) {
        Seller sellerProxy = sellerRepository.getReferenceById(sellerNo);
        List<Item> itemList = itemRepository.findBySeller(sellerProxy);
        return itemList.stream()
                .map(ManageItemDto::new)
                .collect(Collectors.toList());

    }



    /**
     * 판매자 상품 조회(페이징)
     */
    public Page<ManageItemDto> findBySearchItemDto(Pageable pageable, SearchItemDto searchItemDto) {
        return itemRepository.searchItemPage(pageable, searchItemDto);
    }



    /**
     * 상품 수정 데이터 불러오기
     */
    public ModifyItemDto getItemModifyData(Long itemNo) {
        Item item = itemRepository.findById(itemNo).orElse(null);
        List<ItemOption> itemOptionList = itemOptionRepository.findByItem(item);

        return new ModifyItemDto(item, itemOptionList);
    }



    /**
     * 상품 수정
     * Cart : CascadeType.REMOVE, orphanRemoval = true
     */
    public void itemModify(ModifyItemDto modifyItemDto) {
        Item item = em.createQuery("select i from Item i where i.no=:itemNo", Item.class)
                .setParameter("itemNo", modifyItemDto.getItemNo())
                .getSingleResult();
        item.updateName(modifyItemDto.getName());
        item.updatePrice(modifyItemDto.getPrice());
        item.updateTotalStock(modifyItemDto.getTotalStock());
        item.updateDiscount(modifyItemDto.getDiscountPer());
        item.updateContent(modifyItemDto.getContent());
        item.updateItemStatus(modifyItemDto.getItemStatus());

        em.flush();
        em.clear();

        Item itemProxy = itemRepository.getReferenceById(modifyItemDto.getItemNo());


        for (ItemOption itemOption : itemOptionRepository.findByItem(itemProxy)) {
            itemOptionRepository.delete(itemOption);
        }

        for (ModifyItemOptionDto modifyItemOptionDto : modifyItemDto.getModifyItemOptionDtoList()) {
            itemOptionRepository.save(new ItemOption(itemProxy,
                    modifyItemOptionDto.getName(),
                    modifyItemOptionDto.getAdditionalPrice(),
                    modifyItemOptionDto.getOptionStock()));
        }

        // 수정한 값 있으면 ItemImage 삭제 후, 데이터 넣기
        if (checkChangeImage(modifyItemDto.getMainImage(), modifyItemDto.getSubImages())) {
            itemImageRepository.deleteItemImageByItem(item);
            int sort = 1;
            itemImageRepository.save(new ItemImage(itemProxy,
                    modifyItemDto.getMainImagePath(),
                    true, sort++));
            for (String subImagePath : modifyItemDto.getSubImagesPath()) {
                itemImageRepository.save(new ItemImage(itemProxy,
                        subImagePath, false, sort++));
            }
        }
    }
    /**
     * @return 변경O true, 변경X false
     */
    public boolean checkChangeImage(MultipartFile mainImage, List<MultipartFile> subImages) {
        return (mainImage != null && !mainImage.isEmpty()) || (subImages != null && !subImages.isEmpty() && !subImages.get(0).isEmpty());
    }



    /**
     * 일괄 수정
     */
    public void bulkModify(BulkModifyItemDto bulkModifyItemDto) {
        itemRepository.bulkItemStatusDiscount(bulkModifyItemDto);
    }



    /**
     * 상품 삭제
     * ItemOption, ItemImage : CascadeType.All, orphanRemoval=true
     */
    public boolean removeItem(Long itemNo) {
        // 저장된 이미지 삭제
        ImagePath imagePath = itemImageService.getItemImageByIsMain(itemNo);
        if (imagePath.getMainPath() != null) {
            fileService.removeFile(imagePath.getMainPath());
        }
        for (String s : imagePath.getSubPath()) {
            fileService.removeFile(s);
        }
        Item item = itemRepository.findById(itemNo).orElse(null);
        if (item == null) {
            return false;
        }
        itemRepository.delete(item);
        em.flush();
        em.clear();
        return true;
    }



    /**
     * 메인 화면 상품 출력
     */
    public List<MainItemDto> getMainItem(Long limit) {
        List<MainItemDto> mainItemDtoList = itemRepository.findMainItem(limit);
        for (MainItemDto mainItemDto : mainItemDtoList) {
            mainItemDto.setDiscountedPrice(getDiscountedPrice(mainItemDto.getPrice(), mainItemDto.getDiscountPer()));
        }
        log.info("mainItemDtoList={}",mainItemDtoList);
        return mainItemDtoList;
    }
    /**
     * 할인된 가격 계산
     * 할인가 소수점 이하 올림, 예)100.23원 -> 101원 할인
     */
    public static BigDecimal getDiscountedPrice(BigDecimal originPrice, BigDecimal discount) {
        BigDecimal discountPer = discount.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return originPrice.subtract(originPrice.multiply(discountPer).setScale(0, RoundingMode.CEILING));
    }


    /**
     * 상품 검색 화면
     * 홈 헤더 -> 검색
     */
    public Page<MainItemDto> getSearchItem(Pageable pageable, String keyword) {
        log.info("keyword={}", keyword);
        Page<MainItemDto> searchItemList = itemRepository.findSearchItem(pageable, keyword);
        for (MainItemDto searchItem : searchItemList) {
            searchItem.setDiscountedPrice(getDiscountedPrice(searchItem.getPrice(), searchItem.getDiscountPer()));
        }
        return searchItemList;
    }

    /**
     * 상품 클릭 -> 상품 상세 가져오기
     */
    public DetailItemDto getDetailItem(Long itemNo) {
        DetailItemDto detailItemDto = itemRepository.findDetailItem(itemNo);
        Map<Long, String> itemImageMap = itemRepository.getImageUrls(itemNo);
        detailItemDto.setItemImageMap(itemImageMap);

        detailItemDto.setDiscountedPrice(getDiscountedPrice(detailItemDto.getPrice(), detailItemDto.getDiscountPer()));
        return detailItemDto;
    }



    /**
     * 상품 조회수 증가
     */
    public void addViewCount(Long itemNo) {
        itemRepository.updateViewCountByNo(itemNo);
    }



    /**
     * 상품 재고 선점
     * 장바구니 폼 -> 구매하기
     */
    public void reserveStock(List<Long> cartNoList, Long memberNo) {

        List<ItemStockUpdateDto> itemStockUpdateDtoList = new ArrayList<>();
        Map<Long, ItemStockUpdateDto> redisVal = new HashMap<>();

        log.info("reserveStock start, cartNoList={} memberNo={}", cartNoList, memberNo);

        for (Long cartNo : cartNoList) {
            ItemStockUpdateDto itemStockUpdate = cartRepository.getItemStockUpdate(cartNo);
            redisVal.put(cartNo, itemStockUpdate);
            itemStockUpdateDtoList.add(itemStockUpdate);
        }

        for (ItemStockUpdateDto itemStockUpdateDto : itemStockUpdateDtoList) {
            Long itemNo = itemStockUpdateDto.getItemNo();
            Long optionNo = itemStockUpdateDto.getOptionNo();
            int count = itemStockUpdateDto.getCount();


            Item item = em.createQuery("select i from Item i where i.no=:itemNo", Item.class)
                    .setParameter("itemNo", itemNo)
                    .getSingleResult();
            if (optionNo != null) {
                ItemOption itemOption = em.createQuery("select io from ItemOption io where io.no=:optionNo", ItemOption.class)
                        .setParameter("optionNo", optionNo)
                        .getSingleResult();
                itemOption.subOptionStock(count);
            }
            item.subTotalStock(count);
            em.flush();
            em.clear();
        }

        // redis TTL
        String redisKey = RESERVE_KEY + memberNo + ":cart:" + cartNoList;

        redisTemplate.opsForValue().set(
                redisKey,
                redisVal,
                timeoutSeconds,
                TimeUnit.SECONDS
        );
        log.info("장바구니 재고 선점 완료, Redis 저장 ({}:{}), {}초 후 만료", redisKey, redisVal, timeoutSeconds);
    }
    @Getter @Setter
    @ToString(of = {"itemNo", "optionNo", "count"})
    public static class ItemStockUpdateDto {
        private Long itemNo;
        private Long optionNo;
        private int count;

        public ItemStockUpdateDto() {
        }
    }


    /**
     * 상품 제고 선점
     * 상품상세 폼 -> 바로구매
     * DirectOrderDto(itemNo=3, memberNo=5, itemOptionNo=null, itemImageNo=7, count=6)
     * DirectOrderDto(itemNo=4, memberNo=5, itemOptionNo=7, itemImageNo=9, count=1)
     */
    public void reserveStock(OrderController.CheckDirectOrderDto checkDirectOrderDto) {

        Long optionNo = checkDirectOrderDto.getItemOptionNo();
        Long itemNo = checkDirectOrderDto.getItemNo();
        int count = checkDirectOrderDto.getCount();
        Long memberNo = checkDirectOrderDto.getMemberNo();

        log.info("reserveStock start, memberNo={} itemInfo={}", memberNo, checkDirectOrderDto);

        Item item = em.createQuery("select i from Item i where i.no=:itemNo", Item.class)
                .setParameter("itemNo", itemNo)
                .getSingleResult();
        if (optionNo != null) {
            ItemOption itemOption = em.createQuery("select io from ItemOption io where io.no=:optionNo", ItemOption.class)
                    .setParameter("optionNo", optionNo)
                    .getSingleResult();
            itemOption.subOptionStock(count);
        }
        item.subTotalStock(count);
        em.flush();
        em.clear();

        // redis TTL
        String redisKey = RESERVE_KEY + memberNo + ":direct:" + itemNo + ":" + optionNo + ":" + count;
        // "itemNo": "123", "optionNo": null, "stock": 10
        Map<String, Object> redisVal = new HashMap<>();
        redisVal.put("itemNo", itemNo);
        redisVal.put("optionNo", optionNo == null ? 0 : optionNo);
        redisVal.put("stock", count);
        redisTemplate.opsForValue().set(
                redisKey,
                redisVal,
                timeoutSeconds,
                TimeUnit.SECONDS
        );
        log.info("장바구니 재고 선점 완료, Redis 저장 ({}:{}), {}초 후 만료", redisKey, redisVal, timeoutSeconds);
    }



    /**
     * 결제시간 만료 롤백 (장바구니)
     */
    public void cartRollbackStock(List<Long> cartNoList) {
        List<ItemStockUpdateDto> itemStockUpdateDtoList = new ArrayList<>();

        for (Long cartNo : cartNoList) {
            ItemStockUpdateDto itemStockUpdate = cartRepository.getItemStockUpdate(cartNo);
            itemStockUpdateDtoList.add(itemStockUpdate);
        }

        for (ItemStockUpdateDto itemStockUpdateDto : itemStockUpdateDtoList) {
            Long itemNo = itemStockUpdateDto.getItemNo();
            Long optionNo = itemStockUpdateDto.getOptionNo();
            int count = itemStockUpdateDto.getCount();


            Item item = em.createQuery("select i from Item i where i.no=:itemNo", Item.class)
                    .setParameter("itemNo", itemNo)
                    .getSingleResult();
            if (optionNo != null) {
                ItemOption itemOption = em.createQuery("select io from ItemOption io where io.no=:optionNo", ItemOption.class)
                        .setParameter("optionNo", optionNo)
                        .getSingleResult();
                itemOption.addOptionStock(count);
            }
            item.addTotalStock(count);
            em.flush();
            em.clear();
        }
    }


    /**
     * 결제시간 만료 롤백 (바로 구매)
     */
    public void directRollbackStock(Long itemNo, Long optionNo, int count) {
        Item item = em.createQuery("select i from Item i where i.no=:itemNo", Item.class)
                .setParameter("itemNo", itemNo)
                .getSingleResult();
        if (optionNo != null) {
            ItemOption itemOption = em.createQuery("select io from ItemOption io where io.no=:optionNo", ItemOption.class)
                    .setParameter("optionNo", optionNo)
                    .getSingleResult();
            itemOption.addOptionStock(count);
        }
        item.addTotalStock(count);
        em.flush();
        em.clear();
    }




}
