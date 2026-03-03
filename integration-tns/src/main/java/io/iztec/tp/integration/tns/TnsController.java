package io.iztec.tp.integration.tns;

import io.iztec.tp.commons.core.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tns/notifications")
public class TnsController {

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> listNotifications() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendNotification() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
