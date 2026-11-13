package myshop.shop.dto.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString(of = {"recipientName", "phoneNumber", "postcode", "roadAddress", "detailAddress",
        "deliveryRequest", "addOrderItemDtoList", "totalProductPrice", "deliveryFee", "totalOrderPrice"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddOrderDto {      // 결제할 때, 주문 상세 내역을 나타낼 때 사용 됨
    /**
     * 배송지 내용
     * 1. 수신자 이름
     * 2. 수신자 핸드폰번호
     * 3. 우편번호
     * 4. 배송지 도로명
     * 5. 배송지 상세
     * 6. 배송 요청사항
     */
    private String recipientName;
    private String phoneNumber;
    private String postcode;
    private String roadAddress;
    private String detailAddress;
    private String deliveryRequest;

    /**
     * 주문 상품 내용
     * 1. cart 번호
     * 2. 옵션 번호
     * 3. 상품 번호
     * 4. 구매한 개수
     * 5. 상품별 가격
     * 6. 이미지 url
     * 7. 상품 이름
     * 8. 옵션 이름
     */
    @Builder.Default
    private List<AddOrderItemDto> addOrderItemDtoList = new ArrayList<>();

    /**
     * 금액관련
     * 1. 총 상품금액
     * 2. 배송비
     * 3. 총 결제 금액
     */
    private BigDecimal totalProductPrice;
    private int deliveryFee;
    private BigDecimal totalOrderPrice;

}
