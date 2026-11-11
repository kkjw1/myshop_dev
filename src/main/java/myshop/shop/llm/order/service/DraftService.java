package myshop.shop.llm.order.service;

import lombok.RequiredArgsConstructor;
import myshop.shop.entity.cancelRequest.CancelReasonCode;
import myshop.shop.entity.orderItem.OrderItem;
import myshop.shop.llm.order.dto.CancelDraftDto;
import myshop.shop.service.RedisService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * 취소 신청서 초안 저장소. RedisService를 통해 Redis에 저장한다.
 * key: assistant:draft:{memberNo}:{draftId}  (TTL 10분)
 */
@Component
@RequiredArgsConstructor
public class DraftService {

    private static final String PREFIX = "assistant:draft:";
    private static final long TTL_MINUTES = 10;
    private static final String SEP = "|";

    private final RedisService redisService;

    /** 초안 저장. 금액/수량은 호출자가 아니라 OrderItem에서 서버가 채운다. */
    public CancelDraftDto save(Long memberNo, OrderItem orderItem, CancelReasonCode reasonCode, String reasonDetail) {
        CancelDraftDto draft = new CancelDraftDto(
                UUID.randomUUID().toString(), memberNo, orderItem.getNo(), orderItem.getCount(),
                orderItem.getItemName(), orderItem.getPrice(), reasonCode, reasonDetail);
        redisService.saveString(key(memberNo, draft.getId()), serialize(draft), TTL_MINUTES);
        return draft;
    }

    /** 조회 후 삭제. 두 번 확정, 만료, 타 회원 draftId는 모두 empty. */
    public Optional<CancelDraftDto> take(Long memberNo, String draftId) {
        String value = redisService.getAndDeleteData(key(memberNo, draftId));
        return Optional.ofNullable(value).map(v -> deserialize(memberNo, draftId, v));
    }

    /** 초안 폐기. */
    public void discard(Long memberNo, String draftId) {
        redisService.deleteData(key(memberNo, draftId));
    }

    private String key(Long memberNo, String draftId) {
        return PREFIX + memberNo + ":" + draftId;
    }

    // 자유 텍스트에 구분자가 섞여도 깨지지 않도록 Base64로 인코딩한다.
    private String serialize(CancelDraftDto d) {
        return String.join(SEP,
                String.valueOf(d.getOrderItemNo()),
                String.valueOf(d.getCount()),
                d.getPrice().toPlainString(),
                d.getReasonCode().name(),
                encode(d.getItemName()),
                encode(d.getReasonDetail()));
    }

    private CancelDraftDto deserialize(Long memberNo, String draftId, String v) {
        String[] p = v.split("\\" + SEP, -1);
        return new CancelDraftDto(draftId, memberNo, Long.valueOf(p[0]), Integer.parseInt(p[1]),
                decode(p[4]), new BigDecimal(p[2]), CancelReasonCode.valueOf(p[3]), decode(p[5]));
    }

    private String encode(String s) {
        return Base64.getUrlEncoder().encodeToString((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String s) {
        return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8);
    }
}
