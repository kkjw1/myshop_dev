package myshop.shop.repository.member;

import jakarta.persistence.EntityManager;
import myshop.shop.dto.member.SignUpMemberDto;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;
import myshop.shop.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired MemberService memberService;
    @Autowired EntityManager em;


    @Test
    public void deleteByIdTest() throws Exception {
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
        em.flush();
        em.clear();

        //when
        Member mem = memberRepository.findById("id").orElse(null);
        memberRepository.delete(mem);
        Member member = memberRepository.findById("id").orElse(null);

        //then
        assertThat(member).isNull();
    }


}