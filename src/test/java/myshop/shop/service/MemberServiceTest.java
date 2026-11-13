package myshop.shop.service;

import jakarta.persistence.EntityManager;
import myshop.shop.dto.member.LoginMemberDto;
import myshop.shop.dto.member.SignUpMemberDto;
import myshop.shop.dto.member.UpdateMemberDto;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;
import myshop.shop.repository.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired EntityManager em;

    @Autowired Environment env;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void signUpTest() {
        SignUpMemberDto signUpMemberDto = SignUpMemberDto.builder()
                .id("id")
                .password("password")
                .email("email")
                .name("name")
                .telecom("telecom")
                .gender(Gender.MAN)
                .phoneNumber("phoneNumber")
                .build();
        Member member = memberService.signUp(signUpMemberDto);

        assertThat(member.getId()).isEqualTo("id");
    }

    @Test
    @Commit
    public void loginTest() throws Exception {
        //given
        SignUpMemberDto signUpMemberDto = SignUpMemberDto.builder()
                .id("id")
                .password("password")
                .email("email")
                .name("name")
                .telecom("telecom")
                .gender(Gender.MAN)
                .phoneNumber("phoneNumber")
                .build();
        memberService.signUp(signUpMemberDto);

        //when
        Member login = memberService.login(LoginMemberDto.builder()
                .id("id")
                .password("password")
                .build());

        //then
        assertThat(login.getId()).isEqualTo("id");
    }
    
    @Test
    public void envTest() throws Exception {
        System.out.println(env.getProperty("solapi.api-key"));
        System.out.println(env.getProperty("solapi.api-secret"));
        System.out.println(env.getProperty("solapi.sender"));
    }

    @Test
    public void authTest() throws Exception {
        //given
        String authCode = memberService.smsAuth("01047106305");
        System.out.println("authCode = " + authCode);

    }
    
    @Test
    public void checkIdTest() throws Exception {
        //given
        SignUpMemberDto signUpMemberDto = SignUpMemberDto.builder()
                .id("id")
                .password("password")
                .email("email")
                .name("name")
                .telecom("010-1111-2222")
                .gender(Gender.MAN)
                .phoneNumber("phoneNumber")
                .build();
        memberService.signUp(signUpMemberDto);

        boolean result = memberService.checkId("id");
        boolean result2 = memberService.checkId("test");

        System.out.println("result = " + result);
        System.out.println("result2 = " + result2);
    }
    
    @Test
    public void checkPhoneNumberTest() throws Exception {
        //given
        SignUpMemberDto signUpMemberDto = SignUpMemberDto.builder()
                .id("id")
                .password("password")
                .email("email@test.com")
                .name("name")
                .telecom("SKT")
                .gender(Gender.MAN)
                .phoneNumber("010-1111-2222")
                .build();
        memberService.signUp(signUpMemberDto);

        //when
        boolean result1 = memberService.checkPhoneNumber("010-1111-2222");
        boolean result2 = memberService.checkPhoneNumber("010-1111-2522");

        //then
        System.out.println("result1 = " + result1);
        System.out.println("result2 = " + result2);

    }
    
    @Test
    @Commit
    public void memberModifyTest() throws Exception {
        //given
        SignUpMemberDto signUpMemberDto = SignUpMemberDto.builder()
                .id("id")
                .password("password")
                .email("email")
                .name("name")
                .telecom("telecom")
                .gender(Gender.MAN)
                .phoneNumber("phoneNumber")
                .build();
        memberService.signUp(signUpMemberDto);
        memberService.memberModify(UpdateMemberDto.builder()
                .id("id")
                .name("name")
                .email("email_change")
                .password("password")
                .telecom("telecom_change")
                .phoneNumber("phoneNumber")
                .gender(Gender.MAN)
                .build());
        em.flush();
        em.clear();

        Member member = memberRepository.findById("id").orElse(null);

        assertThat(member.getEmail()).isEqualTo("email_change");
        assertThat(member.getTelecom()).isEqualTo("telecom_change");
        assertThat(member.getName()).isEqualTo("name");
    }
}