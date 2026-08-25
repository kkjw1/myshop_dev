package myshop.shop.dto.inquiry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(of = {"inquiryNo", "answerContent"})
public class UpdateInquiryDto {
    private Long inquiryNo;
    private String answerContent;

    public UpdateInquiryDto() {
    }
}
