package myshop.shop.service;

import myshop.shop.dto.item.AddItemDto;
import myshop.shop.dto.item.AddItemOptionDto;
import myshop.shop.entity.Seller;
import myshop.shop.entity.item.ItemCategory;
import myshop.shop.entity.item.ItemStatus;
import myshop.shop.repository.seller.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

@SpringBootTest
@Transactional
class FileServiceTest {

    @Autowired FileService fileService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    ItemService itemService;

/*    @BeforeEach
    @Commit
    public void init() {
        *//**
         * 판매자 저장
         *//*
        String sellerPassword = passwordEncoder.encode("test");
        Seller seller = new Seller("test", sellerPassword, "kkjjoo1212@naver.com", "판매자테스트", "010-4710-6305", "테스트회사명",
                "테스트회사전화번호", "테스트회사우편번호", "테스트회사 도로명", "테스트회사 상세주소");
        sellerRepository.save(seller);


        *//**
         * 상품 저장
         *//*
        Long sellerNo = sellerRepository.findById("test").orElse(null).getNo();
        itemService.saveItem(AddItemDto.builder()
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
                .itemStatus(ItemStatus.승인대기)
                .useOptions(true)
                .build());

        itemService.saveItem(AddItemDto.builder()
                .sellerNo(sellerNo)
                .name("상품테스트2(품절)")
                .itemCategory(ItemCategory.바지)
                .price(BigDecimal.valueOf(50000))
                .totalStock(0)
                .discountPer(BigDecimal.valueOf(0))
                .addItemOptionDtoList(List.of(
                        AddItemOptionDto.builder()
                                .name("면바지")
                                .additionalPrice(BigDecimal.valueOf(0))
                                .optionStock(0)
                                .build(),
                        AddItemOptionDto.builder()
                                .name("청바지")
                                .additionalPrice(BigDecimal.valueOf(10000))
                                .optionStock(0)
                                .build()
                ))
                .mainImage(null)
                .mainImagePath("/shop_image/1736f6e5-e78c-4f29-96d4-621fbfd034d0.png")
                .subImages(null)
                .subImagesPath(List.of(
                        "/shop_image/97297b82-81cd-4de7-ba70-bd4e467716df.png"
                ))
                .content("판매완료")
                .itemStatus(ItemStatus.품절)
                .useOptions(true)
                .build());


        List<AddItemOptionDto> emptyAddItemOptionDtoList = new ArrayList<>();
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
                .itemStatus(ItemStatus.승인대기)
                .useOptions(true)
                .build());


        List<String> emptySubImageList = new ArrayList<>();
        itemService.saveItem(AddItemDto.builder()
                .sellerNo(sellerNo)
                .name("상품테스트4(추가이미지X)")
                .itemCategory(ItemCategory.신발)
                .price(BigDecimal.valueOf(150000))
                .totalStock(20)
                .discountPer(BigDecimal.valueOf(0))
                .addItemOptionDtoList(List.of(
                        AddItemOptionDto.builder()
                                .name("250")
                                .additionalPrice(BigDecimal.valueOf(0))
                                .optionStock(10)
                                .build(),
                        AddItemOptionDto.builder()
                                .name("260")
                                .additionalPrice(BigDecimal.valueOf(10000))
                                .optionStock(10)
                                .build()
                ))
                .mainImage(null)
                .mainImagePath("/shop_image/b3ecf6ae-140e-4950-81bf-74adce043239.png")
                .subImages(null)
                .subImagesPath(emptySubImageList)
                .content("상품옵션있음, 추가이미지없음")
                .itemStatus(ItemStatus.승인대기)
                .useOptions(true)
                .build());


        for (int i=0; i<50; i++) {
            itemService.saveItem(AddItemDto.builder()
                    .sellerNo(sellerNo)
                    .name("페이징테스트" + i)
                    .itemCategory(ItemCategory.신발)
                    .price(BigDecimal.valueOf(i*1000))
                    .totalStock(i)
                    .discountPer(BigDecimal.valueOf(i))
                    .addItemOptionDtoList(emptyAddItemOptionDtoList)
                    .mainImage(null)
                    .mainImagePath("/shop_image/c9cf742d-c0b0-4b8d-9253-cc32823d36db.png")
                    .subImages(null)
                    .subImagesPath(emptySubImageList)
                    .content("상품옵션없음, 추가이미지없음")
                    .itemStatus(ItemStatus.승인대기)
                    .useOptions(true)
                    .build());
        }

    }*/


    @Test
    public void removeFileTest() throws Exception {
        //given
        fileService.removeFile("/shop_image/test.png");
    }
}