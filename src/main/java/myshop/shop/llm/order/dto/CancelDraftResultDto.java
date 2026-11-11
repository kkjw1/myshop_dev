package myshop.shop.llm.order.dto;

import lombok.*;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.cancelRequest.CancelRequestStatus;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDraftResultDto {
    private boolean create;

    private String draftId;
    private Long memberNo;
    private Long orderItemNo;
    private int count;
    private String itemName;
    private CancelReasonCode cancelReasonCode;
    private BigDecimal price;
    private CancelRequestStatus cancelRequestStatus;
    private String reasonDetail;

    private String message;
}
