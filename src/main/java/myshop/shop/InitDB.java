package myshop.shop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import myshop.shop.dto.cancelRequest.SaveCancelRequestDto;
import myshop.shop.dto.cart.SaveCartDto;
import myshop.shop.dto.item.AddItemDto;
import myshop.shop.dto.item.AddItemOptionDto;
import myshop.shop.dto.order.AddOrderDto;
import myshop.shop.dto.order.AddOrderItemDto;
import myshop.shop.dto.returnRequest.SaveReturnRequestDto;
import myshop.shop.entity.*;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.cancelRequest.CancelRequestStatus;
import myshop.shop.entity.delivery.DeliveryStatus;
import myshop.shop.entity.inquiry.Inquiry;
import myshop.shop.entity.inquiry.InquiryCategory;
import myshop.shop.entity.inquiry.InquiryStatus;
import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemCategory;
import myshop.shop.entity.item.ItemStatus;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;
import myshop.shop.entity.member.MemberLevel;
import myshop.shop.entity.orderItem.OrderItemStatus;
import myshop.shop.entity.returnRequest.ReturnReasonCode;
import myshop.shop.entity.returnRequest.ReturnRequestStatus;
import myshop.shop.entity.review.Review;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.repository.address.AddressRepository;
import myshop.shop.repository.inquiry.InquiryRepository;
import myshop.shop.repository.member.MemberRepository;
import myshop.shop.repository.review.ReviewRepository;
import myshop.shop.repository.seller.SellerRepository;
import myshop.shop.service.*;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
@Profile("local")
@RequiredArgsConstructor
public class InitDB {

    private final InitData initData;

