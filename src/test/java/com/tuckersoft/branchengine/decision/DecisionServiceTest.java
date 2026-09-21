package com.tuckersoft.branchengine.decision;

import com.tuckersoft.branchengine.node.StoryNode;
import com.tuckersoft.branchengine.node.StoryNodeRepository;
import com.tuckersoft.branchengine.playthrough.Playthrough;
import com.tuckersoft.branchengine.playthrough.PlaythroughRepository;
import com.tuckersoft.branchengine.user.User;
import com.tuckersoft.branchengine.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock DecisionRepository decisions;
    @Mock RealityLogRepository logs;
    @Mock PlaythroughRepository playthroughs;
    @Mock StoryNodeRepository nodes;
    @Mock UserService userService;
    @Mock ApplicationEventPublisher events;

    DecisionService service;
    Playthrough playthrough;
    StoryNode origin;

    @BeforeEach
    void setUp() {
        service = new DecisionService(decisions, logs, playthroughs, nodes, userService, events);

        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        owner.setEmail("qa@tuckersoft.test");
        owner.setDisplayName("Ada Lovelace");
        owner.setRole(User.ROLE_USER);

        origin = new StoryNode();
        ReflectionTestUtils.setField(origin, "id", 10L);
        origin.setNodeCode("NODE-A");
        origin.setPrimaryBranchCode("NODE-B");
        origin.setGlitchBranchCode("NODE-A");

        playthrough = new Playthrough();
        ReflectionTestUtils.setField(playthrough, "id", 100L);
        playthrough.setPlayerTag("STEFAN-01");
        playthrough.setUser(owner);
        playthrough.setStartNodeCode("NODE-A");
        playthrough.setCurrentNode(origin);
        playthrough.setLucidity(100);
        playthrough.setControlLevel(0);
        playthrough.setStatus(Playthrough.ACTIVA);
        playthrough.setCreatedAt(Instant.now());
        playthrough.setUpdatedAt(Instant.now());

        when(userService.currentUser()).thenReturn(owner);
        when(playthroughs.findById(100L)).thenReturn(Optional.of(playthrough));
        when(decisions.save(any(Decision.class))).thenAnswer(inv -> {
            Decision d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", 500L);
            return d;
        });
        // Solo los tests con destino existente usan este stub.
        org.mockito.Mockito.lenient().when(nodes.findByNodeCode("NODE-A")).thenReturn(Optional.of(origin));
        org.mockito.Mockito.lenient().when(nodes.findByNodeCode("NODE-B")).thenReturn(Optional.of(origin));
    }

    private DecisionResponse decidir(String texto, String impacto) {
        return service.create(new DecisionRequest(100L, texto, impacto), null);
    }

    @Test
    void destruye_la_camara_clasifica_como_ruptura_por_precedencia_de_reglas() {
        DecisionResponse res = decidir("Stefan destruye la camara", "LEVE");

        assertEquals("RUPTURA_CUARTA_PARED", res.branchType());
        assertEquals("Departamento Netflix", res.handlerUnit());
    }

    @Test
    void un_texto_sin_letras_es_entrada_corrupta_y_no_modifica_la_partida() {
        DecisionResponse res = decidir("%%% 0101 ### @@ 11", "CRITICO");

        assertEquals("ENTRADA_CORRUPTA", res.branchType());
        assertEquals("ERROR", res.status());
        assertNull(res.resolvedNodeCode());
        assertEquals(100, playthrough.getLucidity());
        assertEquals(0, playthrough.getControlLevel());
        assertEquals(Playthrough.ACTIVA, playthrough.getStatus());
        assertEquals(origin, playthrough.getCurrentNode());
        verify(playthroughs, never()).save(any(Playthrough.class));
    }

    @Test
    void impacto_critico_baja_40_de_lucidez_y_sube_45_de_control_sin_pasarse_de_los_limites() {
        DecisionResponse primera = decidir("Stefan sigue el guion previsto.", "CRITICO");
        assertEquals(60, primera.lucidity());
        assertEquals(45, primera.controlLevel());

        playthrough.setLucidity(10);
        playthrough.setControlLevel(90);
        playthrough.setStatus(Playthrough.ACTIVA);
        DecisionResponse limite = decidir("Stefan sigue el guion previsto.", "CRITICO");

        assertEquals(0, limite.lucidity());
        assertEquals(100, limite.controlLevel());
    }

    @Test
    void control_en_100_termina_con_pac_symbol_aunque_la_lucidez_tambien_sea_0() {
        playthrough.setLucidity(40);
        playthrough.setControlLevel(55);

        DecisionResponse res = decidir("Stefan sigue el guion previsto.", "CRITICO");

        assertEquals(0, res.lucidity());
        assertEquals(100, res.controlLevel());
        assertEquals("FINALIZADA", res.playthroughStatus());
        assertEquals("ENDING_PAC_SYMBOL", res.endingCode());
    }

    @Test
    void publish_event_se_llama_una_vez_en_decision_normal_y_cero_veces_en_entrada_corrupta() {
        decidir("Stefan acepta la oferta de Mohan y se queda.", "LEVE");
        verify(events, times(1)).publishEvent(ArgumentMatchers.any(DecisionCommittedEvent.class));

        org.mockito.Mockito.clearInvocations(events);
        decidir("%%% 0101 ### @@ 11", "LEVE");
        verify(events, never()).publishEvent(ArgumentMatchers.<Object>any());
    }
}
