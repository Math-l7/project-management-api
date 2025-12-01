package project_management_api.project_management_api.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import project_management_api.project_management_api.dto.MessageInputDTO;
import project_management_api.project_management_api.dto.MessageReturnDTO;
import project_management_api.project_management_api.service.MessageService;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final MessageService messageService;

    @MessageMapping("/project/{projectId}/send")
    @SendTo("/topic/project/{projectId}")
    public MessageReturnDTO sendMessage(@DestinationVariable Integer projectId,
            @Payload MessageInputDTO messageDto,
            Principal principal) {

        return messageService.sendMessage(projectId, messageDto);
    }

    @MessageMapping("/message/{messageId}/markRead")
    @SendTo("/topic/message/{messageId}")
    public MessageReturnDTO markRead(@DestinationVariable Integer messageId,
            Principal principal) {

        return messageService.markRead(messageId);
    }

    @MessageMapping("/message/{messageId}/delete")
    @SendTo("/topic/message/{messageId}/deleted")
    public void delete(@DestinationVariable Integer messageId,
            Principal principal) {

        messageService.deleteMessage(messageId);
    }
}