package myshop.shop.llm.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import myshop.shop.dto.cancelRequest.SaveCancelRequestDto;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.cancelRequest.CancelRequestStatus;

import java.math.BigDecimal;

/** Redis에 저장되는 취소 신청서 초안. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDraftDto {
    private String id;
    private Long memberNo;
    private Long orderItemNo;
    private int count;
    private String itemName;
    private BigDecimal price;
    private CancelReasonCode cancelReasonCode;
    private String reasonDetail;

    /** 확정 시 CancelRequestService.saveCancelRequest에 넘길 DTO. 회원번호는 인증된 값을 쓴다. */
    public SaveCancelRequestDto toDto(Long authenticatedMemberNo, Long orderNo) {
        return new SaveCancelRequestDto(orderNo, orderItemNo, authenticatedMemberNo, count,
                cancelReasonCode, reasonDetail, price, CancelRequestStatus.요청);
    }
}
