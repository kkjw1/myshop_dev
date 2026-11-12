package myshop.shop.llm.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatResponseDto {
    private String reply;
    /**
     * 사용자가 확인 버튼으로 접수할 수 있는 취소 초안 목록
     */
    private List<CancelDraftResultDto> drafts;
}
