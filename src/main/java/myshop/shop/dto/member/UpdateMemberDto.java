package myshop.shop.dto.member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateMemberDto {
    public String id;

    @NotBlank(message = "이름이 공백입니다.")
    private String name;

    @NotBlank
    @Email(message = "이메일 주소가 정확한지 확인해 주세요.")
    private String email;

    private String password;
    private String checkPassword;

    @NotBlank(message = "통신사가 공백입니다.")
    private String telecom;
    @NotBlank(message = "휴대전화번호가 공백입니다.")
    private String phoneNumber;

    private Gender gender;

}
