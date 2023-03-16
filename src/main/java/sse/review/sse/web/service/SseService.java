package sse.review.sse.web.service;

import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sse.review.sse.util.NotificationUtil;
import sse.review.sse.util.NotificationUtil.EmitterNotification;
import sse.review.sse.util.NotificationUtil.EmitterNotification.EventType;

@Slf4j
@Service
public class SseService {

    private final NotificationUtil notificationUtil;

    public SseService(NotificationUtil notificationUtil) {
        this.notificationUtil = notificationUtil;
    }

    public SseEmitter subscribe(HttpServletRequest request) {
        String userId = request.getSession().getId();

        SseEmitter emitter = notificationUtil.makeSseEmitter(userId);

        notificationUtil.save(userId, emitter);

        EmitterNotification emitterNotification = EmitterNotification.of(EventType.CONNECTION, Strings.EMPTY, "subscribed");
        notificationUtil.send(emitter, userId, emitterNotification);

        return emitter;
    }

    @Scheduled(fixedDelay = 6000)
    private void sendMessage() {
        Map<String, SseEmitter> emitterMap = notificationUtil.getEmitterMap();

        EmitterNotification emitterNotification = EmitterNotification.of(EventType.NOTICE, "title", "message send ! at " + System.currentTimeMillis());
        for(Entry<String, SseEmitter> entry : emitterMap.entrySet()) {
            notificationUtil.send(entry.getValue(), entry.getKey(), emitterNotification);
        }
    }
}
