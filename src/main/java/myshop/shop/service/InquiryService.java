package myshop.shop.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import myshop.shop.controller.HomeItemController;
import myshop.shop.dto.inquiry.*;
import myshop.shop.entity.inquiry.Inquiry;
import myshop.shop.entity.inquiry.InquiryCategory;
import myshop.shop.entity.inquiry.InquiryStatus;
import myshop.shop.entity.item.Item;
import myshop.shop.repository.Item.ItemRepository;
import myshop.shop.repository.inquiry.InquiryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final ItemRepository itemRepository;
    private final EntityManager em;

    /**
     * 문의하기
     * 주문 목록/배송 조회 -> 문의하기
     * 취소/반품 내역 -> 문의하기
     */
    public void saveInquiry(SaveInquiryDto saveInquiryDto) {
        Item itemProxy = itemRepository.getReferenceById(saveInquiryDto.getItemNo());

        inquiryRepository.save(new Inquiry(itemProxy,
                saveInquiryDto.getMemberNo(),
                saveInquiryDto.getOptionName(),
                saveInquiryDto.getInquiryCategory(),
                saveInquiryDto.getTitle(),
                saveInquiryDto.getContent(),
                InquiryStatus.답변대기));
    }


    /**
     * 상품/기타 문의하기
     * 아이템 상세 -> 문의하기
     */
    public void saveProductInquiry(SaveInquiryDto saveInquiryDto) {
        Item itemProxy = itemRepository.getReferenceById(saveInquiryDto.getItemNo());

        inquiryRepository.save(new Inquiry(itemProxy,
                saveInquiryDto.getMemberNo(),
                saveInquiryDto.getOptionName(),
                InquiryCategory.PRODUCT,
                saveInquiryDto.getTitle(),
                saveInquiryDto.getContent(),
                InquiryStatus.답변대기));
    }


    /**
     * 문의내역 확인 폼
     */
    public List<CheckInquiryDto> getInquiryList(Long memberNo) {
        return inquiryRepository.getCheckInquiryDtoList(memberNo);
    }


    /**
     * 상품문의 내역
     * 상품 상세 -> 상품문의
     */
    public Page<HomeItemController.DetailItemInquiryDto> getItemDetailInquiry(Pageable pageable, Long itemNo) {
        return inquiryRepository.findDetailItemInquiry(pageable, itemNo);
    }


    /**
     * 고객 문의 폼
     * 판매자 페이지 -> 고객 문의
     */
    public Page<ManageInquiryDto> getManageInquiry(Pageable pageable, Long sellerNo, SearchInquiryDto searchInquiryDto) {
        List<Long> itemNoList = itemRepository.getSellerItemNo(sellerNo);
        return inquiryRepository.findManageInquiry(pageable, itemNoList, searchInquiryDto);
    }


    /**
     * 답변 대기, 답변 완료 카운트 조회
     * 판매자 페이지 -> 고객 문의
     */
    public Map<InquiryStatus, Long> countInquiryStatus(Long sellerNo) {
        List<Long> itemNoList = itemRepository.getSellerItemNo(sellerNo);


        List<Inquiry> inquiryList = em.createQuery("select i from Inquiry i where i.item.no IN :itemNoList", Inquiry.class)
                .setParameter("itemNoList", itemNoList)
                .getResultList();


        Map<InquiryStatus, Long> count = new HashMap<>(Map.of(
                InquiryStatus.답변대기, 0L,
                InquiryStatus.답변완료, 0L
        ));


        for (Inquiry inquiry : inquiryList) {
            if (inquiry.getInquiryStatus() == InquiryStatus.답변대기) {
                count.replace(InquiryStatus.답변대기, count.get(InquiryStatus.답변대기) + 1);
            }
            else if (inquiry.getInquiryStatus() == InquiryStatus.답변완료) {
                count.replace(InquiryStatus.답변완료, count.get(InquiryStatus.답변완료) + 1);
            }
        }

        return count;
    }



    /**
     * 고객 문의 답변 작성
     * 고객 문의 폼 -> 답변 하기
     */
    public void replyInquiry(UpdateInquiryDto updateInquiryDto) {
        Inquiry inquiryNo = em.createQuery("select i from Inquiry i where i.no=:inquiryNo", Inquiry.class)
                .setParameter("inquiryNo", updateInquiryDto.getInquiryNo())
                .getSingleResult();
        inquiryNo.updateAnswerContent(updateInquiryDto.getAnswerContent());
        inquiryNo.updateInquiryStatus(InquiryStatus.답변완료);
    }
}
