package myshop.shop.repository.delivery;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import myshop.shop.dto.delivery.OrderDeliveryDto;
import myshop.shop.dto.delivery.OrderItemDeliveryDto;
import myshop.shop.entity.delivery.Delivery;
import myshop.shop.entity.delivery.QDelivery;


import java.util.*;

import static myshop.shop.entity.orderItem.QOrderItem.orderItem;
import static myshop.shop.entity.delivery.QDelivery.delivery;
import static myshop.shop.entity.item.QItem.item;
import static myshop.shop.entity.order.QOrder.order;


@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> getOrderNoSet(Long sellerNo) {
        // seller -> item -> item의 모든 것 가져오기
        Set<Long> orderSet = new HashSet<>();

        List<Long> itemNoList = queryFactory
                .select(item.no)
                .from(item)
                .where(item.seller.no.eq(sellerNo))
                .fetch();

        for (Long itemNo : itemNoList) {
            orderSet.add(itemNo);
        }

        return orderSet;
    }

    @Override
    public List<OrderDeliveryDto> getOrderDeliveryList(Set<Long> orderNoList) {

        List<OrderDeliveryDto> orderDeliveryDtoList = new ArrayList<>();

        for (Long orderNo : orderNoList) {
            OrderDeliveryDto orderDeliveryDto = queryFactory
                    .select(Projections.fields(OrderDeliveryDto.class,
                            order.orderStatus,
                            order.no.as("orderNo"),
                            order.orderInfo,
                            order.totalPrice,
                            order.createdDate.as("orderTime"),
                            order.recipientName,
                            order.recipientPhone,
                            order.postcode,
                            order.roadAddress,
                            order.detailAddress,
                            order.deliveryRequest
                    ))
                    .from(order)
                    .where(order.no.eq(orderNo))
                    .fetchOne();

            if (orderDeliveryDto == null) {
                continue;
            }
/*
            List<OrderItemDeliveryDto> orderItemDeliveryDtoList = queryFactory
                    .select(Projections.fields(OrderItemDeliveryDto.class,
                            orderItem.no.as("orderItemNo"),
                            orderItem.itemName,
                            orderItem.count,
                            orderItem.price,
                            delivery.deliveryStatus,
                            delivery.courier,
                            delivery.trackingNumber))
                    .from(orderItem)
                    .leftJoin(orderItem.delivery, delivery)
                    .where(orderItem.order.no.eq(orderNo))
                    .fetch();

            orderDeliveryDto.setOrderItemDeliveryDtoList(orderItemDeliveryDtoList);
*/

            orderDeliveryDtoList.add(orderDeliveryDto);
        }
        return orderDeliveryDtoList;
    }

    @Override
    public List<OrderItemDeliveryDto> getOrderItemDeliveryDtoList(Long orderNo) {
        return queryFactory
                .select(Projections.fields(OrderItemDeliveryDto.class,
                        orderItem.no.as("orderItemNo"),
                        orderItem.itemName,
                        orderItem.count,
                        orderItem.price,
                        delivery.deliveryStatus,
                        delivery.courier,
                        delivery.trackingNumber,
                        orderItem.orderItemStatus))
                .from(orderItem)
                .leftJoin(orderItem.delivery, delivery)
                .where(orderItem.order.no.eq(orderNo))
                .fetch();
    }


    @Override
    public Delivery getDelivery(Long orderItemNo) {
        return queryFactory
                .select(QDelivery.delivery)
                .from(orderItem, QDelivery.delivery)
                .where(orderItem.delivery.no.eq(orderItemNo))
                .fetchOne();
    }
}
