package myshop.shop.controller;

import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.controller.memberWeb.MemberController;
import myshop.shop.dto.item.DetailItemDto;
import myshop.shop.dto.item.MainItemDto;
import myshop.shop.dto.member.LoginCheckMemberDto;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemOption;
import myshop.shop.repository.Item.ItemOptionRepository;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_MEMBER;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final ItemService itemService;

    /**
     * 홈 화면
     */
    @GetMapping({"/", "/home"})
    public String homeForm(HttpServletRequest request, Model model) {
        // 상품들 가져오기
        List<MainItemDto> mainItemDtoList = itemService.getMainItem(4L);

        model.addAttribute("mainItemDtoList", mainItemDtoList);
        return "shop/home";
    }



    /**
     * 상품 검색 화면
     * 홈 헤더 -> 검색
     */
    @GetMapping("/home/search")
    public String homeSearchForm(@PageableDefault(size = 8) Pageable pageable,
                                 @RequestParam String keyword, Model model) {
        Page<MainItemDto> mainItemDtoPage = itemService.getSearchItem(pageable, keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("mainItemDtoPage", mainItemDtoPage);
        return "shop/home_search";
    }

    /**
     * ExceptionController 테스트
     */
    @GetMapping("/testException")
    @ResponseBody
    public void test() {
        throw new EntityExistsException();
    }
}
