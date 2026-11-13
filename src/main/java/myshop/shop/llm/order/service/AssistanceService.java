package myshop.shop.llm.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.entity.orderItem.OrderItem;
import myshop.shop.entity.orderItem.OrderItemStatus;
import myshop.shop.llm.order.dto.CancelDraftDto;
import myshop.shop.llm.order.dto.CancelDraftResultDto;
import myshop.shop.llm.order.dto.ChatResponseDto;
import myshop.shop.repository.orderItem.OrderItemRepository;
import myshop.shop.service.CancelRequestService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistanceService {

    private static final int MAX_MESSAGE_LENGTH = 500;

    private final ChatClient shopAssistant;
    private final DraftService draftService;
    private final CancelRequestService cancelRequestService;
    private final OrderItemRepository orderItemRepository;

    /** 상담 메시지 처리. 회원번호는 인증된 값만 사용한다. */
    public ChatResponseDto chat(Long memberNo, String message) {
        if (message == null || message.isBlank()) {
            return ChatResponseDto.builder()
                    .reply("메시지를 입력해 주세요.")
                    .drafts(List.of())
                    .build();
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH);
        }

        List<CancelDraftResultDto> drafts = new ArrayList<>();
        try {
            String reply = shopAssistant.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "member:" + memberNo))
                    .toolContext(Map.of("memberNo", memberNo, "drafts", drafts))
                    .call()
                    .content();
            return ChatResponseDto.builder()
                    .reply(reply)
                    .drafts(drafts)
                    .build();
        } catch (Exception e) {
            log.error("상담 에이전트 호출 실패, memberNo={}", memberNo, e);
            return ChatResponseDto.builder()
                    .reply("죄송합니다. 지금은 상담을 처리할 수 없습니다. 잠시 후 다시 시도하거나 1:1 문의를 이용해 주세요.")
                    .drafts(List.of())
                    .build();
        }
    }

    /** 사용자가 확인 버튼을 눌렀을 때 취소를 실제로 접수한다. */
    @Transactional
    public String confirm(Long memberNo, String draftId) {
        CancelDraftDto draft = draftService.take(memberNo, draftId).orElse(null);
        if (draft == null) {
            return "취소 신청서가 만료되었거나 이미 처리되었습니다. 다시 요청해 주세요.";
        }

        // 초안 생성 이후 상태가 바뀌었을 수 있으므로 재검증
        OrderItem orderItem = orderItemRepository.findByNoAndMemberNo(draft.getOrderItemNo(), memberNo).orElse(null);
        if (orderItem == null || orderItem.getOrderItemStatus() != OrderItemStatus.상품준비중) {
            return "현재 상태에서는 취소할 수 없습니다. 배송 이후에는 반품을 이용해 주세요.";
        }

        cancelRequestService.saveCancelRequest(draft.toDto(memberNo, orderItem.getOrder().getNo()));
        return "'" + draft.getItemName() + "' 취소 신청이 접수되었습니다. 판매자 승인 후 환불이 진행됩니다.";
    }

    /** 사용자가 취소 초안을 닫았을 때. */
    public void discard(Long memberNo, String draftId) {
        draftService.discard(memberNo, draftId);
    }
}
