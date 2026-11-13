package myshop.shop.dto.seller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import myshop.shop.entity.Seller;
import org.springframework.ui.Model;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_SELLER;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginCheckSellerDto {

    private Long no;
    private String id;
    private String name;

    public boolean loginCheck(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            model.addAttribute("isLogin", false);
            return false;
        }

        LoginCheckSellerDto loginCheckSellerDto = (LoginCheckSellerDto) session.getAttribute(LOGIN_SELLER);

        if (loginCheckSellerDto == null) {
            model.addAttribute("isLogin", false);
            return false;
        }

        model.addAttribute("loginCheckSellerDto", loginCheckSellerDto);
        model.addAttribute("isLogin", true);
        return true;
    }
}
