package myshop.shop.llm.order.dto;

import lombok.Getter;
import myshop.shop.dto.cancelRequest.SaveCancelRequestDto;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.cancelRequest.CancelRequestStatus;

import java.math.BigDecimal;

/** Redis에 저장되는 취소 신청서 초안. */
@Getter
public class CancelDraftDto {
    private final String id;
    private final Long memberNo;
    private final Long orderItemNo;
    private final int count;
    private final String itemName;
    private final BigDecimal price;
    private final CancelReasonCode reasonCode;
    private final String reasonDetail;

    public CancelDraftDto(String id, Long memberNo, Long orderItemNo, int count, String itemName,
                          BigDecimal price, CancelReasonCode reasonCode, String reasonDetail) {
        this.id = id;
        this.memberNo = memberNo;
        this.orderItemNo = orderItemNo;
        this.count = count;
        this.itemName = itemName;
        this.price = price;
        this.reasonCode = reasonCode;
        this.reasonDetail = reasonDetail;
    }

    /** 확정 시 CancelRequestService.saveCancelRequest에 넘길 DTO. 회원번호는 인증된 값을 쓴다. */
    public SaveCancelRequestDto toDto(Long authenticatedMemberNo, Long orderNo) {
        return new SaveCancelRequestDto(orderNo, orderItemNo, authenticatedMemberNo, count,
                reasonCode, reasonDetail, price, CancelRequestStatus.요청);
    }
}
