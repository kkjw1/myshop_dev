package myshop.shop.dto.seller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(of = {"id", "password"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginSellerDto {
    @NotBlank(message = "아이디가 공백입니다.")
    String id;
    @NotBlank(message = "비밀번호가 공백입니다.")
    String password;

}
