package myshop.shop.llm.order.dto;

import lombok.RequiredArgsConstructor;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.orderItem.OrderItem;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftStore {

    private static final String PREFIX = "assistant:draft:";
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final String SEP = "|";

    private final StringRedisTemplate redis;

    /** 초안 저장. 금액은 호출자가 넘기지 않고 OrderItem.price에서 서버가 채운다. */
    public CancelDraftDto save(Long memberNo, OrderItem orderItem, CancelReasonCode reasonCode, String reasonDetail) {
        CancelDraftDto draft = new CancelDraftDto(
                UUID.randomUUID().toString(),
                memberNo,
                orderItem.getNo(),
                orderItem.getItemName(),
                orderItem.getPrice(),
                reasonCode,
                reasonDetail);
        redis.opsForValue().set(key(memberNo, draft.getId()), serialize(draft), TTL);
        return draft;
    }

    /** 조회와 동시에 삭제(GETDEL). 두 번째 호출, 만료, 타 회원 draftId는 모두 Optional.empty(). */
    public Optional<CancelDraftDto> take(Long memberNo, String draftId) {
        String value = redis.opsForValue().getAndDelete(key(memberNo, draftId));
        return Optional.ofNullable(value).map(v -> deserialize(memberNo, draftId, v));
    }

    private String key(Long memberNo, String draftId) {
        return PREFIX + memberNo + ":" + draftId;
    }

    // 자유 텍스트(itemName, reasonDetail)에 구분자가 섞여도 깨지지 않도록 Base64로 인코딩한다.
    private String serialize(CancelDraftDto d) {
        return String.join(SEP,
                String.valueOf(d.orderItemNo()),
                String.valueOf(d.refundAmount()),
                d.reasonCode().name(),
                encode(d.itemName()),
                encode(d.reasonDetail()));
    }

    private CancelDraftDto deserialize(Long memberNo, String draftId, String v) {
        String[] p = v.split("\\" + SEP, -1);
        return new CancelDraftDto(
                draftId,
                memberNo,
                Long.valueOf(p[0]),
                decode(p[3]),
                Integer.parseInt(p[1]),
                CancelReasonCode.valueOf(p[2]),
                decode(p[4]));
    }

    private String encode(String s) {
        return Base64.getUrlEncoder().encodeToString((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String s) {
        return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8);
    }
}
