package myshop.shop.repository.Item;

import myshop.shop.controller.sellerWeb.ItemController;
import myshop.shop.dto.item.DetailItemDto;
import myshop.shop.dto.item.MainItemDto;
import myshop.shop.dto.item.ManageItemDto;
import myshop.shop.dto.item.SearchItemDto;
import myshop.shop.entity.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static myshop.shop.entity.item.QItem.item;

public interface ItemRepositoryCustom {
    /**
     * 상품 관리 데이터 불러오기
     * Item & ItemImage -> ManageItemDto
     */
    Page<ManageItemDto> searchItemPage(Pageable pageable, SearchItemDto searchItemDto);

    /**
     * 상품 일괄 수정
     */
    long bulkItemStatusDiscount(ItemController.BulkModifyItemDto bulkModifyItemDto);

    /**
     * 메인 화면 상품 출력
     * viewCount desc
     */
    List<MainItemDto> findMainItem(Long limit);

    /**
     * 상품 클릭 -> 상품 상세 출력
     * Item & ItemOption -> DetailItemDto
     */
    DetailItemDto findDetailItem(Long itemNo);

    /**
     * 이미지 주소들 가져오기
     * sortOrder ASC
     */
    Map<Long, String> getImageUrls(Long itemNo);

    /**
     * 판매상품 불러오기
     * 판매자 페이지 -> 고객 문의 관리
     */
    List<Long> getSellerItemNo(Long sellerNo);
}
