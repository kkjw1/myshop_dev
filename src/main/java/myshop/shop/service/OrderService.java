package myshop.shop.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import myshop.shop.controller.memberWeb.OrderController;
import myshop.shop.dto.order.*;
import myshop.shop.entity.orderItem.OrderItem;
import myshop.shop.entity.delivery.Delivery;
import myshop.shop.entity.delivery.DeliveryStatus;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.member.Member;
import myshop.shop.entity.order.Order;
import myshop.shop.entity.order.OrderStatus;
import myshop.shop.entity.orderItem.OrderItemStatus;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.repository.delivery.DeliveryRepository;
import myshop.shop.repository.member.MemberRepository;
import myshop.shop.repository.order.OrderRepository;
import myshop.shop.repository.orderItem.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static myshop.shop.service.ItemService.getDiscountedPrice;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final EntityManager em;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * 주문 저장
     */
    public Order saveOrder(Long memberNo, AddOrderDto addOrderDto) {
        Member memberProxy = memberRepository.getReferenceById(memberNo);

        // Order 저장
        Order savedOrder = orderRepository.save(new Order(memberProxy,
                OrderStatus.결제완료,
                addOrderDto.getTotalOrderPrice(),
                addOrderDto.getRecipientName(),
                addOrderDto.getPhoneNumber(),
                addOrderDto.getPostcode(),
                addOrderDto.getRoadAddress(),
                addOrderDto.getDetailAddress(),
                addOrderDto.getDeliveryRequest(),
                addOrderDto.getDeliveryFee()));


        // Delivery, OrderItem 저장
        List<AddOrderItemDto> addOrderItemDtoList = addOrderDto.getAddOrderItemDtoList();

        for (AddOrderItemDto addOrderItemDto : addOrderItemDtoList) {
            Delivery savedDelivery = deliveryRepository.save(new Delivery(null,
                    null,
                    DeliveryStatus.상품준비중));
            Item itemProxy = itemRepository.getReferenceById(addOrderItemDto.getItemNo());
            OrderItem orderItem = new OrderItem(savedOrder,
                    itemProxy,
                    savedDelivery,
                    addOrderItemDto.getCount(),
                    addOrderItemDto.getTotalPrice(),
                    addOrderItemDto.getImageUrl(),
                    addOrderItemDto.getItemName(),
                    addOrderItemDto.getOptionName(),
                    OrderItemStatus.상품준비중);
            orderItem.updateOrder(savedOrder);
            orderItemRepository.save(orderItem);
        }

        int size = addOrderItemDtoList.size();
        if (size > 1) {
            savedOrder.setOrderInfo(addOrderItemDtoList.get(0).getItemName() + " 외 " + String.valueOf(size - 1) + "건");
        } else {
            savedOrder.setOrderInfo(addOrderItemDtoList.get(0).getItemName());
        }

        return savedOrder;
    }


    /**
     * 주문 상세 내역 불러오기
     */
    public DetailOrderDto getDetailOrder(Long orderNo) {
        DetailOrderDto detailOrderDto = orderItemRepository.getDetailOrder(orderNo);

        List<DetailOrderItemDto> detailOrderItemDtoList = detailOrderDto.getDetailOrderItemDtoList();

        BigDecimal totalItemPrice = BigDecimal.valueOf(0);
        for (DetailOrderItemDto detailOrderItemDto : detailOrderItemDtoList) {
            totalItemPrice = totalItemPrice.add(detailOrderItemDto.getTotalPrice());
        }
        System.out.println("totalItemPrice=" + totalItemPrice);

        detailOrderDto.setTotalItemPrice(totalItemPrice);
        detailOrderDto.setTotalOrderPrice(detailOrderDto.getTotalItemPrice().add(BigDecimal.valueOf(detailOrderDto.getDeliveryFee())));

        return detailOrderDto;
    }


    /**
     * 주문/결제 폼
     * 상품 상세 폼 -> 바로 구매
     */
    public DirectOrderDto getDirectOrder(OrderController.CheckDirectOrderDto checkDirectOrderDto) {
        DirectOrderDto directOrderDto = orderRepository.getDirectOrder(checkDirectOrderDto);

        directOrderDto.setCount(checkDirectOrderDto.getCount());

        BigDecimal originalPrice = directOrderDto.getOriginalPrice();
        BigDecimal discountPer = directOrderDto.getDiscountPer();
        BigDecimal optionPrice = directOrderDto.getOptionPrice() == null ? BigDecimal.valueOf(0) : directOrderDto.getOptionPrice();
        BigDecimal discountedPrice = getDiscountedPrice(originalPrice, discountPer);
        directOrderDto.setPrice(discountedPrice.add(optionPrice));

        return directOrderDto;
    }

    /**
     * 주문 목록/배송 조회 폼
     * 주문 상세 내역 -> 주문 내역 보기
     * 마이페이지 -> 주문 목록/배송 조회
     */
    public List<ManageOrderDto> getManageOrder(Long memberNo) {
        return orderItemRepository.getManageOrder(memberNo);
    }

}
