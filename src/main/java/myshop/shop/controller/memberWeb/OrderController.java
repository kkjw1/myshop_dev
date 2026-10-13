package myshop.shop.controller.memberWeb;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.dto.order.*;
import myshop.shop.dto.address.ManageAddressDto;
import myshop.shop.dto.cart.ManageCartDto;
import myshop.shop.dto.member.LoginCheckMemberDto;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemOption;
import myshop.shop.entity.order.Order;
import myshop.shop.repository.Item.ItemOptionRepository;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.service.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static myshop.shop.service.RedisService.RESERVE_KEY;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final AddressService addressService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ItemService itemService;

    private final ItemRepository itemRepository;
    private final ItemOptionRepository itemOptionRepository;

    private final RedisTemplate<String, Object> redisTemplate;



    /**
     * 주문/결제 폼
     * 장바구니 폼 -> 구매하기
     */
    @GetMapping("/myPage/order")
    public String orderForm(@ModelAttribute CartToOrderDto cartToOrderDto,
                            @AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
                            HttpServletRequest request, Model model) {
        log.info("cartToOrderDto={}", cartToOrderDto);

        Long memberNo = loginCheckMemberDto.getNo();

        // 구매 상품 재고 선점
        itemService.reserveStock(cartToOrderDto.getCartNo(), memberNo);

        // 배송지
        ManageAddressDto manageAddressDto = addressService.getAddressByMemberNo(memberNo);

        List<ManageCartDto> manageCartDtoList = new ArrayList<>();
        // Cart내용
        for (Long cartNo : cartToOrderDto.getCartNo()) {
            manageCartDtoList.add(cartService.findCart(cartNo));
        }


        model.addAttribute("totalProductPrice", cartToOrderDto.getTotalProductPrice());
        model.addAttribute("deliveryFee", cartToOrderDto.getDeliveryFee());
        model.addAttribute("totalOrderPrice", cartToOrderDto.getTotalOrderPrice());
        model.addAttribute("manageAddressDto", manageAddressDto);
        model.addAttribute("manageCartDtoList", manageCartDtoList);

        return "member/mypage/order";
    }

    @Getter @Setter
    @ToString(of = {"cartNo", "totalProductPrice", "deliveryFee", "totalOrderPrice"})
    public static class CartToOrderDto {
        private List<Long> cartNo = new ArrayList<>();
        private BigDecimal totalProductPrice;
        private int deliveryFee;
        private BigDecimal totalOrderPrice;

        public CartToOrderDto() {
        }
    }




    /**
     * 주문/결제 폼
     * 상품 상세 폼 -> 바로 구매
     */
    @PostMapping("/myPage/directOrder")
    public String directOrderForm(@ModelAttribute CheckDirectOrderDto checkDirectOrderDto,
                                  @AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        Long memberNo = loginCheckMemberDto.getNo();
        checkDirectOrderDto.setMemberNo(memberNo);
        log.info("주문/결제 폼, checkDirectOrderDto={}", checkDirectOrderDto);

        // 재고 확인
        int stock;
        if (checkDirectOrderDto.getItemOptionNo() == null) {
            Item item = itemRepository.findById(checkDirectOrderDto.getItemNo()).orElse(null);
            stock = item.getTotalStock();
        } else {
            ItemOption itemOption = itemOptionRepository.findById(checkDirectOrderDto.getItemOptionNo()).orElse(null);
            stock = itemOption.getOptionStock();
        }
        if (stock < checkDirectOrderDto.getCount()) {
            // 재고 없는 경우 알람띄우기
            log.info("재고 없음, stock={}, 요청개수={}", stock, checkDirectOrderDto.getCount());
            redirectAttributes.addFlashAttribute("alertMessage", "재고가 부족합니다.");
            return "redirect:/item?itemNo=" + checkDirectOrderDto.getItemNo();
        }
        // 구매 상품 재고 선점
        itemService.reserveStock(checkDirectOrderDto);

        // 배송지
        ManageAddressDto manageAddressDto = addressService.getAddressByMemberNo(memberNo);

        // 구매 상품 정보
        DirectOrderDto directOrder = orderService.getDirectOrder(checkDirectOrderDto);
        log.info("directOrder={}", directOrder);

        // 배송지 설정
        BigDecimal totalProductPrice = directOrder.getPrice().multiply(BigDecimal.valueOf(directOrder.getCount()));
        int deliveryFee = totalProductPrice.compareTo(BigDecimal.valueOf(30000)) > 0 ? 0 : 3000;

        model.addAttribute("totalProductPrice", totalProductPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("totalOrderPrice", totalProductPrice.add(BigDecimal.valueOf(deliveryFee)));
        model.addAttribute("manageAddressDto", manageAddressDto);
        model.addAttribute("directOrder", directOrder);

        return "member/mypage/direct_order";
    }
    @Getter @Setter
    @ToString(of = {"itemNo", "memberNo", "itemOptionNo", "itemImageNo", "count"})
    public static class CheckDirectOrderDto {
        private Long itemNo;
        private Long memberNo;
        private Long itemOptionNo;      // null or Data
        private Long itemImageNo;
        private int count;

        public CheckDirectOrderDto() {
        }

        public CheckDirectOrderDto(Long itemNo, Long memberNo, Long itemOptionNo, Long itemImageNo, int count) {
            this.itemNo = itemNo;
            this.memberNo = memberNo;
            this.itemOptionNo = itemOptionNo;
            this.itemImageNo = itemImageNo;
            this.count = count;
        }
    }

    /**
     * 동시성 테스트용
     */
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "재고가 부족합니다.";
    }



    /**
     * 주문/결제 폼 -> 배송지 변경
     */
    @GetMapping("/myPage/order/changeAddress")
    @ResponseBody
    public List<ManageAddressDto> changeAddress(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, HttpServletRequest request) {
//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        Long memberNo = loginCheckMemberDto.getNo();

        return addressService.getAddressesByMemberNo(memberNo);
    }



    /**
     * 결제 완료(장바구니)
     * 주문/결제 폼 -> 결제하기
     */
    //reserve:5:cart:[1, 2, 3]
    @PostMapping("/myPage/order/payment")
    @ResponseBody
    public Long payment(@RequestBody AddOrderDto addOrderDto,
                        @AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, HttpServletRequest request) {
        log.info("addOrderDto={}", addOrderDto);
//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);

        Long memberNo = loginCheckMemberDto.getNo();
        List<AddOrderItemDto> addOrderItemDtoList = addOrderDto.getAddOrderItemDtoList();

        List<Long> cartNoList = new ArrayList<>();
        for (AddOrderItemDto addOrderItemDto : addOrderItemDtoList) {
            cartNoList.add(addOrderItemDto.getCartNo());
        }


  /*      StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (AddOrderItemDto addOrderItemDto : addOrderItemDtoList) {
            // getCartNo()가 null이 아닐 때만 추가되도록 안전하게 처리
            if (addOrderItemDto.getCartNo() != null) {
                joiner.add(addOrderItemDto.getCartNo().toString());
            }
        }
        String cartNo = joiner.toString();*/

        Boolean hasKey = redisTemplate.hasKey(RESERVE_KEY + memberNo + ":cart:" + cartNoList);
        if (!hasKey) {  // 주문시간 만료
            log.info("장바구니 결제 주문시간 만료");
            return 0L;
        }

        // redis 키 삭제, 만료 이벤트 방지
        redisTemplate.delete(RESERVE_KEY + memberNo + ":cart:" + cartNoList);

        // 주문, 배송, 상품 주문 추가
        Order order = orderService.saveOrder(memberNo, addOrderDto);

        // 장바구니에서 제거하기
        for (AddOrderItemDto addOrderItemDto : addOrderItemDtoList) {
            if (addOrderItemDto.getCartNo() != null) {
                cartService.removeCart(addOrderItemDto.getCartNo());
            }
        }
        return order.getNo();
    }



    /**
     * 결제 완료(바로구매)
     * 주문/결제 폼 -> 결제하기
     * addOrderDto의 cartNo=null
     */
    //reserve:5:direct:3:null:1
    @PostMapping("/myPage/order/directPayment")
    @ResponseBody
    public Long directPayment(@RequestBody AddOrderDto addOrderDto,
                              @AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto, HttpServletRequest request) {
        log.info("addOrderDto={}", addOrderDto);

//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        Long memberNo = loginCheckMemberDto.getNo();

        AddOrderItemDto addOrderItemDto = addOrderDto.getAddOrderItemDtoList().get(0);
        Boolean hasKey = redisTemplate.hasKey(RESERVE_KEY + memberNo + ":direct:" + addOrderItemDto.getItemNo() + ":" +
                addOrderItemDto.getOptionNo() + ":" + addOrderItemDto.getCount());

        if (!hasKey) {  // 주문시간 만료
            log.info("바로구매 결제 주문시간 만료");
            return 0L;
        }

        // redis 키 삭제, 만료 이벤트 방지
        redisTemplate.delete(RESERVE_KEY+memberNo+":direct:"+addOrderItemDto.getItemNo()+":"+
                addOrderItemDto.getOptionNo()+":"+addOrderItemDto.getCount());

        // 주문, 배송, 상품 주문 추가
        Order order = orderService.saveOrder(memberNo, addOrderDto);

        return order.getNo();
    }


    /**
     * 주문 상세 화면
     * 주문/결제 폼 -> 결제하기
     * 주문 목록/배송 조회 폼 -> 주문 상세 보기
     */
    @GetMapping("/order/complete")
    public String orderComplete(@RequestParam("orderNo") Long orderNo, HttpServletRequest request, Model model) {
//        new LoginCheckMemberDto().loginCheck(request, model);

        log.info("orderComplete, orderNo={}", orderNo);
        DetailOrderDto detailOrderDto = orderService.getDetailOrder(orderNo);
        log.info("detailOrderDto={}", detailOrderDto);

        model.addAttribute("detailOrderDto", detailOrderDto);
        model.addAttribute("orderNo", orderNo);
        return "member/mypage/order_complete";
    }



    /**
     * 주문 목록/배송 조회 폼
     * 주문 상세 내역 -> 주문 내역 보기
     * 마이페이지 -> 주문 목록/배송 조회
     */
    @GetMapping("/myPage/orderList")
    public String orderListForm(@AuthenticationPrincipal LoginCheckMemberDto loginCheckMemberDto,
            HttpServletRequest request, Model model) {
//        new LoginCheckMemberDto().loginCheck(request, model);

//        LoginCheckMemberDto loginCheckMemberDto = (LoginCheckMemberDto) request.getSession().getAttribute(LOGIN_MEMBER);
        Long memberNo = loginCheckMemberDto.getNo();

        List<ManageOrderDto> manageOrderDtoList = orderService.getManageOrder(memberNo);
        log.info("manageOrderDtoList={}", manageOrderDtoList);

        model.addAttribute("manageOrderDtoList", manageOrderDtoList);
        return "member/mypage/order_list";
    }


}
