package myshop.shop.dto.member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.ToString;
import myshop.shop.entity.member.Member;
import org.springframework.ui.Model;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_MEMBER;

@Getter
@ToString(of = {"no", "id", "name"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginCheckMemberDto {
    private Long no;
    private String id;
    private String name;

    public boolean loginCheck(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            model.addAttribute("isLogin", false);
            return false;
        }

        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) session.getAttribute(LOGIN_MEMBER);

        if (loginCheckMemberDto == null) {
            model.addAttribute("isLogin", false);
            return false;
        }

        model.addAttribute("loginCheckMemberDto", loginCheckMemberDto);
        model.addAttribute("isLogin", true);
        return true;
    }

    public LoginCheckMemberDto loginCheck2(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            model.addAttribute("isLogin", false);
            return null;
        }

        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) session.getAttribute(LOGIN_MEMBER);

        if (loginCheckMemberDto == null) {
            model.addAttribute("isLogin", false);
            return null;
        }

        model.addAttribute("loginCheckMemberDto", loginCheckMemberDto);
        model.addAttribute("isLogin", true);
        return loginCheckMemberDto;
    }
}
