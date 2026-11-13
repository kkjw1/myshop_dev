package myshop.shop.dto.cancelRequest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.cancelRequest.CancelRequestStatus;

import java.math.BigDecimal;

@Getter @Setter
@ToString(of = {"orderNo", "orderItemNo", "memberNo", "count", "cancelReasonCode", "reasonDetail", "price", "cancelRequestStatus"})
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SaveCancelRequestDto {
    private Long orderNo;
    private Long orderItemNo;
    private Long memberNo;
    private int count;
    private CancelReasonCode cancelReasonCode;
    private String reasonDetail;
    private BigDecimal price;
    private CancelRequestStatus cancelRequestStatus;

}
