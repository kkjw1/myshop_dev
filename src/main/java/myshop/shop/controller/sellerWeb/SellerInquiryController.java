package myshop.shop.controller.sellerWeb;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.controller.memberWeb.MemberController;
import myshop.shop.dto.inquiry.ManageInquiryDto;
import myshop.shop.dto.inquiry.SearchInquiryDto;
import myshop.shop.dto.inquiry.UpdateInquiryDto;
import myshop.shop.dto.seller.LoginCheckSellerDto;
import myshop.shop.entity.inquiry.InquiryStatus;
import myshop.shop.service.InquiryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SellerInquiryController {

    private final InquiryService inquiryService;


    /**
     * 고객 문의 폼
     * 판매자 페이지 -> 고객 문의
     */
    @GetMapping("/seller/inquiry_manage")
    public String requestInquiryForm(Pageable pageable, HttpServletRequest request, Model model,
                                     @ModelAttribute SearchInquiryDto searchInquiryDto) {
        // todo: 판매자 페이지-> 고객 문의 페이지 기능
        LoginCheckSellerDto loginCheckSellerDto = (LoginCheckSellerDto) request.getSession().getAttribute(MemberController.SessionConst.LOGIN_SELLER);
        Long sellerNo = loginCheckSellerDto.getNo();

        Page<ManageInquiryDto> manageInquiryDtoPage = inquiryService.getManageInquiry(pageable, sellerNo, searchInquiryDto);

        Map<InquiryStatus, Long> countedInquiryStatus = inquiryService.countInquiryStatus(sellerNo);

        log.info("고객 문의 폼, pageable={} manageInquiryDtoPage={}", pageable, manageInquiryDtoPage);
        log.info("countedInquiryStatus={}", countedInquiryStatus);

        model.addAttribute("unansweredCount", countedInquiryStatus.get(InquiryStatus.답변대기));
        model.addAttribute("answeredCount", countedInquiryStatus.get(InquiryStatus.답변완료));
        model.addAttribute("manageInquiryDtoPage", manageInquiryDtoPage);
        return "seller/inquiry/inquiry_manage";
    }


    /**
     * 고객 문의 답변 작성
     * 고객 문의 폼 -> 답변 하기
     */
    @PostMapping("/seller/inquiry/reply")
    @ResponseBody
    public ResponseEntity<String> inquiryReply(@RequestBody UpdateInquiryDto updateInquiryDto) {
        inquiryService.replyInquiry(updateInquiryDto);
        return ResponseEntity.ok("success");
    }


}
