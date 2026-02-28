package io.iztec.tp.integration.tns.dto.sim;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record TnsSimPatchRequest(
        @NotNull @JsonProperty("phase__id") Integer phaseId
) {}

