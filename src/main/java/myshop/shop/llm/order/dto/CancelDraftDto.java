package myshop.shop.llm.order.dto;

import lombok.Data;
import myshop.shop.dto.cancelRequest.SaveCancelRequestDto;
import myshop.shop.entity.cancelRequest.CancelReasonCode;

import java.math.BigDecimal;

@Data
public class CancelDraftDto {
    String id;
    Long memberNo;
    Long orderItemNo;
    String itemName;
    BigDecimal refundAmount;
    CancelReasonCode reasonCode;
    String reasonDetail;


    public CancelDraftDto(String id, Long memberNo, Long orderItemNo, String itemName, BigDecimal refundAmount, CancelReasonCode reasonCode, String reasonDetail) {
        this.id = id;
        this.memberNo = memberNo;
        this.orderItemNo = orderItemNo;
        this.itemName = itemName;
        this.refundAmount = refundAmount;
        this.reasonCode = reasonCode;
        this.reasonDetail = reasonDetail;
    }

    /** 확정 시 기존 CancelRequestService.saveCancelRequest에 넘길 DTO로 변환. memberNo는 인증된 회원 값을 사용한다. */
    public SaveCancelRequestDto toDto(Long authenticatedMemberNo) {
        // TODO: 실제 SaveCancelRequestDto의 필드/생성 방식(builder, setter 등)에 맞게 수정
        return new SaveCancelRequestDto();
    }
}
