package myshop.shop.controller.memberWeb;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.cart.ManageCartDto;
import myshop.shop.dto.cart.SaveCartDto;
import myshop.shop.dto.member.LoginCheckMemberDto;
import myshop.shop.entity.Cart;
import myshop.shop.service.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_MEMBER;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    /**
     * 상품 상세 폼 -> 장바구니 담기
     */
    @PostMapping("/cart/save")
    @ResponseBody
    public boolean cartSave(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, @RequestBody SaveCartDto saveCartDto, HttpServletRequest request) {
 /*       LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        if (loginCheckMemberDto == null) {
            return false;
        }*/

        saveCartDto.setMemberNo(loginCheckMemberDto.getNo());
        cartService.saveCart(saveCartDto);
        return true;
    }


    /**
     * 장바구니 폼
     */
    @GetMapping("/myPage/cart")
    public String cartForm(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, HttpServletRequest request, Model model) {
//        new LoginCheckMemberDto().loginCheck(request, model);
//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);

        Long memberNo = loginCheckMemberDto.getNo();
        List<ManageCartDto> manageCartDtoList = cartService.findAllCart(memberNo);

        model.addAttribute("manageCartDtoList", manageCartDtoList);
        return "member/mypage/cart";
    }


    /**
     * 장바구니 폼 -> 수량 변경
     */
    @PostMapping("/myPage/cart/updateCount")
    @ResponseBody
    public boolean cartUpdateCount(@RequestBody UpdateCartCount updateCartCount) {
        cartService.updateCount(updateCartCount.getCartNo(), updateCartCount.getCount());
        return true;
    }
    @Getter @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UpdateCartCount {
        private Long cartNo;
        private int count;
    }


    /**
     * 장바구니 폼 -> 삭제
     */
    @PostMapping("/myPage/cart/removeCart")
    @ResponseBody
    public boolean cartRemove(@RequestParam("cartNo") Long cartNo) {
         cartService.removeCart(cartNo);
         return true;
    }


    /**
     * 장바구니 폼 -> 선택 삭제
     */
    @PostMapping("/myPage/cart/removeCartList")
    @ResponseBody
    public boolean cartListRemove(@RequestBody List<Long> cartNoList) {
        for (Long cartNo : cartNoList) {
            cartService.removeCart(cartNo);
        }
        return true;
    }




}
