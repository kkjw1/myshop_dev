package myshop.shop.controller.memberWeb;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.address.AddAddressDto;
import myshop.shop.dto.address.ManageAddressDto;
import myshop.shop.dto.address.UpdateAddressDto;
import myshop.shop.dto.member.LoginCheckMemberDto;
import myshop.shop.entity.Address;
import myshop.shop.entity.member.Member;
import myshop.shop.repository.address.AddressRepository;
import myshop.shop.repository.member.MemberRepository;
import myshop.shop.service.AddressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_MEMBER;
import static org.springframework.util.StringUtils.hasText;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;


    /**
     * 베송지 관리 폼
     */
    @GetMapping("/myPage/addressManage")
    public String addressManageForm(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, HttpServletRequest request, Model model) {
/*        if (!new LoginCheckMemberDto().loginCheck(request, model)) {
            return "redirect:/login?redirectURL=" + request.getRequestURI();
        }*/
        List<ManageAddressDto> manageAddressDtoList = addressService.getAddressesByMemberNo(loginCheckMemberDto.getNo());

        if(manageAddressDtoList.isEmpty()) {
            model.addAttribute("manageAddressDtoList", Collections.emptyList());
        } else if (manageAddressDtoList.get(0).getIsMain() != true) {
            model.addAttribute("manageAddressDtoList", manageAddressDtoList);
        } else {
            ManageAddressDto mainAddress = manageAddressDtoList.get(0);
            List<ManageAddressDto> manageAddressDtos = manageAddressDtoList.subList(1, manageAddressDtoList.size());
            model.addAttribute("mainAddress", mainAddress);
            model.addAttribute("manageAddressDtoList", manageAddressDtos);
        }
        return "member/mypage/address_manage";
    }



    /**
     * 배송지 관리 -> 기본배송지로 설정
     */
    @PostMapping("/myPage/addressManage/updateMain")
    public String addressMainUpdate(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
                                    @RequestParam("addressNo") Long addressNo, HttpServletRequest request) {
//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);

        addressService.mainUpdate(addressNo, loginCheckMemberDto.getNo());

        return "redirect:/myPage/addressManage";
    }



    /**
     * 배송지 관리 -> 삭제
     */
    @PostMapping("/myPage/addressManage/delete")
    public String addressDelete(@RequestParam("addressNo") Long addressNo) {
        addressService.deleteAddress(addressNo);

        return "redirect:/myPage/addressManage";
    }



    /**
     * 배송지 관리 -> 수정 폼
     * /myPage/addressManage/update?addressNo=
     */
    @GetMapping("/myPage/addressManage/update")
    public String addressUpdateForm(@RequestParam("addressNo") Long addressNo, Model model, HttpServletRequest request) {

/*        if (!new LoginCheckMemberDto().loginCheck(request, model)) {
            return "redirect:/login?redirectURL=" + request.getRequestURI();
        }*/
        Address address = addressRepository.findByNo(addressNo).orElse(null);
        UpdateAddressDto updateAddressDto = UpdateAddressDto.builder()
            .addressNo(address.getNo())
            .addressName(address.getAddressName())
            .recipientName(address.getRecipientName())
            .phoneNumber(address.getPhoneNumber())
            .postcode(address.getPostcode())
            .roadAddress(address.getRoadAddress())
            .detailAddress(address.getDetailAddress())
            .build();

        model.addAttribute("updateAddressDto", updateAddressDto);
        return "member/mypage/address_manage_update";
    }



    /**
     * 배송지 수정 폼 -> 내정보 가져오기
     */
    @GetMapping("myPage/addressManage/getMyInfo")
    @ResponseBody
    public List<String> getMyInfo(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
                                  HttpServletRequest request) {
//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        Member member = memberRepository.findById(loginCheckMemberDto.getId()).orElse(null);

        List<String> list = new ArrayList<>();
        list.add(member.getName());
        list.add(member.getPhoneNumber());
        return list;
    }



    /**
     * 배송지 수정하기
     */
    @PostMapping("/myPage/addressManage/update")
    public String addressUpdate(@Validated @ModelAttribute("updateAddressDto") UpdateAddressDto updateAddressDto, BindingResult bindingResult,
                                @RequestParam("addressNo") Long addressNo, HttpServletRequest request, Model model) {

        if(bindingResult.hasErrors()) {
            log.info("Address Update Error no={}", addressNo);
//            new LoginCheckMemberDto().loginCheck(request, model);
            return "member/mypage/address_manage_update";
        }

        if (!hasText(updateAddressDto.getAddressName())) {
            updateAddressDto.setAddressName(updateAddressDto.getRoadAddress());
        }
        updateAddressDto.setAddressNo(addressNo);

        addressService.addressModify(updateAddressDto);

        return "redirect:/myPage/addressManage";
    }



    /**
     * 배송지 관리 -> 배송지 추가 폼
     */
    @GetMapping("/myPage/addressManage/add")
    public String addressAddForm(HttpServletRequest request, Model model) {

/*        if (!new LoginCheckMemberDto().loginCheck(request, model)) {
            return "redirect:/login?redirectURL=" + request.getRequestURI();
        }*/

        model.addAttribute("addAddressDto", new AddAddressDto());
        return "member/mypage/address_manage_add";
    }



    /**
     * 배송지 추가 폼 -> 저장하기
     */
    @PostMapping("/myPage/addressManage/add")
    public String addressAdd(@Validated @ModelAttribute("addAddressDto") AddAddressDto addAddressDto, BindingResult bindingResult,
                             @AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
                             HttpServletRequest request, Model model) {

        if(bindingResult.hasErrors()) {
//            new LoginCheckMemberDto().loginCheck(request, model);
            return "member/mypage/address_manage_add";
        }

        if (!hasText(addAddressDto.getAddressName())) {
            addAddressDto.setAddressName(addAddressDto.getRoadAddress());
        }

//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        addressService.saveAddress(loginCheckMemberDto.getNo(), addAddressDto);

        return "redirect:/myPage/addressManage";
    }


    /**
     * 주문/결제 폼 -> 배송지 추가 -> 저장 및 선택
     */
    @PostMapping("/myPage/order/addressAdd")
    @ResponseBody
    public boolean orderAddressAdd(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
                                   @RequestBody AddAddressDto addAddressDto, HttpServletRequest request) {
        if (!hasText(addAddressDto.getAddressName())) {
            addAddressDto.setAddressName(addAddressDto.getRoadAddress());
        }

//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        try {
            addressService.saveAddress(loginCheckMemberDto.getNo(), addAddressDto);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

}