    @PostConstruct
    public void init() {
        initData.dbInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitData {
        private final MemberRepository memberRepository;
        private final PasswordEncoder passwordEncoder;
        private final AddressRepository addressRepository;
        private final EntityManager em;
        private final SellerRepository sellerRepository;
        private final ItemService itemService;
        private final CartService cartService;
        private final OrderService orderService;
        private final CancelRequestService cancelRequestService;
        private final ReturnRequestService returnRequestService;
        private final ItemRepository itemRepository;
        private final ReviewRepository reviewRepository;
        private final InquiryRepository inquiryRepository;

        public void dbInit() {
            /**
             * 회원 저장
             */
            for(int i=1; i<5; i++) {
                String testPasswordEncode = passwordEncoder.encode("password" + i);
                Member member = new Member("id" + i, "email" + i, testPasswordEncode, "name" + i,
                        "telecom" + i, "010-0000-000" + i, Gender.MAN, MemberLevel.normal);
                memberRepository.save(member);
            }
            String memberPassword = passwordEncoder.encode("test");
            Member admin = new Member("test", "kkjjoo1212@naver.com", memberPassword,
                    "테스트아이디", "KT", "010-4710-6305", Gender.MAN, MemberLevel.vip);
            memberRepository.save(admin);
            em.flush();
            em.clear();


            /**
             * 주소 저장
             */
            Member findMember = memberRepository.findById("test").orElse(null);
            Address mainAddress = new Address(findMember, "테스트 메인주소", "메인수령인",
                    "010-1234-1234", "12345", "인천광역시 서구",
                    "A아파트", true);
            addressRepository.save(mainAddress);

            for (int i=1; i<5; i++) {
                addressRepository.save(new Address(findMember, "테스트 서브주소" + i, "수령인" + i,
                        "010-0000-000" + i, "0000" + i, "도로명" + i,
                        "아파트" + i, false));
            }


            /**
             * 판매자 저장
             */
            String sellerPassword = passwordEncoder.encode("test");
            Seller seller = new Seller("test", sellerPassword, "kkjjoo1212@naver.com", "판매자테스트", "010-4710-6305", "테스트회사명",
                    "테스트회사전화번호", "테스트회사우편번호", "테스트회사 도로명", "테스트회사 상세주소");
            sellerRepository.save(seller);


            /**
             * 상품 저장
             */
            Long sellerNo = sellerRepository.findById("test").orElse(null).getNo();
            itemService.saveItem(new AddItemDto(sellerNo, "상품테스트1", ItemCategory.상의, 27000, 48, 10,
                    List.of(
                            new AddItemOptionDto("검정색", 0, 4),
                            new AddItemOptionDto("나이키", 3000, 9),
                            new AddItemOptionDto("흰색", 1000, 15),
                            new AddItemOptionDto("로고", 3000, 20)
                    ), null, "/shop_image/8ea0eafb-b1a7-492c-b574-654946184243.jpg", null,
                    List.of(
                            "/shop_image/0d57c72e-ad5c-463d-b404-93b58fc020e7.png",
                            "/shop_image/00e766c7-b5b7-46bb-8b9b-50bb5079668b.png",
                            "/shop_image/1c7e2d70-4ef2-4fd9-862e-40226846215c.png"
                    ), "상품옵션있음, 추가이미지있음", ItemStatus.판매중 ,true));

            itemService.saveItem(new AddItemDto(sellerNo, "상품테스트2(품절)", ItemCategory.바지, 50000, 0, 0,
                    List.of(
                            new AddItemOptionDto("면바지", 0, 0),
                            new AddItemOptionDto("청바지", 10000, 0)
                    ), null, "/shop_image/1736f6e5-e78c-4f29-96d4-621fbfd034d0.png", null,
                    List.of(
                            "/shop_image/97297b82-81cd-4de7-ba70-bd4e467716df.png"
                    ), "판매완료", ItemStatus.품절,true));


            List<AddItemOptionDto> emptyAddItemOptionDtoList = new ArrayList<>();
            itemService.saveItem(new AddItemDto(sellerNo, "상품테스트3(옵션X)", ItemCategory.아우터, 250000, 20, 0,
                    emptyAddItemOptionDtoList, null, "/shop_image/30537136-0d60-450e-8544-2f9eda4e4800.png", null,
                    List.of(
                            "/shop_image/b3ba7699-d546-4075-84a9-9b6888049a0c.png"
                    ), "상품옵션없음, 추가이미지있음", ItemStatus.판매중, true));


            List<String> emptySubImageList = new ArrayList<>();
            itemService.saveItem(new AddItemDto(sellerNo, "상품테스트4(추가이미지X)", ItemCategory.신발, 150000, 10, 0,
                    List.of(
                            new AddItemOptionDto("250", 0, 10),
                            new AddItemOptionDto("260", 10000, 0)
                    ), null, "/shop_image/b3ecf6ae-140e-4950-81bf-74adce043239.png", null,
                    emptySubImageList, "상품옵션있음, 추가이미지없음", ItemStatus.판매중,true));


            for (int i=0; i<50; i++) {
                itemService.saveItem(new AddItemDto(sellerNo, "페이징테스트" + i, ItemCategory.신발, i*1000, i, i,
                        emptyAddItemOptionDtoList, null, "/shop_image/c9cf742d-c0b0-4b8d-9253-cc32823d36db.png", null,
                        emptySubImageList, "상품옵션없음, 추가이미지없음", ItemStatus.판매중,true));
            }

            /**
             * 장바구니 데이터
             */
            cartService.saveCart(new SaveCartDto(1L, 5L, 1L, 1));
            cartService.saveCart(new SaveCartDto(1L, 5L, 4L, 2));
            cartService.saveCart(new SaveCartDto(3L, 5L, null, 6));
            cartService.saveCart(new SaveCartDto(4L, 5L, 7L, 1));

            em.flush();
            em.clear();

            /**
             * 주문 목록 데이터
             */
            List<AddOrderItemDto> addOrderItemDtoList1 = new ArrayList<>();
            addOrderItemDtoList1.add(new AddOrderItemDto(1L, 1L, 1L, 1, null, BigDecimal.valueOf(24300),
                    "/shop_image/8ea0eafb-b1a7-492c-b574-654946184243.jpg", "상품테스트1", "검정색"));
            addOrderItemDtoList1.add(new AddOrderItemDto(3L, null, 3L, 6, null, BigDecimal.valueOf(1500000),
                    "/shop_image/30537136-0d60-450e-8544-2f9eda4e4800.png", "상품테스트3(옵션X)", ""));
            orderService.saveOrder(5L, new AddOrderDto("메인수령인", "010-1234-1234", "12345", "인천광역시 서구",
                    "A아파트", "배송 전 미리 연락 부탁드립니다.", addOrderItemDtoList1, BigDecimal.valueOf(1524300),
                    0, BigDecimal.valueOf(1524300)));

            List<AddOrderItemDto> addOrderItemDtoList2 = new ArrayList<>();
            addOrderItemDtoList2.add(new AddOrderItemDto(4L, 7L, 4L, 1, null, BigDecimal.valueOf(150000),
                    "/shop_image/b3ecf6ae-140e-4950-81bf-74adce043239.png", "상품테스트4(추가이미지X)", "250"));
            orderService.saveOrder(5L, new AddOrderDto("메인수령인", "010-1234-1234", "12345", "인천광역시 서구",
                    "A아파트", "배송 전 미리 연락 부탁드립니다.", addOrderItemDtoList2, BigDecimal.valueOf(150000),
                    0, BigDecimal.valueOf(150000)));

            List<AddOrderItemDto> addOrderItemDtoList3 = new ArrayList<>();
            addOrderItemDtoList3.add(new AddOrderItemDto(1L, 1L, 1L, 1, null, BigDecimal.valueOf(24300),
                    "/shop_image/8ea0eafb-b1a7-492c-b574-654946184243.jpg", "상품테스트1", "검정색"));
            orderService.saveOrder(5L, new AddOrderDto("메인수령인", "010-1234-1234", "12345", "인천광역시 서구",
                    "A아파트", "부재 시 문 앞에 놓아주세요.", addOrderItemDtoList3, BigDecimal.valueOf(24300),
                    3000, BigDecimal.valueOf(27300)));

            em.flush();
            em.clear();
            // 반품, 반품은 배송완료, 배송중 상태에서만 가능하다.
            em.createQuery("update Delivery d " +
                            "set d.deliveryStatus=:status, d.courier=:courier, d.trackingNumber=:trackingNumber " +
                            "where d.no = 1L")
                    .setParameter("status", DeliveryStatus.배송완료)
                    .setParameter("courier", "우체국택배")
                    .setParameter("trackingNumber", "12345432")
                    .executeUpdate();
            SaveReturnRequestDto returnRequestDto = new SaveReturnRequestDto(1L, 5L, 1, ReturnReasonCode.DEFECTIVE,
                    "모서리쪽 스크래치가 있음", BigDecimal.valueOf(24300), ReturnRequestStatus.요청);
            returnRequestService.saveReturnRequest(returnRequestDto);

            // 취소
            SaveCancelRequestDto saveCancelRequestDto = new SaveCancelRequestDto(3L, 4L, 5L, 1, CancelReasonCode.WRONG_ORDER,
                    "사이즈 잘못 선택함", BigDecimal.valueOf(24300), CancelRequestStatus.요청);
            cancelRequestService.saveCancelRequest(saveCancelRequestDto);

            // 취소2
            SaveCancelRequestDto saveCancelRequestDto2 = new SaveCancelRequestDto(1L, 2L, 5L, 6, CancelReasonCode.CHANGED_MIND,
                    "단순변심", BigDecimal.valueOf(1500000), CancelRequestStatus.요청);
            cancelRequestService.saveCancelRequest(saveCancelRequestDto2);


            em.createQuery("update CancelRequest cr set cr.createdDate=:updateDate, cr.lastModifiedDate=:updateDate2 where cr.no = 1L")
                    .setParameter("updateDate", LocalDateTime.of(2026, 7, 1, 0, 0))
                    .setParameter("updateDate2", LocalDateTime.of(2026, 7, 1, 0, 0))
                    .executeUpdate();

            em.createQuery("update ReturnRequest rr set rr.createdDate=:updateDate, rr.lastModifiedDate=:updateDate2 where rr.no = 1L")
                    .setParameter("updateDate", LocalDateTime.of(2026, 7, 4, 0, 0))
                    .setParameter("updateDate2", LocalDateTime.of(2026, 7, 4, 0, 0))
                    .executeUpdate();

            em.createQuery("update CancelRequest cr set cr.createdDate=:updateDate, cr.lastModifiedDate=:updateDate2 where cr.no = 2L")
                    .setParameter("updateDate", LocalDateTime.of(2026, 7, 7, 0, 0))
                    .setParameter("updateDate2", LocalDateTime.of(2026, 7, 7, 0, 0))
                    .executeUpdate();


            /**
             * 배송완료 데이터
             */
            List<AddOrderItemDto> addOrderItemDtoList4 = new ArrayList<>();
            addOrderItemDtoList4.add(new AddOrderItemDto(5L, 1L, 1L, 2, null, BigDecimal.valueOf(48600),
                    "/shop_image/8ea0eafb-b1a7-492c-b574-654946184243.jpg", "상품테스트1", "검정색"));
            orderService.saveOrder(5L, new AddOrderDto("메인수령인", "010-1234-1234", "12345", "인천광역시 서구",
                    "A아파트", "배송 전 미리 연락 부탁드립니다.", addOrderItemDtoList4, BigDecimal.valueOf(48600),
                    0, BigDecimal.valueOf(48600)));
            em.createQuery("update Delivery d " +
                            "set d.deliveryStatus=:status, d.courier=:courier, d.trackingNumber=:trackingNumber " +
                            "where d.no = 5L")
                    .setParameter("status", DeliveryStatus.배송완료)
                    .setParameter("courier", "우체국택배")
                    .setParameter("trackingNumber", "12345432")
                    .executeUpdate();
            em.createQuery("update OrderItem oi set oi.orderItemStatus=:orderItemStatus where oi.no = 5L")
                    .setParameter("orderItemStatus", OrderItemStatus.배송완료)
                    .executeUpdate();

            /**
             * 상품리뷰 데이터
             */
            //itemNo=1, itemOption 1,2,3,4 존재함
            Item itemProxy = itemRepository.getReferenceById(1L);
            for (int i=1; i<10; i++) {
                reviewRepository.save(new Review(itemProxy, 1L, "검정색", i, 5L, "상품 1번의 검정색 옵션 상품리뷰" + i));
            }
            for (int i=1; i<9; i++) {
                reviewRepository.save(new Review(itemProxy, 2L, "나이키", i, 4L, "상품 1번의 나이키 옵션 상품리뷰" + i));
            }
            for (int i=1; i<8; i++) {
                reviewRepository.save(new Review(itemProxy, 3L, "흰색", i, 3L, "상품 1번의 흰색 옵션 상품리뷰" + i));
            }
            for (int i=1; i<7; i++) {
                reviewRepository.save(new Review(itemProxy, 4L, "로고", i, 2L, "상품 1번의 로고 옵션 상품리뷰" + i));
            }
            for (int i=1; i<6; i++) {
                reviewRepository.save(new Review(itemProxy, 3L, "검정색", i, 1L, "상품 1번의 검정색 옵션 상품리뷰" + i));
            }


            /**
             * 상품문의 데이터
             * 상품 기타 문의 10개, 상품 반품 문의 5개, 결제 취소 문의 3개
             */

            for (int i=1; i<11; i++) {
                inquiryRepository.save(new Inquiry(itemProxy, 5L, "나이키", InquiryCategory.PRODUCT, "상품/기타 제목" + i, "기타문의 상세내용" + i,
                        InquiryStatus.답변완료, "문의 답변 완료" + i));
            }
            for (int i=1; i<6; i++) {
                inquiryRepository.save(new Inquiry(itemProxy, 5L, "검정색", InquiryCategory.CANCEL, "결제 취소 제목" + i, "결제 취소 상세내용" + i,
                        InquiryStatus.답변대기));
            }
            for (int i=1; i<6; i++) {
                inquiryRepository.save(new Inquiry(itemProxy, 5L, "흰색", InquiryCategory.RETURN, "상품 반품 문의" + i, "상품 반품 상세내용" + i,
                        InquiryStatus.답변완료, "상품 반품 답변 완료" + i));
            }

            em.flush();
            em.clear();
        }
    }
}
