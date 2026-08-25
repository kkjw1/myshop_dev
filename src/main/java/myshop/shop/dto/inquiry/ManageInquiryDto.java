package myshop.shop.dto.inquiry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import myshop.shop.entity.inquiry.InquiryCategory;
import myshop.shop.entity.inquiry.InquiryStatus;

import java.time.LocalDateTime;

@Getter @Setter
@ToString(of = {"inquiryNo", "inquiryCategory", "inquiryStatus", "inquiryCreateDate", "title", "content", "itemName", "itemOptionName", "answerContent"})
public class ManageInquiryDto {
    private Long inquiryNo;
    private InquiryCategory inquiryCategory;
    private InquiryStatus inquiryStatus;
    private LocalDateTime inquiryCreateDate;
    private String title;
    private String content;

    private String itemName;
    private String itemOptionName;
    private String answerContent;

    public ManageInquiryDto() {
    }
}
