package sse.review.sse.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class NotificationUtil {

    @Getter
    @ToString
    public static class EmitterNotification {

        public enum EventType {
            CONNECTION, NOTICE
        }
        private final EventType type;
        private final String title;
        private final String message;

        private EmitterNotification(EventType type, String title, String message) {
            this.type = type;
            this.title = title;
            this.message = message;
        }

        public static EmitterNotification of(EventType type, String title, String message) {
            return new EmitterNotification(type, title, message);
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();


    public SseEmitter makeSseEmitter(String id) {
        if(ObjectUtils.isEmpty(id)) {
            throw new IllegalArgumentException();
        }

        long DEFAULT_TIMEOUT = 30 * 1000;

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onCompletion(() -> removeById(id));
        emitter.onTimeout(() -> removeById(id));

        return emitter;
    }

    public void save(String id, SseEmitter sseEmitter) {
        if(ObjectUtils.isEmpty(sseEmitter)) {
            throw new IllegalArgumentException();
        }

        log.info("emitter subscribe: {}", id);
        emitterMap.put(id, sseEmitter);
    }

    private void removeById(final String id) {
        log.info("emitter remove: {}", id);
        emitterMap.remove(id);
    }

    public Map<String, SseEmitter> getEmitterMap() {
        return this.emitterMap;
    }

    public void send(SseEmitter emitter, String id, EmitterNotification emitterNotification) {
        try {
            final String eventType = "SSE";
            log.info("emitter send : {}, type: {}, message: {}", id, eventType, emitterNotification);
            emitter.send(SseEmitter.event()
                                   .id(id)
                                   .name(eventType)
                                   .data(objectMapper.writeValueAsString(emitterNotification)));
        } catch(IOException e) {
            removeById(id);
            log.error("emitter send failed", e);
        }
    }
}
