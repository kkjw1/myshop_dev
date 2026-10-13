package myshop.shop.repository.order;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import myshop.shop.controller.memberWeb.OrderController;
import myshop.shop.dto.order.DirectOrderDto;

import static myshop.shop.entity.item.QItem.item;
import static myshop.shop.entity.item.QItemImage.itemImage;
import static myshop.shop.entity.item.QItemOption.itemOption;


@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    // checkDirectOrderDto(itemNo=3, memberNo=5, itemOptionNo=null, itemImageNo=7, count=1)
    @Override
    public DirectOrderDto getDirectOrder(OrderController.CheckDirectOrderDto checkDirectOrderDto) {
        // 옵션 X
        if (checkDirectOrderDto.getItemOptionNo() == null) {
            return queryFactory
                    .select(Projections.fields(DirectOrderDto.class,
                            item.no.as("itemNo"),
                            item.name,
                            item.totalStock,
                            item.originalPrice,
                            item.discountPer,
                            itemImage.imageUrl.as("imagePath")))
                    .from(itemImage)
                    .leftJoin(itemImage.item, item)
                    .where(itemImage.no.eq(checkDirectOrderDto.getItemImageNo()))
                    .fetchOne();
        }
        // 옵션 O
        DirectOrderDto directOrderDto = queryFactory
                .select(Projections.fields(DirectOrderDto.class,
                        item.no.as("itemNo"),
                        itemOption.no.as("itemOptionNo"),
                        item.name,
                        item.totalStock,
                        itemOption.optionStock,
                        item.originalPrice,
                        item.discountPer,
                        itemOption.additionalPrice.as("optionPrice"),
                        itemOption.name.as("optionName")))
                .from(itemOption)
                .leftJoin(itemOption.item, item)
                .where(itemOption.no.eq(checkDirectOrderDto.getItemOptionNo()))
                .fetchOne();

        String imagePath = em.createQuery("select ii.imageUrl from ItemImage ii where ii.item.no=:itemNo and ii.isMain=true", String.class)
                .setParameter("itemNo", checkDirectOrderDto.getItemNo())
                .getSingleResult();
        directOrderDto.setImagePath(imagePath);

        return directOrderDto;
    }
}
