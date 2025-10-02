package project_management_api.project_management_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project_management_api.project_management_api.dto.MessageReturnDTO;
import project_management_api.project_management_api.service.MessageService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageReturnDTO> getMessageById(@PathVariable Integer id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MessageReturnDTO>> search(@RequestParam Integer projectId,
            @RequestParam String text) {
        return ResponseEntity.ok(messageService.search(projectId, text));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

}
