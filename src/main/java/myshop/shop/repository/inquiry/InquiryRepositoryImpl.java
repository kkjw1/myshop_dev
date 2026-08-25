package myshop.shop.repository.inquiry;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import myshop.shop.controller.HomeItemController.DetailItemInquiryDto;
import myshop.shop.dto.inquiry.CheckInquiryDto;
import myshop.shop.dto.inquiry.ManageInquiryDto;
import myshop.shop.dto.inquiry.SearchInquiryDto;
import myshop.shop.entity.inquiry.InquiryCategory;
import myshop.shop.entity.inquiry.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;


import java.util.ArrayList;
import java.util.List;

import static myshop.shop.entity.inquiry.QInquiry.inquiry;
import static myshop.shop.entity.item.QItem.item;

@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom{
    private final JPAQueryFactory queryFactory;


    @Override
    public List<CheckInquiryDto> getCheckInquiryDtoList(Long memberNo) {

        return queryFactory
                .select(Projections.fields(CheckInquiryDto.class,
                        inquiry.no,
                        inquiry.inquiryCategory,
                        item.name.as("itemName"),
                        inquiry.optionName,
                        inquiry.title,
                        inquiry.content,
                        inquiry.inquiryStatus,
                        inquiry.memberNo,
                        inquiry.answerContent
                ))
                .from(inquiry)
                .leftJoin(inquiry.item, item)
                .where(inquiry.memberNo.eq(memberNo))
                .orderBy(inquiry.createdDate.desc())
                .fetch();
    }

    @Override
    public Page<DetailItemInquiryDto> findDetailItemInquiry(Pageable pageable, Long itemNo) {
        List<DetailItemInquiryDto> content = queryFactory
                .select(Projections.fields(DetailItemInquiryDto.class,
                        inquiry.memberNo,
                        item.name.as("itemName"),
                        inquiry.optionName,
                        inquiry.inquiryCategory,
                        inquiry.title,
                        inquiry.content,
                        inquiry.inquiryStatus,
                        inquiry.answerContent
                ))
                .from(inquiry)
                .leftJoin(inquiry.item, item)
                .where(inquiry.item.no.eq(itemNo))
                .orderBy(inquiry.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(inquiry.item.no.eq(itemNo));

        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }


    @Override
    public Page<ManageInquiryDto> findManageInquiry(Pageable pageable, List<Long> itemNoList, SearchInquiryDto searchInquiryDto) {

        List<ManageInquiryDto> content = queryFactory
                .select(Projections.fields(ManageInquiryDto.class,
                        inquiry.no.as("inquiryNo"),
                        inquiry.inquiryCategory,
                        inquiry.inquiryStatus,
                        inquiry.createdDate.as("inquiryCreateDate"),
                        inquiry.title,
                        inquiry.content,
                        item.name.as("itemName"),
                        inquiry.optionName.as("itemOptionName"),
                        inquiry.answerContent
                ))
                .from(inquiry)
                .leftJoin(inquiry.item, item)
                .where(
                        item.no.in(itemNoList),
                        inquiryStatusEq(searchInquiryDto.getInquiryStatus()),
                        inquiryCategoryEq(searchInquiryDto.getInquiryCategory()),
                        searchInputLike(searchInquiryDto.getSearchInput())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiry.item, item)
                .where(
                        item.no.in(itemNoList),
                        inquiryStatusEq(searchInquiryDto.getInquiryStatus()),
                        inquiryCategoryEq(searchInquiryDto.getInquiryCategory()),
                        searchInputLike(searchInquiryDto.getSearchInput())
                );

        return PageableExecutionUtils.getPage(content, pageable, () -> count.fetchOne());
    }

    public BooleanExpression inquiryStatusEq(InquiryStatus inquiryStatus) {
        return inquiryStatus != null ? inquiry.inquiryStatus.eq(inquiryStatus) : null;
    }

    public BooleanExpression inquiryCategoryEq(InquiryCategory inquiryCategory) {
        return inquiryCategory != null ? inquiry.inquiryCategory.eq(inquiryCategory) : null;
    }

    // 문의 제목, 옵션이름, 상품이름
    public BooleanExpression searchInputLike(String searchInput) {
        if (searchInput == null || searchInput.isBlank()) {
            return null; // 조건 없음 (전체 조회)
        }
        return inquiry.title.contains(searchInput).or(inquiry.optionName.contains(searchInput)).or(item.name.contains(searchInput));
    }
}
