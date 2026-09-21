package com.tuckersoft.branchengine.decision;

import java.time.Instant;

/**
 * Lleva todo lo que el listener necesita: corre en otro hilo, sin usuario autenticado.
 * {@code simulate} es el valor de la cabecera X-Bandersnatch-Simulate (puede ser null).
 */
public record DecisionCommittedEvent(Long decisionId, String recipientEmail, String recipientName,
                                     String playerTag, String branchType, String impactLevel,
                                     String handlerUnit, String outcomeCode, String sourceNodeCode,
                                     String resolvedNodeCode, String playthroughStatus, Integer lucidity,
                                     Integer controlLevel, String endingCode, String rawInput,
                                     Instant createdAt, String simulate) {
}
