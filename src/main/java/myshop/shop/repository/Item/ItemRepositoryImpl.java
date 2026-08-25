package myshop.shop.repository.Item;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import myshop.shop.controller.sellerWeb.ItemController.BulkModifyItemDto;
import myshop.shop.dto.item.*;

import myshop.shop.entity.item.Item;
import myshop.shop.entity.item.ItemStatus;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static myshop.shop.entity.QSeller.seller;
import static myshop.shop.entity.item.QItem.item;
import static myshop.shop.entity.item.QItemImage.itemImage;
import static myshop.shop.entity.item.QItemOption.itemOption;


@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;


    /**
     * 상품 관리 데이터 불러오기
     * Item & ItemImage -> ManageItemDto
     */
    @Override
    public Page<ManageItemDto> searchItemPage(Pageable pageable, SearchItemDto searchItemDto) {

        List<ManageItemDto> content = queryFactory
                .select(Projections.fields(ManageItemDto.class,
                        item.no.as("itemNo"),
                        itemImage.imageUrl.as("mainImagePath"),
                        item.name,
                        item.originalPrice.as("price"),
                        item.totalStock,
                        item.itemStatus,
                        item.createdDate))
                .from(itemImage)
                .join(itemImage.item, item)
                .join(item.seller, seller)
                .where(sellerNoEq(searchItemDto.getSellerNo()), itemNoEq(searchItemDto.getItemNo()),
                        itemNameLike(searchItemDto.getName()), itemStatusEq(searchItemDto.getItemStatus()),
                        itemImage.isMain.eq(true))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = queryFactory
                .select(item.count())
                .from(item)
                .leftJoin(item.seller, seller)
                .leftJoin(item.itemImages, itemImage)
                .where(sellerNoEq(searchItemDto.getSellerNo()), itemNoEq(searchItemDto.getItemNo()),
                        itemNameLike(searchItemDto.getName()), itemStatusEq(searchItemDto.getItemStatus()),
                        itemImage.isMain.eq(true));

        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }

    private BooleanExpression sellerNoEq(Long sellerNo) {
        return sellerNo != null ? seller.no.eq(sellerNo) : null;
    }

    private BooleanExpression itemNoEq(Long itemNo) {
        return itemNo != null ? item.no.eq(itemNo) : null;
    }

    private BooleanExpression itemNameLike(String name) {
        return name != null ? item.name.contains(name) : null;
    }

    private BooleanExpression itemStatusEq(ItemStatus itemStatus) {
        return itemStatus != null ? item.itemStatus.eq(itemStatus) : null;
    }


    /**
     * 상품 일괄 수정
     */
    @Override
    public long bulkItemStatusDiscount(BulkModifyItemDto bulkModifyItemDto) {
        JPAUpdateClause query = queryFactory.update(item);

        if(bulkModifyItemDto.getItemStatus() != null) {
            query.set(item.itemStatus, bulkModifyItemDto.getItemStatus());
        }
        if(bulkModifyItemDto.getDiscountPer() != null) {
            query.set(item.discountPer, bulkModifyItemDto.getDiscountPer());
        }

        return query.where(item.no.in(bulkModifyItemDto.getItemNos()))
                .execute();
    }


    /**
     * 메인 화면 상품 출력
     * viewCount desc
     */
    @Override
    public List<MainItemDto> findMainItem(Long limit) {

        return queryFactory
                .select(Projections.fields(MainItemDto.class,
                        item.no.as("itemNo"),
                        itemImage.imageUrl.as("mainImagePath"),
                        item.name,
                        item.originalPrice.as("price"),
                        item.discountPer,
                        item.viewCount))
                .from(itemImage)
                .join(itemImage.item, item)
                .where(itemImage.isMain.eq(true), item.itemStatus.eq(ItemStatus.판매중))
                .orderBy(item.viewCount.desc())
                .limit(limit)
                .fetch();
    }



    /**
     * 상품 클릭 -> 상품 상세 출력
     * Item & ItemOption -> DetailItemDto
     */
    @Override
    public DetailItemDto findDetailItem(Long itemNo) {

        Item detailItem = queryFactory
                .select(item)
                .from(item)
                .leftJoin(item.itemOptions, itemOption).fetchJoin()
                .where(item.no.eq(itemNo))
                .fetchOne();

        if (detailItem != null) {
            return new DetailItemDto(
                    detailItem.getItemCategory(),
                    detailItem.getName(),
                    detailItem.getOriginalPrice(),
                    detailItem.getDiscountPer(),
                    detailItem.getTotalStock(),
                    detailItem.getItemOptions(),
                    detailItem.getContent()
            );
        }
        return null;
    }



    /**
     * 이미지 주소들 가져오기
     * sortOrder ASC
     */
    @Override
    public Map<Long, String> getImageUrls(Long itemNo) {
        List<Tuple> fetch = queryFactory
                .select(itemImage.no,
                        itemImage.imageUrl)
                .from(itemImage)
                .where(itemImage.item.no.eq(itemNo))
                .orderBy(itemImage.sortOrder.asc())
                .fetch();

        return fetch.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(itemImage.no),
                        tuple -> tuple.get(itemImage.imageUrl)
                ));
    }


    @Override
    public List<Long> getSellerItemNo(Long sellerNo) {
        return queryFactory
                .select(item.no)
                .from(item)
                .where(item.seller.no.eq(sellerNo))
                .fetch();
    }
}
