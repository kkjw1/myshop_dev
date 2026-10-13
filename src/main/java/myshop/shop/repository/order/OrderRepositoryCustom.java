package myshop.shop.repository.order;

import myshop.shop.controller.memberWeb.OrderController;
import myshop.shop.dto.order.DirectOrderDto;

public interface OrderRepositoryCustom {
    DirectOrderDto getDirectOrder(OrderController.CheckDirectOrderDto checkDirectOrderDto);
}
