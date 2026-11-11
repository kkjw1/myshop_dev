package myshop.shop.llm.order.tool;

import lombok.RequiredArgsConstructor;
import myshop.shop.dto.order.ManageOrderDto;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.cancelRequest.CancelRequestStatus;
import myshop.shop.entity.delivery.Delivery;
import myshop.shop.entity.delivery.DeliveryStatus;
import myshop.shop.entity.orderItem.OrderItem;
import myshop.shop.entity.orderItem.OrderItemStatus;
import myshop.shop.llm.order.dto.CancelDraftDto;
import myshop.shop.llm.order.dto.CancelDraftResultDto;
import myshop.shop.llm.order.dto.OrderItemStatusView;
import myshop.shop.llm.order.service.DraftService;
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
    private final DraftService draftService;

    @Tool(description = "로그인한 회원의 주문 목록을 조회한다. 각 상품의 주문상품번호, 상품명, 옵션, 주문상태가 포함된다.")
    public List<ManageOrderDto> getMyOrders(ToolContext toolContext) {
        return orderService.getManageOrder(getMemberNo(toolContext));
    }

    @Tool(description = "주문상품번호로 배송 상태, 택배사, 운송장번호, 취소 가능 여부를 조회한다.")
    public OrderItemStatusView getOrderItemStatusView(
            @ToolParam(description = "주문상품번호(orderItemNo)") Long orderItemNo,
            ToolContext toolContext) {
        // 소유권 검증: 본인 주문만 조회
        OrderItem orderItem = orderItemRepository.findByNoAndMemberNo(orderItemNo, getMemberNo(toolContext)).orElse(null);
        if (orderItem == null) {
            return OrderItemStatusView.builder()
                    .errorMessage("해당 주문상품을 찾을 수 없습니다.")
                    .build();
        }

        Delivery delivery = orderItem.getDelivery();
        if (delivery == null) {
            return OrderItemStatusView.builder()
                    .orderItemNo(orderItemNo)
                    .errorMessage("해당 상품의 배송정보를 찾을 수 없습니다.")
                    .build();
        }

        boolean cancellable = delivery.getDeliveryStatus() == DeliveryStatus.상품준비중
                && orderItem.getOrderItemStatus() == OrderItemStatus.상품준비중;

        return OrderItemStatusView.builder()
                .orderItemNo(orderItemNo)
                .deliveryStatus(delivery.getDeliveryStatus())
                .courier(delivery.getCourier())
                .trackingNumber(delivery.getTrackingNumber())
                .cancellable(cancellable)
                .build();
    }

    @Tool(description = """
            주문 취소 신청서 '초안'을 만든다. 실제로 취소를 접수하지 않는다.
            사용자가 화면의 확인 버튼을 눌러야 접수된다. 사용자가 취소 의사를 분명히 밝혔을 때만 호출한다.""")
    public CancelDraftResultDto draftCancelRequest(
            @ToolParam(description = "주문상품번호") Long orderItemNo,
            @ToolParam(description = "취소 사유 코드: CHANGED_MIND(단순변심), WRONG_ORDER(주문실수), DELIVERY_DELAY(배송지연 우려), PRICE_FALL(더 저렴한 상품 발견), OTHER(기타)") CancelReasonCode cancelReasonCode,
            @ToolParam(description = "사용자가 말한 사유 요약", required = false) String reasonDetail,
            ToolContext toolContext) {

        Long memberNo = getMemberNo(toolContext);
        OrderItem orderItem = orderItemRepository.findByNoAndMemberNo(orderItemNo, memberNo).orElse(null);

        if (orderItem == null) {
            return CancelDraftResultDto.builder()
                    .create(false)
                    .message("해당 주문을 찾을 수 없습니다.")
                    .build();
        }
        if (orderItem.getOrderItemStatus() != OrderItemStatus.상품준비중) {
            return CancelDraftResultDto.builder()
                    .create(false)
                    .message("현재 상태(" + orderItem.getOrderItemStatus() + ")에서는 취소할 수 없습니다. 배송 이후에는 반품을 이용해야 합니다.")
                    .build();
        }

        // 초안을 Redis에 저장 (금액·수량은 서버가 OrderItem에서 채움)
        CancelDraftDto draft = draftService.save(memberNo, orderItem, cancelReasonCode, reasonDetail);

        CancelDraftResultDto result = CancelDraftResultDto.builder()
                .create(true)
                .draftId(draft.getId())
                .memberNo(memberNo)
                .orderItemNo(orderItemNo)
                .count(draft.getCount())
                .itemName(draft.getItemName())
                .cancelReasonCode(cancelReasonCode)
                .price(draft.getPrice())
                .cancelRequestStatus(CancelRequestStatus.요청)
                .reasonDetail(reasonDetail)
                .message("취소 신청서 초안을 만들었습니다. 사용자가 확인 버튼을 눌러야 접수됩니다.")
                .build();
        collector(toolContext).add(result);

        return result;
    }

    private Long getMemberNo(ToolContext toolContext) {
        return (Long) toolContext.getContext().get("memberNo");
    }

    @SuppressWarnings("unchecked")
    private List<CancelDraftResultDto> collector(ToolContext toolContext) {
        return (List<CancelDraftResultDto>) toolContext.getContext().get("drafts");
    }
}
