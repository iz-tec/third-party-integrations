package io.iztec.tp.integration.tns.sim;

import io.iztec.tp.commons.core.dto.ApiResponse;
import io.iztec.tp.integration.tns.dto.sim.TnsSimPatchRequest;
import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes TNS SIM endpoints:
 * - GET  /tns/sims         — list all SIMs
 * - GET  /tns/sims/{id}    — get a single SIM by ID
 * - PATCH /tns/sims/{id}   — NOT YET AVAILABLE (requires write permission on TNS)
 */
@RestController
@RequestMapping("/tns/sims")
@RequiredArgsConstructor
public class TnsSimController {

    private final TnsSimService simService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TnsSimResponse>>> listSims() {
        return ResponseEntity.ok(ApiResponse.ok(simService.listSims()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TnsSimResponse>> getSimById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(simService.getSimById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TnsSimResponse>> patchSim(
            @PathVariable Integer id,
            @Valid @RequestBody TnsSimPatchRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.fail("PATCH /tns/sims is not enabled yet"));
    }
}

