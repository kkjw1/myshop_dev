package myshop.shop.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.service.ItemService.ItemStockUpdateDto;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final ItemService itemService;
    private final StringRedisTemplate stringRedisTemplate;

    public static final String RESERVE_KEY = "Reserve:";
    private static final String REFRESH_KEY = "Refresh:";

    // TTL 설정
    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this,
                new PatternTopic("__keyevent@0__:expired"));
        log.info("Redis keyspace Notification 리스너 등록");
    }

    /**
     * 개인정보 확인
     * ModifyCheckPW:{ID}
     */
    public void saveData(String key, String value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
        log.info("Redis 저장 ({}:{}), {}분 후 만료", key, value, time);
    }

    /**
     * refresh 토큰 저장
     */
    public void tokenSave(String memberId, String refreshToken, long validityMillis) {
        stringRedisTemplate.opsForValue().set(
                REFRESH_KEY + memberId,
                refreshToken,
                Duration.ofMillis(validityMillis)
        );
    }


    /**
     * refresh 토큰 삭제
     */
    public void tokenDelete(String memberId) {
        stringRedisTemplate.delete(REFRESH_KEY + memberId);
    }


    /**
     * refresh토큰 가져오기
     */
    public String getToken(String memberId) {
        return stringRedisTemplate.opsForValue().get(REFRESH_KEY + memberId);
    }


    public Optional<String> findBymemberNo(Long memberNo) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(REFRESH_KEY + memberNo));
    }


//    /**
//     * 결제
//     * Reserve:{memberNo}:direct:{itemNo}:{optionNo}:{count}
//     */
//    public void directReserve(String key, Map<String, Object> value, int seconds) {
//        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
//        log.info("장바구니 재고 선점 완료, Redis 저장 ({}:{}), {}초 후 만료", key, value, seconds);
//    }
//
//    /**
//     * 결제
//     * Reserve:{memberNo}:cart:{cartNoList}
//     */
//    public void cartReserve(String key, Map<Long, ItemStockUpdateDto> value, int seconds) {
//        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
//        log.info("장바구니 재고 선점 완료, Redis 저장 ({}:{}), {}초 후 만료", key, value, seconds);
//    }

    public String getData(String key) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (String) valueOperations.get(key);
    }

    /** 문자열 그대로 저장(JSON 따옴표 없음). 상담 에이전트 초안용. */
    public void saveString(String key, String value, long minutes) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofMinutes(minutes));
    }

    /** 조회와 동시에 삭제(GETDEL). 문자열 값 전용. */
    public String getAndDeleteData(String key) {
        return stringRedisTemplate.opsForValue().getAndDelete(key);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String expiredKey = new String(message.getBody());
        log.info("Redis 키 만료 감지, {}", expiredKey);


        /**
         * Reserve:{memberNo}:direct:{itemNo}:{optionNo}:{count}
         * Reserve:{memberNo}:cart:{cartNoList}
         */
        if (expiredKey.startsWith(RESERVE_KEY))     {
            String[] keySplit = expiredKey.split(":");
            if (keySplit[2].equals("direct")) {
                Long itemNo = Long.valueOf(keySplit[3]);
                Long optionNo = keySplit[4].equals("null") ? null : Long.valueOf(keySplit[4]);
                int count = Integer.parseInt(keySplit[5]);

                itemService.directRollbackStock(itemNo, optionNo, count);
            }
            if (keySplit[2].equals("cart")) {
                String cartStr = keySplit[3]; // "[1, 2, 3]"
                String cleanStr = cartStr.replaceAll("[\\[\\]\\s]", "");
                List<Long> cartList = Arrays.stream(cleanStr.split(","))
                        .map(Long::valueOf)
                        .toList();

                itemService.cartRollbackStock(cartList);
            }
        }
    }
}
