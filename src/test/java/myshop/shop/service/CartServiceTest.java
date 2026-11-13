package myshop.shop.service;

import jakarta.persistence.EntityManager;
import myshop.shop.controller.memberWeb.CartController;
import myshop.shop.dto.cart.ManageCartDto;
import myshop.shop.dto.cart.SaveCartDto;
import myshop.shop.dto.item.AddItemDto;
import myshop.shop.dto.item.AddItemOptionDto;
import myshop.shop.entity.Cart;
import myshop.shop.entity.Seller;
import myshop.shop.entity.item.ItemCategory;
import myshop.shop.entity.item.ItemStatus;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;
import myshop.shop.entity.member.MemberLevel;
import myshop.shop.repository.Item.ItemOptionRepository;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.repository.cart.CartRepository;
import myshop.shop.repository.member.MemberRepository;
import myshop.shop.repository.seller.SellerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;


@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired CartService cartService;
    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired ItemOptionRepository itemOptionRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepository;
    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    @Commit
    public void init() {

        String memberPassword = passwordEncoder.encode("test");
        Member admin = new Member("test", "kkjjoo1212@naver.com", memberPassword,
                "테스트아이디", "KT", "010-4710-6305", Gender.MAN, MemberLevel.vip);
        memberRepository.save(admin);

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
        itemService.saveItemForTest(AddItemDto.builder()
                .sellerNo(sellerNo)
                .name("상품테스트1")
                .itemCategory(ItemCategory.상의)
                .price(BigDecimal.valueOf(30000))
                .totalStock(40)
                .discountPer(BigDecimal.valueOf(10))
                .addItemOptionDtoList(List.of(
                        AddItemOptionDto.builder()
                                .name("검정색")
                                .additionalPrice(BigDecimal.valueOf(0))
                                .optionStock(10)
                                .build(),
                        AddItemOptionDto.builder()
                                .name("나이키")
                                .additionalPrice(BigDecimal.valueOf(3000))
                                .optionStock(10)
                                .build(),
                        AddItemOptionDto.builder()
                                .name("흰색")
                                .additionalPrice(BigDecimal.valueOf(1000))
                                .optionStock(10)
                                .build(),
                        AddItemOptionDto.builder()
                                .name("로고")
                                .additionalPrice(BigDecimal.valueOf(3000))
                                .optionStock(10)
                                .build()
                ))
                .mainImage(null)
                .mainImagePath("/shop_image/8ea0eafb-b1a7-492c-b574-654946184243.png")
                .subImages(null)
                .subImagesPath(List.of(
                        "/shop_image/0d57c72e-ad5c-463d-b404-93b58fc020e7.png",
                        "/shop_image/00e766c7-b5b7-46bb-8b9b-50bb5079668b.png",
                        "/shop_image/1c7e2d70-4ef2-4fd9-862e-40226846215c.png"
                ))
                .content("상품옵션있음, 추가이미지있음")
                .itemStatus(ItemStatus.판매중)
                .useOptions(true)
                .viewCount(10L)
                .build());

/*        List<AddItemOptionDto> emptyAddItemOptionDtoList = new ArrayList<>();
        itemService.saveItem(AddItemDto.builder()
                .sellerNo(sellerNo)
                .name("상품테스트3(옵션X)")
                .itemCategory(ItemCategory.아우터)
                .price(BigDecimal.valueOf(250000))
                .totalStock(20)
                .discountPer(BigDecimal.valueOf(0))
                .addItemOptionDtoList(emptyAddItemOptionDtoList)
                .mainImage(null)
                .mainImagePath("/shop_image/30537136-0d60-450e-8544-2f9eda4e4800.png")
                .subImages(null)
                .subImagesPath(List.of(
                        "/shop_image/b3ba7699-d546-4075-84a9-9b6888049a0c.png"
                ))
                .content("상품옵션없음, 추가이미지있음")
                .itemStatus(ItemStatus.판매중)
                .useOptions(true)
                .build());*/

        em.flush();
        em.clear();

    }
    
    @Test
    @DisplayName("장바구니 처음 저장")
    public void saveCartTest() throws Exception {
        //given
        SaveCartDto saveCartDto = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(1L)
                .count(5)
                .build();

        //when
        cartService.saveCart(saveCartDto);
        em.flush();
        em.clear();

        //then
        Cart cart = cartRepository.findById(1L).orElse(null);
        assertThat(cart.getCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("같은 물품 장바구니에 저장")
    public void saveCartTest2() throws Exception {
        //given
        SaveCartDto saveCartDto = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(1L)
                .count(5)
                .build();
        cartService.saveCart(saveCartDto);
        em.flush();
        em.clear();

        //when
        SaveCartDto saveCartDto2 = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(1L)
                .count(4)
                .build();
        cartService.saveCart(saveCartDto2);

        //then
        Cart cart = cartRepository.findById(1L).orElse(null);
        assertThat(cart.getCount()).isEqualTo(9);
    }
    
    @Test
    @DisplayName("다른 물품 장바구니에 저장")
    public void saveCartTest3() throws Exception {
        //given
        SaveCartDto saveCartDto = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(1L)
                .count(5)
                .build();
        cartService.saveCart(saveCartDto);
        em.flush();
        em.clear();

        //when
        SaveCartDto saveCartDto2 = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(2L)
                .count(4)
                .build();
        cartService.saveCart(saveCartDto2);

        //then
        Member member = memberRepository.getReferenceById(1L);
        List<Cart> cartList = cartRepository.findByMember(member);
        assertThat(cartList.size()).isEqualTo(2);
        assertThat(cartList.get(0).getCount()).isEqualTo(5);
        assertThat(cartList.get(1).getCount()).isEqualTo(4);

    }
    

    @Test
    public void findAllCartTest() throws Exception {
        //given
        SaveCartDto saveCartDto = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(1L)
                .count(5)
                .build();
        cartService.saveCart(saveCartDto);
        SaveCartDto saveCartDto2 = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(2L)
                .count(4)
                .build();
        cartService.saveCart(saveCartDto2);
        SaveCartDto saveCartDto3 = SaveCartDto.builder()
                .itemNo(1L)
                .memberNo(1L)
                .itemOptionNo(1L)
                .count(2)
                .build();
        cartService.saveCart(saveCartDto3);
        em.flush();
        em.clear();


        //when
        List<ManageCartDto> allCart = cartService.findAllCart(1L);
        for (ManageCartDto manageCartDto : allCart) {
            System.out.println("manageCartDto = " + manageCartDto);
        }


    }

}