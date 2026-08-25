package myshop.shop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.item.DetailItemDto;
import myshop.shop.dto.member.LoginCheckMemberDto;
import myshop.shop.dto.review.ReviewScoreDto;
import myshop.shop.dto.review.SearchReviewDto;
import myshop.shop.entity.inquiry.InquiryCategory;
import myshop.shop.entity.inquiry.InquiryStatus;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemOption;
import myshop.shop.repository.Item.ItemOptionRepository;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.service.InquiryService;
import myshop.shop.service.ItemService;
import myshop.shop.service.ReviewService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_MEMBER;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeItemController {

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final ReviewService reviewService;
    private final InquiryService inquiryService;

    /**
     * 상품 상세 폼
     */
    @GetMapping("/item")
    public String itemForm(@PageableDefault(size = 5) Pageable pageable,
            @RequestParam("itemNo") Long itemNo, Model model) {

        DetailItemDto detailItemDto = itemService.getDetailItem(itemNo);
        detailItemDto.setItemNo(itemNo);

        SearchReviewDto searchReviewDto = new SearchReviewDto();
        searchReviewDto.setSortType("latest");
        Page<DetailItemReviewDto> detailItemReviewDtoPage = reviewService.itemDetailReview(pageable, itemNo, searchReviewDto);
        ReviewScoreDto reviewScoreDto = reviewService.itemDetailReviewScore(itemNo);

        Page<DetailItemInquiryDto> detailItemInquiryDtoPage = inquiryService.getItemDetailInquiry(pageable, itemNo);

        //조회수 증가
        itemService.addViewCount(itemNo);

        log.info("detailItemReviewDtoPage={}", detailItemReviewDtoPage);
        log.info("reviewScoreDto={}", reviewScoreDto);
        log.info("detailItemInquiryDtoPage={}", detailItemInquiryDtoPage);
        log.info("itemNo={}, 조회수 1증가", itemNo);

        model.addAttribute("detailItemInquiryDtoPage", detailItemInquiryDtoPage);
        model.addAttribute("detailItemReviewDtoPage", detailItemReviewDtoPage);
        model.addAttribute("reviewScoreDto", reviewScoreDto);
        model.addAttribute("detailItemDto", detailItemDto);
        return "shop/item_detail";
    }
    @Getter @Setter
    @ToString
    public static class DetailItemReviewDto {
        private Long reviewNo;
        private String optionName;
        private int count;
        private Long score;
        private String content;
        private String maskedMemberId;
        private String memberName;
        private Long goodCount;

        public DetailItemReviewDto() {
        }
    }
    @Getter @Setter
    @ToString
    public static class DetailItemInquiryDto {
        private Long memberNo;
        private String itemName;
        private String optionName;
        private InquiryCategory inquiryCategory;
        private String title;
        private String content;
        private InquiryStatus inquiryStatus;
        private String answerContent;

        public DetailItemInquiryDto() {
        }
    }


    /**
     * 상품리뷰 정렬/별점/페이징
     * 상품 상세 -> 상품리뷰
     * /item/reviews&itemNo=1&review_page=1&sortType=latest&score=4
     */
    @GetMapping("/item/reviews")
    @ResponseBody
    public Page<DetailItemReviewDto> searchItemReview(@RequestParam("itemNo") Long itemNo,
                                                      @ModelAttribute SearchReviewDto searchReviewDto, @Qualifier("review") Pageable pageable) {
        log.info("Review page, searchReviewDto={}, pageable={}", searchReviewDto, pageable);
        return reviewService.itemDetailReview(pageable, itemNo, searchReviewDto);
    }


    /**
     * 상품문의 페이징(더보기 버튼 페이징)
     * /item/inquiries&itemNo=1&inquiry_page=1
     */
    @GetMapping("/item/inquiries")
    @ResponseBody
    public Page<DetailItemInquiryDto> searchItemInquiry(@RequestParam("itemNo") Long itemNo, @Qualifier("inquiry") Pageable pageable) {
        log.info("inquiry page, pageable={}", pageable);
        return inquiryService.getItemDetailInquiry(pageable, itemNo);
    }


    /**
     * 도움이 돼요 버튼
     * 상품상세 -> 상품 리뷰
     */
    @PostMapping("/item/review/good/{reviewNo}")
    @ResponseBody
    public ResponseEntity<Long> addGoodCount(@PathVariable Long reviewNo, @RequestParam Boolean like) {
        Long goodCount = reviewService.toggleGoodCount(reviewNo, like);
        return ResponseEntity.ok(goodCount);
    }


    /**
     * 상품 상세 폼 -> 바로 구매(1. 로그인 체크, 재고 확인, 재고 선점)
     */
    @PostMapping("/item/checkDirectOrder")
    @ResponseBody
    public String checkDirectOrder(@RequestBody CheckDirectOrderDto checkDirectOrderDto, HttpServletRequest request, Model model) {
        // 로그인 체크
        HttpSession session = request.getSession();
        if (session == null || session.getAttribute(LOGIN_MEMBER) == null) {
            return "loginFail";
        }
        checkDirectOrderDto.setMemberNo(((LoginCheckMemberDto) session.getAttribute(LOGIN_MEMBER)).getNo());

        // 재고 확인
        int stock;
        if (checkDirectOrderDto.getItemOptionNo() == null) {
            Item item = itemRepository.findById(checkDirectOrderDto.getItemNo()).orElse(null);
            stock = item.getTotalStock();
        } else {
            ItemOption itemOption = itemOptionRepository.findById(checkDirectOrderDto.getItemOptionNo()).orElse(null);
            stock = itemOption.getOptionStock();
        }
        if (stock < checkDirectOrderDto.count) {
            return "soldOut";
        }

        // 구매 상품 재고 선점
        itemService.reserveStock(checkDirectOrderDto);

        return "ok";
    }
    @Getter @Setter
    @ToString(of = {"itemNo", "memberNo", "itemOptionNo", "itemImageNo", "count"})
    public static class CheckDirectOrderDto {
        private Long itemNo;
        private Long memberNo;
        private Long itemOptionNo;      // null or Data
        private Long itemImageNo;
        private int count;

        public CheckDirectOrderDto() {
        }

        public CheckDirectOrderDto(Long itemNo, Long memberNo, Long itemOptionNo, Long itemImageNo, int count) {
            this.itemNo = itemNo;
            this.memberNo = memberNo;
            this.itemOptionNo = itemOptionNo;
            this.itemImageNo = itemImageNo;
            this.count = count;
        }
    }
}
