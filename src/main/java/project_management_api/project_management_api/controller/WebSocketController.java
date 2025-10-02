package project_management_api.project_management_api.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import project_management_api.project_management_api.dto.MessageInputDTO;
import project_management_api.project_management_api.dto.MessageReturnDTO;
import project_management_api.project_management_api.service.MessageService;

@Controller
public class WebSocketController {

    private final MessageService messageService;

    public WebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/project/{projectId}/send")
    @SendTo("/topic/project/{projectId}")
    public MessageReturnDTO sendMessage(@DestinationVariable Integer projectId,
            @Payload MessageInputDTO messageDto,
            Principal principal) { // <-- ESSENCIAL: Garante que o usuário está autenticado

        // O Service AINDA pode usar userService.getAuthenticatedUser() aqui dentro.
        return messageService.sendMessage(projectId, messageDto);
    }

    @MessageMapping("/message/{messageId}/markRead")
    @SendTo("/topic/message/{messageId}")
    public MessageReturnDTO markRead(@DestinationVariable Integer messageId,
            Principal principal) { // <-- ESSENCIAL

        return messageService.markRead(messageId);
    }

    @MessageMapping("/message/{messageId}/delete")
    @SendTo("/topic/message/{messageId}/deleted")
    public void delete(@DestinationVariable Integer messageId,
            Principal principal) { // <-- ESSENCIAL

        messageService.deleteMessage(messageId);
    }
}