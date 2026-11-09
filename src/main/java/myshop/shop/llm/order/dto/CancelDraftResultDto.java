package myshop.shop.llm.order.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDraftResultDto {
    boolean create;
    String draftId;
    String itemName;
    Integer cancelCount;
    String message;
}