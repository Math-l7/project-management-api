package project_management_api.project_management_api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
public class SseController {

    private final Sinks.Many<String> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping(path = "/sse/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamNotifications() {
        return notificationSink.asFlux();
    }

    public void sendNotification(String message) {
        notificationSink.tryEmitNext(message).orThrow();
    }
}