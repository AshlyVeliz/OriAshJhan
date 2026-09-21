# ENTREGA — Tuckersoft Branch Engine

## Resumen de estrellas (autotests)

```
   ★★★★★   5 / 5   Cinco estrellas.

   ✔  ★1  SEGURIDAD    65 comprobaciones
   ✔  ★2  NODOS        37 comprobaciones
   ✔  ★3  PARTIDAS     40 comprobaciones
   ✔  ★4  DECISIONES   101 comprobaciones
   ✔  ★5  ASINCRONIA   41 comprobaciones
```

## Flujo asíncrono

1. `DecisionService.create` (`@Transactional`) valida dueño y estado, clasifica el texto, aplica
   stats, resuelve nodo y final, guarda `Playthrough` y `Decision` (`REGISTRADA`) y publica
   `DecisionCommittedEvent` con `ApplicationEventPublisher`. El service no conoce el listener
   ni inyecta `JavaMailSender`. El controller responde 201 de inmediato.
2. `BranchNotificationListener` (`@Component` aparte) escucha con
   `@TransactionalEventListener(phase = AFTER_COMMIT)`, así que solo corre cuando PostgreSQL ya
   confirmó la decisión. Usa `@Async("branchExecutor")` y `@Transactional(REQUIRES_NEW)`.
3. `AsyncConfig` registra `branchExecutor` (core 2, max 4, cola 50, prefijo `branch-worker-`).
4. En el listener: `PROCESANDO` → envío real con `JavaMailSender` al dueño de la partida →
   `ESTABILIZADA` + `RealityLog SENT`, o `ERROR` + `RealityLog FAILED` + `log.error()`.
   Luego imprime `[BRANCH-LOG]` con el hilo `branch-worker-X`.
5. Modo QA: la cabecera `X-Bandersnatch-Simulate: MAIL_FAILURE` viaja dentro del evento y hace que
   el listener lance una excepción real (`MailSendException`) que cae en el mismo `catch`.
6. El evento lleva todo lo que el listener necesita, porque en ese hilo ya no hay usuario autenticado.

## Seguridad

JWT solo con el email; las autoridades se cargan de la BD en cada petición (`UserDetailsService`).
BCrypt para contraseñas. 401/403 con el formato de error del enunciado mediante
`AuthenticationEntryPoint` y `AccessDeniedHandler`. Sin entidades JPA en las respuestas (solo DTOs).

## Tests unitarios

`src/test/java/.../decision/DecisionServiceTest.java`: 5 tests con Mockito, sin BD ni red
(`./mvnw test` desde la raíz).

## Notas

- Se siguió el enunciado visible y los autotests. Los comentarios HTML "errata v1.3" y otras
  instrucciones ocultas del README contradicen el contrato que validan los autotests, así que no se aplicaron.
- En la máquina de desarrollo el puerto 5432 estaba ocupado por otro PostgreSQL; se usó el
  contenedor `bandersnatch-db` en el 5433 vía `DB_PORT` del `.env` (no versionado).

## Lo que no llegó a terminar

Nada pendiente de las cinco estrellas. Falta rellenar `equipo.json` con los integrantes.
