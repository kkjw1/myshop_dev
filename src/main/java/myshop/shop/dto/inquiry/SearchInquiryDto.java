package myshop.shop.dto.inquiry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.inquiry.InquiryCategory;
import myshop.shop.entity.inquiry.InquiryStatus;

@Getter @Setter
@ToString
public class SearchInquiryDto {
    public InquiryStatus inquiryStatus;
    public InquiryCategory inquiryCategory;
    public String searchInput;          // 주문번호, 문의 제목

    public SearchInquiryDto() {
    }
}
