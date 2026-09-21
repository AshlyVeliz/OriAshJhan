package com.tuckersoft.branchengine.notification;

import com.tuckersoft.branchengine.decision.Decision;
import com.tuckersoft.branchengine.decision.DecisionCommittedEvent;
import com.tuckersoft.branchengine.decision.DecisionRepository;
import com.tuckersoft.branchengine.decision.RealityLog;
import com.tuckersoft.branchengine.decision.RealityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/** Envia el Informe de Realidad despues del COMMIT, en un hilo aparte. */
@Component
public class BranchNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(BranchNotificationListener.class);
    static final String SIMULATE_MAIL_FAILURE = "MAIL_FAILURE";

    private final DecisionRepository decisions;
    private final RealityLogRepository realityLogs;
    private final JavaMailSender mailSender;
    private final String from;

    public BranchNotificationListener(DecisionRepository decisions, RealityLogRepository realityLogs,
                                      JavaMailSender mailSender,
                                      @Value("${spring.mail.username:qa@tuckersoft.test}") String from) {
        this.decisions = decisions;
        this.realityLogs = realityLogs;
        this.mailSender = mailSender;
        this.from = from == null || from.isBlank() ? "qa@tuckersoft.test" : from;
    }

    @Async("branchExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCommit(DecisionCommittedEvent event) {
        Decision decision = decisions.findById(event.decisionId()).orElse(null);
        if (decision == null) {
            log.error("Decision {} no encontrada tras el commit", event.decisionId());
            return;
        }
        decision.setStatus(Decision.PROCESANDO);
        decision.setUpdatedAt(Instant.now());
        decisions.saveAndFlush(decision);

        String subject = "[TUCKERSOFT] " + event.branchType() + " en " + event.playerTag()
                + " | Impacto " + event.impactLevel();
        RealityLog entry = new RealityLog();
        entry.setDecision(decision);
        entry.setRecipientEmail(event.recipientEmail());
        entry.setSubject(subject);
        entry.setCreatedAt(Instant.now());

        try {
            if (SIMULATE_MAIL_FAILURE.equals(event.simulate())) {
                throw new MailSendException("Fallo de correo simulado (X-Bandersnatch-Simulate: MAIL_FAILURE)");
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(event.recipientEmail());
            message.setSubject(subject);
            message.setText(body(event));
            mailSender.send(message);

            entry.setLogStatus(RealityLog.SENT);
            entry.setSentAt(Instant.now());
            decision.setStatus(Decision.ESTABILIZADA);
        } catch (Exception e) {
            log.error("No se pudo enviar el Informe de Realidad de la decision {}", event.decisionId(), e);
            entry.setLogStatus(RealityLog.FAILED);
            entry.setErrorMessage(e.getMessage() == null ? e.toString() : e.getMessage());
            decision.setStatus(Decision.ERROR);
        }
        decision.setUpdatedAt(Instant.now());
        decisions.save(decision);
        realityLogs.save(entry);

        log.info("[BRANCH-LOG] Decision ID: {} | Player: {} | Branch: {} | Impact: {} | Unit: {} | "
                        + "Node: {} -> {} | Thread: {} | Status: {}",
                event.decisionId(), event.playerTag(), event.branchType(), event.impactLevel(),
                event.handlerUnit(), event.sourceNodeCode(), event.resolvedNodeCode(),
                Thread.currentThread().getName(), decision.getStatus());
    }

    static String body(DecisionCommittedEvent e) {
        String ending = e.endingCode() == null ? "-" : e.endingCode();
        return """
                Hola %s,

                Una partida de prueba acaba de ramificarse.

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Decision ID      : #%d
                Jugador          : %s
                Rama             : %s
                Impacto          : %s
                Departamento     : %s
                Consecuencia     : %s
                Nodo origen      : %s
                Nodo destino     : %s
                Estado partida   : %s
                Lucidez          : %d/100
                Nivel de control : %d/100
                Final            : %s
                Registrada       : %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                Decisión original del jugador:
                "%s"

                — Tuckersoft Branch Engine, 1984
                """.formatted(e.recipientName(), e.decisionId(), e.playerTag(), e.branchType(),
                e.impactLevel(), e.handlerUnit(), e.outcomeCode(), e.sourceNodeCode(),
                e.resolvedNodeCode(), e.playthroughStatus(), e.lucidity(), e.controlLevel(), ending,
                e.createdAt(), e.rawInput());
    }
}
