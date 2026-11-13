package myshop.shop.controller.memberWeb;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.member.*;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;
import myshop.shop.repository.member.MemberRepository;
import myshop.shop.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static myshop.shop.controller.memberWeb.MemberController.SessionConst.LOGIN_MEMBER;
import static myshop.shop.service.JwtService.*;
import static org.springframework.util.StringUtils.hasText;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final RedisService redisService;
    private final MailService mailService;
    private final JwtService jwtService;


    /**
     * 로그인 폼
     */
    @GetMapping("/login")
    public String loginForm(Model model, @RequestParam(required = false) String redirectURL) {
        model.addAttribute("loginMemberDto", new LoginMemberDto());
        model.addAttribute("redirectURL", redirectURL);
        return "member/login";
    }



    /**
     * 회원가입 완료 후 로그인 폼
     * 아이디 찾기 완료 후 로그인 폼
     */
    @GetMapping("login/{loginId}")
    public String loginForm2(@PathVariable("loginId") String id, Model model) {
        model.addAttribute("loginMemberDto", LoginMemberDto.builder()
                .id(id)
                .build());
        return "member/login";
    }



    /**
     * 로그인
     */
    @PostMapping("/login")
    public String login(@Validated @ModelAttribute("loginMemberDto") LoginMemberDto loginMemberDto, BindingResult bindingResult,
                        HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String redirectURL) {
        log.info("loginMemberDto={}", loginMemberDto);

        if (bindingResult.hasErrors()) {
            log.info("login fail");
            return "member/login";
        }

        Member login = memberService.login(loginMemberDto);
        if (login == null) {
            bindingResult.reject("loginFail","로그인 실패 메시지");
            return "member/login";
        }


        String accessToken = jwtService.createAccessToken(login);

        long refreshDate = loginMemberDto.getLoginCheck() == true
                ? Duration.ofMillis(checkRefreshTokenValidity).toMillis()
                : Duration.ofMillis(unCheckRefreshTokenValidity).toMillis();

        String refreshToken = jwtService.createRefreshToken(login, refreshDate);
        redisService.tokenSave(login.getId(), refreshToken, refreshDate);


        ResponseCookie.ResponseCookieBuilder refreshCookieBuilder = ResponseCookie
                .from("refreshToken", refreshToken)     // 쿠키 이름, value
                .httpOnly(true)                               // js 접근 차단, http요청만
                .secure(false)                                // HTTPS
                .path("/")                                    // url 경로 범위
                .sameSite("Strict");                          // 외부사이트 요청에 쿠키 사용X <-> "Lax"
        if (loginMemberDto.getLoginCheck()) {
            refreshCookieBuilder.maxAge(Duration.ofMillis(refreshDate));
        }
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieBuilder.build().toString());

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(accessCookieValidity))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());


        LoginCheckMemberDto loginCheckMemberDto = LoginCheckMemberDto.builder()
                .no(login.getNo())
                .id(login.getId())
                .name(login.getName())
                .build();
        // Spring Security의 Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginCheckMemberDto, null, List.of(new SimpleGrantedAuthority("MEMBER_" + loginCheckMemberDto.getId()))
        );
        // SecurityContext에 인증 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);


        String target = (redirectURL != null && !redirectURL.isBlank()) ? redirectURL : "/";
        log.info("redirectURL={} target={}", redirectURL, target);
        return "redirect:" + target;
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public String logout(@RequestParam("memberId") String memberId, HttpServletResponse response) {
        redisService.tokenDelete(memberId);

        // 클라이언트 쿠키 삭제
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 즉시 만료
                .sameSite("Strict")
                .build();

        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        return "redirect:/";
    }



    /**
     * 회원가입 폼
     */
    @GetMapping("/signUp")
    public String signUpForm(Model model) {
        model.addAttribute("signUpMemberDto", new SignUpMemberDto());
        return "member/signUp";
    }



    /**
     * 회원가입 -> 아이디 중복 체크(onblur)
     */
    @GetMapping("/checkId")
    @ResponseBody
    public boolean checkDuplicateId(@RequestParam("id") String id) {
        return memberService.checkId(id);
    }



    /**
     * 회원가입
     */
    @PostMapping("/signUp")
    public String signUp(@Validated @ModelAttribute("signUpMemberDto") SignUpMemberDto signUpMemberDto, BindingResult bindingResult,
                         RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult);
            return "member/signUp";
        }

        String data = redisService.getData("confirmedPhone:" + signUpMemberDto.getPhoneNumber());
        log.info("data={}", data);
        if (data == null) {
            log.info("핸드폰이 인증되지 않았습니다.");
            bindingResult.reject("phoneAuthFail", "핸드폰 인증 실패 메시지");
            return "member/signUp";
        }

        Member signed = memberService.signUp(signUpMemberDto);
        log.info("signUp complete, {}", signed);

        redirectAttributes.addAttribute("loginId", signUpMemberDto.getId());
        return "redirect:/login/{loginId}";
    }

    public static class SessionConst {
        public static final String LOGIN_MEMBER="loginMember";
        public static final String LOGIN_SELLER="loginSeller";
    }



    /**
     * 회원가입 -> 핸드폰번호 중복 체크
     * 아이디,비밀번호 찾기 -> 핸드폰번호 체크
     */
    @GetMapping("/checkPhoneNumber")
    @ResponseBody
    public Boolean checkDuplicatePhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
        boolean result = memberService.checkPhoneNumber(phoneNumber);
        log.info("checkDuplicatePhoneNumber, phoneNumber={} result={}", phoneNumber, result);
        return result;
    }



    /**
     * 회원가입 -> 이메일 중복 확인(onblur)
     * 아이디,비밀번호 찾기 -> 이메일 중복 체크
     */
    @GetMapping("/checkEmail")
    @ResponseBody
    public Boolean checkDuplicateEmail(@RequestParam("email") String email) {
        boolean result = memberService.checkEmail(email);
        log.info("checkDuplicateEmail, email={} result={}", email, result);
        return result;
    }



    /**
     * 회원가입 -> 핸드폰 인증
     * 아이디,비밀번호 찾기 -> 핸드폰 인증
     */
    @PostMapping("/sendSmsAuth")
    @ResponseBody
    public String sendSmsAuth(@RequestBody SendSmsAuthDto sendSmsAuthDto) {
        log.info("phoneNumber={}", sendSmsAuthDto.getPhoneNumber());
        String authCode = memberService.smsAuth(sendSmsAuthDto.getPhoneNumber());
//        String authCode = "123456";
        redisService.saveData("sendSmsAuth:" + sendSmsAuthDto.getPhoneNumber(), authCode, 3L);
        return "ok";
    }

    @Getter @Setter
    public static class SendSmsAuthDto {
        private String phoneNumber;
    }



    /**
     * 회원가입 -> 핸드폰 인증번호 확인
     */
    @PostMapping("/checkAuthCode")
    @ResponseBody
    public String checkAuthCode(@RequestBody CheckSmsAuthCodeDto checkSmsAuthCodeDto,
                                HttpServletRequest request) {
        String authCode = checkSmsAuthCodeDto.getAuthCode();
        String phoneNumber = checkSmsAuthCodeDto.getPhoneNumber();
        log.info("authCode:{}, phoneNumber:{}", authCode, phoneNumber);

        String data = redisService.getData("sendSmsAuth:"+phoneNumber);
        boolean result = authCode.equals(data);
        log.info("authCode.equals:{}", result);
        if (result) {
            redisService.deleteData("sendSmsAuth:"+phoneNumber);
            redisService.saveData("confirmedPhone:"+phoneNumber, "SUCCESS", 10L);
            return "ok";
        }
        return "no";
    }

    @Getter @Setter
    public static class CheckSmsAuthCodeDto {
        private String authCode;
        private String phoneNumber;
    }



    /**
     * 아이디, 비밀번호 찾기 폼
     */
    @GetMapping("/findMember")
    public String findMemberForm() {
        return "member/find_member";
    }



    /**
     * 아이디,비밀번호 찾기 -> 핸드폰 인증번호 확인
     */
    @PostMapping("/findMember/checkSmsAuthCode")
    public String checkSmsAuthCode(@RequestBody CheckSmsAuthCodeDto checkSmsAuthCodeDto, RedirectAttributes redirectAttributes) {

        String authCode = checkSmsAuthCodeDto.getAuthCode();
        String phoneNumber = checkSmsAuthCodeDto.getPhoneNumber();
        log.info("checkSmsAuthCode authCode:{}, phoneNumber:{}", authCode, phoneNumber);

        String data = redisService.getData("sendSmsAuth:"+phoneNumber);
        boolean result = authCode.equals(data);
        log.info("result:{}", result);
        if (result) {
            redisService.deleteData("sendSmsAuth:"+phoneNumber);
            Member member = memberRepository.findByPhoneNumber(phoneNumber).orElse(null);
            String memberId = member.getId();
            redisService.saveData("findMemberId:"+ memberId, "SUCCESS", 10L);
            redirectAttributes.addAttribute("memberId", memberId);
            return "redirect:/resetPassword";
        }
        return "member/find_member";
    }



    /**
     * 아이디 비밀번호 찾기 -> 이메일 인증
     */
    @PostMapping("/sendEmailAuth")
    @ResponseBody
    public String sendEmailAuth(@RequestBody SendEmailAuthDto sendEmailAuthDto) {
        log.info("toAddress={}", sendEmailAuthDto.getToAddress());
        String authCode = mailService.authCodeCreate();
        mailService.sendMail(sendEmailAuthDto.getToAddress(), authCode);
        redisService.saveData("sendEmailAuth:" + sendEmailAuthDto.getToAddress(), authCode, 3L);
        return "ok";
    }

    @Getter @Setter
    public static class SendEmailAuthDto {
        private String toAddress;
    }



    /**
     * 아이디,비밀번호 찾기 -> 이메일 인증번호 확인
     */
    @PostMapping("/findMember/checkEmailAuthCode")
    public String checkEmailAuthCode(@RequestBody CheckEmailAuthCode checkEmailAuthCode, RedirectAttributes redirectAttributes) {

        String authCode = checkEmailAuthCode.getAuthCode();
        String toAddress = checkEmailAuthCode.getToAddress();
        log.info("checkEmailAuthCode authCode:{}, toAddress:{}", authCode, toAddress);

        String data = redisService.getData("sendEmailAuth:" + toAddress);
        boolean result = authCode.equals(data);
        log.info("authCode.equals:{}", result);

        if (result) {
            redisService.deleteData("sendEmailAuth:" + toAddress);
            Member member = memberRepository.findByEmail(toAddress).orElse(null);
            String memberId = member.getId();
            redisService.saveData("findMemberId:"+ memberId, "SUCCESS", 10L);
            redirectAttributes.addAttribute("memberId", memberId);
            return "redirect:/resetPassword";
        }
        return "member/find_member";
    }

    @Getter @Setter
    public static class CheckEmailAuthCode {

        private String authCode;
        private String toAddress;
    }



    /**
     * 비밀번호 재설정 폼
     */
    @GetMapping("/resetPassword")
    public String resetPasswordForm(@RequestParam("memberId") String memberId, Model model) {
        log.info("findMember:{}", memberId);

        String data = redisService.getData("findMemberId:" + memberId);

        if (!data.equals("SUCCESS")) {
            log.error("잘못된 접근입니다.");
            return "redirect:/login";
        }

        model.addAttribute("resetPasswordMemberDto", ResetPasswordMemberDto.builder()
                .id(memberId)
                .build());
        return "member/reset_password";
    }


    /**
     * 비밀번호 재설정
     */
    @PostMapping("/resetPassword")
    public String resetPassword(@Validated @ModelAttribute("resetPasswordMemberDto") ResetPasswordMemberDto resetPasswordMemberDto, BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/reset_password";
        }

        int result = memberService.resetPassword(resetPasswordMemberDto);
        log.info("result:{}",result);

        redirectAttributes.addAttribute("loginId", resetPasswordMemberDto.getId());
        return "redirect:/login/{loginId}";
    }



    /**
     * 개인정보 확인/수정 폼
     */
    @GetMapping("/myPage/memberModify")
    public String memberModifyForm(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, Model model, HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        String memberId = loginCheckMemberDto.getId();

        String access = redisService.getData("ModifyCheckPW:" + memberId);
        if (access == null) {
            redirectAttributes.addAttribute("memberId", memberId);
            return "redirect:/memberModifyCheckPW";
        }

/*        if (!new LoginCheckMemberDto().loginCheck(request, model)) {
            return "redirect:/login?redirectURL=" + request.getRequestURI();
        }*/

        Member member = memberRepository.findById(memberId).orElse(null);
        UpdateMemberDto updateMember = UpdateMemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .telecom(member.getTelecom())
                .phoneNumber(member.getPhoneNumber())
                .gender(member.getGender())
                .build();
        model.addAttribute("updateMember", updateMember);

        return "member/mypage/member_modify";
    }



    /**
     * 개인정보 확인/수정 -> 비밀번호 재확인 폼
     */
    @GetMapping("/memberModifyCheckPW")
    public String memberModifyCheckPWForm(@RequestParam("memberId") String memberId, Model model) {
        LoginMemberDto loginMemberDto = new LoginMemberDto();
        loginMemberDto.setId(memberId);
        model.addAttribute("loginMemberDto", loginMemberDto);
        return "member/mypage/member_modify_checkPW";
    }



    /**
     * 비밀번호 재확인 폼 -> 비밀번호 확인
     */
    @PostMapping("/memberModifyCheckPW")
    public String memberModifyCheckPW(@Validated @ModelAttribute LoginMemberDto loginMemberDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/mypage/member_modify_checkPW";
        }

        Member member = memberService.login(loginMemberDto);
        if (member == null) {
            log.info("memberModifyCheckPW Error");
            bindingResult.reject("CheckPWError", "비밀번호 체크 에러");
            return "member/mypage/member_modify_checkPW";
        }

        redisService.saveData("ModifyCheckPW:"+member.getId(), "SUCCESS", 10L);

        return "redirect:/myPage/memberModify";
    }



    /**
     * 개인정보 확인/수정 -> 수정하기
     */
    @PostMapping("/myPage/memberModify")
    public String memberModify(@Validated @ModelAttribute("updateMember") UpdateMemberDto updateMemberDto, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, HttpServletRequest request, Model model) {

        if (bindingResult.hasErrors()) {
            log.info("myPageUpdate Fail={}", bindingResult);
//            new LoginCheckMemberDto().loginCheck(request, model);
            return "member/mypage/member_modify";
        }

        Member beforeMember = memberRepository.findById(updateMemberDto.getId()).orElse(null);
        Member updateMember = memberService.memberModify(updateMemberDto);

        ChangedDataDto changedDataDto = ChangedDataDto.builder()
                .name(Objects.equals(beforeMember.getName(), updateMember.getName()) ? null : updateMember.getName())
                .email(Objects.equals(beforeMember.getEmail(), updateMember.getEmail()) ? null : updateMember.getEmail())
                .telecom(Objects.equals(beforeMember.getTelecom(), updateMember.getTelecom()) ? null : updateMember.getTelecom())
                .phoneNumber(Objects.equals(beforeMember.getPhoneNumber(), updateMember.getPhoneNumber()) ? null : updateMember.getPhoneNumber())
                .gender(Objects.equals(beforeMember.getGender(), updateMember.getGender()) ? null : updateMember.getGender())
                .build();
        if (StringUtils.hasText(updateMemberDto.getPassword())) {
            changedDataDto.setPassword("비밀번호가 변경되었습니다.");
        }
        log.info("changedDataDto:{}", changedDataDto);

        redirectAttributes.addFlashAttribute("changedDataDto", changedDataDto); //model에 바로 보냄
        redirectAttributes.addAttribute("memberId", updateMemberDto.getId());
        return "redirect:/myPage/memberModify/complete";
    }

    @Getter @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(of = {"name", "email", "password", "telecom", "phoneNumber", "gender"})
    public static class ChangedDataDto {
        private String name;
        private String email;
        private String password;
        private String telecom;
        private String phoneNumber;
        private Gender gender;

    }



    /**
     * 개인정보 수정 후, 확인 폼
     */
    @GetMapping("/myPage/memberModify/complete")
    public String memberModifyCompleteForm(@RequestParam("memberId") String memberId, Model model) {
        model.addAttribute("memberId", memberId);
        return "member/mypage/member_modify_complete";
    }



    /**
     * 개인정보 확인/수정 -> 회원 탈퇴하기
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("memberId") String memberId, HttpServletRequest request) {
        try {
            if (memberService.withdraw(memberId)) {
                log.info("delete member={}", memberId);
                HttpSession session = request.getSession(false);
                session.invalidate();
                return "redirect:/";
            }
        } catch (EntityNotFoundException e) {
            log.info("삭제 실패, 회원을 찾을 수 없습니다. Id={}", memberId);
        }
        return "redirect:/";
    }
}
