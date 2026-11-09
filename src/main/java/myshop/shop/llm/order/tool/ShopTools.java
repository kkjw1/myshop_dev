package myshop.shop.llm.order.tool;

import lombok.RequiredArgsConstructor;
import myshop.shop.dto.order.ManageOrderDto;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.orderItem.OrderItem;
import myshop.shop.entity.orderItem.OrderItemStatus;
import myshop.shop.llm.order.dto.CancelDraftDto;
import myshop.shop.llm.order.dto.CancelDraftResultDto;
import myshop.shop.repository.orderItem.OrderItemRepository;
import myshop.shop.service.OrderService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class ShopTools {

    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;

    @Tool(description = "로그인한 회원의 주문 목록을 조회한다. 각 상품의 주문상품번호, 상품명, 옵션, 주문상태가 포함된다.")
    public List<ManageOrderDto> getMyOrders(ToolContext toolContext) {
        return orderService.getManageOrder(getMemberNo(toolContext));
    }

    @Tool(description = """
            주문 취소 신청서 '초안'을 만든다. 실제로 취소를 접수하지 않는다.
            사용자가 화면의 확인 버튼을 눌러야 접속된다. 사용자가 취소 의사를 분명히 밝혔을 때만 호출한다.""")
    public CancelDraftResultDto draftCancelRequest(
            @ToolParam(description = "주문상품번호") Long orderItemNo,
            @ToolParam(description = "취소 사유 코드") CancelReasonCode cancelReasonCode,
            @ToolParam(description = "사용자가 말한 사유 요약", required = false) String reasonDetail,
            ToolContext toolContext) {

        Long memberNo = getMemberNo(toolContext);
        OrderItem orderItem = orderItemRepository.findByNoAndMemberNo(orderItemNo, memberNo).orElse(null);

        if (orderItem == null) {
            return CancelDraftResultDto.builder()
                    .create(false)
                    .draftId(null)
                    .itemName(null)
                    .cancelCount(null)
                    .message("해당 주문을 찾을 수 없습니다.")
                    .build();
        }
        if (orderItem.getOrderItemStatus() != OrderItemStatus.상품준비중) {
            return CancelDraftResultDto.builder()
                    .create(false)
                    .draftId(null)
                    .itemName(null)
                    .cancelCount(null)
                    .message("현재 상태(" + orderItem.getOrderItemStatus() + ")에서는 취소할 수 없습니다. 배송 이후에는 반품을 이용해야 합니다.")
                    .build();
        }

        // 상품 취소신청서 초안 제작




        return new CancelDraftResultDto();
    }



    private Long getMemberNo(ToolContext toolContext) {
        return (Long) toolContext.getContext().get("memberNo");
    }

    @SuppressWarnings("unchecked")
    private List<CancelDraftDto> collector(ToolContext toolContext) {
        return (List<CancelDraftDto>) toolContext.getContext().get("drafts");
    }

}
