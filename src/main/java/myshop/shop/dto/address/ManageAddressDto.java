package myshop.shop.dto.address;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.Address;

@Getter @Setter
@ToString(of = {"addressName", "recipientName", "phoneNumber", "postcode", "roadAddress", "detailAddress", "isMain"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManageAddressDto {
    private Long addressNo;
    private String addressName;
    private String recipientName;
    private String phoneNumber;
    private String postcode;
    private String roadAddress;
    private String detailAddress;
    private Boolean isMain;

}
