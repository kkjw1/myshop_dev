package myshop.shop.llm.order.dto;

import lombok.Data;

@Data
public class CancelDraftResultDto {
    boolean create;
    String draftId;
    String itemName;
    Integer refundAmount;
    String message;

    public CancelDraftResultDto(String draftId, String itemName, int price) {
        this.create = true;
        this.draftId = draftId;
        this.itemName = itemName;
        this.refundAmount = price;
        this.message = "취소 신청서 초안을 만들었습니다. 사용자가 확인 버튼을 눌러야 접수됩니다.";
    }

    public CancelDraftResultDto(String reason) {
        this.create = false;
        this.message = reason;
    }
}