package com.tuckersoft.branchengine.playthrough;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** No lleva usuario: el dueño sale del token. */
public record PlaythroughRequest(
        @NotBlank @Size(min = 2, max = 40) String playerTag,
        @NotBlank String startNodeCode) {
}
