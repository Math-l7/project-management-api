package project_management_api.project_management_api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
public class SseController {

    // Sinks é a fila de eventos que os clientes vão receber
    private final Sinks.Many<String> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    // Endpoint que o front vai se conectar para receber notificações
    @GetMapping(path = "/sse/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamNotifications() {
        return notificationSink.asFlux();
    }

    // Método chamado pelo NotificationService para enviar notificações
    public void sendNotification(String message) {
        // Emit com checagem de falha (mais seguro)
        notificationSink.tryEmitNext(message).orThrow();
    }
}