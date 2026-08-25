package myshop.shop.entity.inquiry;

import jakarta.persistence.*;
import lombok.Getter;
import myshop.shop.entity.BaseDateEntity;
import myshop.shop.entity.item.Item;


@Entity
@Getter
@SequenceGenerator(name = "INQUIRY_SEQ", sequenceName = "INQUIRY_SEQ", initialValue = 1, allocationSize = 1)
public class Inquiry extends BaseDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INQUIRY_SEQ")
    @Column(name = "INQUIRY_NO")
    private Long no;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_NO")
    private Item item;

    private Long memberNo;
    private String optionName;
    private InquiryCategory inquiryCategory;
    private String title;
    private String content;
    private InquiryStatus inquiryStatus;
    private String answerContent;

    public Inquiry() {
    }

    public Inquiry(Item item, Long memberNo, String optionName, InquiryCategory inquiryCategory, String title, String content, InquiryStatus inquiryStatus) {
        this.item = item;
        this.memberNo = memberNo;
        this.optionName = optionName;
        this.inquiryCategory = inquiryCategory;
        this.title = title;
        this.content = content;
        this.inquiryStatus = inquiryStatus;
    }

    public Inquiry(Item item, Long memberNo, String optionName, InquiryCategory inquiryCategory, String title, String content, InquiryStatus inquiryStatus, String answerContent) {
        this.item = item;
        this.memberNo = memberNo;
        this.optionName = optionName;
        this.inquiryCategory = inquiryCategory;
        this.title = title;
        this.content = content;
        this.inquiryStatus = inquiryStatus;
        this.answerContent = answerContent;
    }


    public void updateAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public void updateInquiryStatus(InquiryStatus inquiryStatus) {
        this.inquiryStatus = inquiryStatus;
    }
}
