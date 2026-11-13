package myshop.shop.controller.sellerWeb;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.item.*;
import myshop.shop.dto.seller.LoginCheckSellerDto;
import myshop.shop.entity.item.ItemStatus;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.service.FileService;
import myshop.shop.service.ItemImageService;
import myshop.shop.service.ItemImageService.ImagePath;
import myshop.shop.service.SellerService;
import myshop.shop.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_SELLER;


@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final SellerService sellerService;
    private final FileService fileService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final ItemImageService itemImageService;

    /**
     * 상품 관리 폼 (상품관리메뉴, 페이지, 검색)
     */
    @GetMapping("/seller/item_manage")
    public String itemManageForm(Pageable pageable, SearchItemDto searchItemDto,
                                 HttpServletRequest request, Model model) {
        LoginCheckSellerDto loginCheckSellerDto = (LoginCheckSellerDto) request.getSession().getAttribute(LOGIN_SELLER);
        searchItemDto.setSellerNo(loginCheckSellerDto.getNo());

        Page<ManageItemDto> manageItemDtoList = itemService.findBySearchItemDto(pageable, searchItemDto);

        model.addAttribute("searchItemDto", searchItemDto);
        model.addAttribute("manageItemDtoList", manageItemDtoList);
        return "seller/item/item_manage";
    }



    /**
     * 상품 관리 -> 새 상품 등록 폼
     */
    @GetMapping("/seller/item_new")
    public String itemNewForm(Model model) {
        model.addAttribute("addItemDto", new AddItemDto());
        return "seller/item/item_new";
    }



    /**
     * 새 상품 등록 폼 -> 상품 등록 완료
     */
    @PostMapping("/seller/item_new")
    public String itemNew(@ModelAttribute("addItemDto") AddItemDto addItemDto, HttpServletRequest request) throws IOException {
        LoginCheckSellerDto loginCheckSellerDto = (LoginCheckSellerDto) request.getSession().getAttribute(LOGIN_SELLER);
        addItemDto.setSellerNo(loginCheckSellerDto.getNo());

        // 이미지 저장
        addItemDto.setMainImagePath(fileService.storeFile(addItemDto.getMainImage()));
        addItemDto.setSubImagesPath(fileService.storeFiles(addItemDto.getSubImages()));

        List<AddItemOptionDto> addItemOptionDtoList = addItemDto.getAddItemOptionDtoList();

        for (AddItemOptionDto addItemOptionDto : addItemOptionDtoList) {
            addItemDto.addTotalStock(addItemOptionDto.getOptionStock());
        }

        log.info("addItemDto={}", addItemDto);
        itemService.saveItem(addItemDto);
        return "redirect:/seller/item_manage";
    }



    /**
     * 상품 관리 폼 -> 수정 데이터
     */
    @GetMapping("/seller/item_manage/modify")
    @ResponseBody
    public ModifyItemDto itemModifyData(@RequestParam("itemNo") Long itemNo) {
        ModifyItemDto itemModifyData = itemService.getItemModifyData(itemNo);
        itemModifyData.setItemNo(itemNo);
        log.info("itemModifyData={}", itemModifyData);
        return itemModifyData;
    }



    /**
     * 상품 관리 폼 -> 수정 완료
     */
    @PostMapping("/seller/item_manage/modify")
    public String ItemModify(@ModelAttribute("modifyItemDto") ModifyItemDto modifyItemDto) throws IOException {
        log.info("ItemModify={}", modifyItemDto);

        if (checkChangeImage(modifyItemDto.getMainImage(), modifyItemDto.getSubImages())) {
            log.info("아이템 삭제, {} \n {}", modifyItemDto.getMainImage(), modifyItemDto.getSubImages());

            // 이미지 삭제
            ImagePath imagePath = itemImageService.getItemImageByIsMain(modifyItemDto.getItemNo());
            if (imagePath.getMainPath() != null) {
                fileService.removeFile(imagePath.getMainPath());
            }
            for (String s : imagePath.getSubPath()) {
                fileService.removeFile(s);
            }

            // 이미지 저장
            modifyItemDto.setMainImagePath(fileService.storeFile(modifyItemDto.getMainImage()));
            modifyItemDto.setSubImagesPath(fileService.storeFiles(modifyItemDto.getSubImages()));
        }

        itemService.itemModify(modifyItemDto);

        return "redirect:/seller/item_manage";
    }

    // 변경O true, 변경X false
    public boolean checkChangeImage(MultipartFile mainImage, List<MultipartFile> subImages) {
        return (mainImage != null && !mainImage.isEmpty()) || (subImages != null && !subImages.isEmpty() && !subImages.get(0).isEmpty());
    }



    /**
     * 상품 관리 폼 -> 일괄 수정
     */
    @PostMapping("/seller/item_manage/bulk_modify")
    public String bulkModify(@RequestBody BulkModifyItemDto bulkModifyItemDto) {
        log.info("bulkModifyItemDto={}", bulkModifyItemDto);
        itemService.bulkModify(bulkModifyItemDto);

        return "redirect:/seller/item_manage";
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(of = {"itemNos", "itemStatus", "discountPer"})
    public static class BulkModifyItemDto {
        private List<Long> itemNos;
        private ItemStatus itemStatus;
        private BigDecimal discountPer;
    }



    /**
     * 상품 1개 삭제
     */
    @PostMapping("/seller/item_manage/remove")
    @ResponseBody
    public String removeItem(@RequestParam("itemNo") Long itemNo) {
        if (itemService.removeItem(itemNo)) {
            return "ok";
        }
        return "no";
    }

}
