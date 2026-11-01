# CityConnect Services — estrutura distribuída em Java

Protótipo didático de uma plataforma urbana organizada em serviços independentes. O projeto ilustra nós, comunicação REST e por eventos, isolamento de falhas e escala horizontal local. Não é uma configuração pronta para produção.

## Serviços e comunicação

- **Gateway Nginx** (`:8080`): entrada HTTP e balanceamento `least_conn` entre duas instâncias do serviço de transporte.
- **Transit Service**: consulta demonstrativa de chegadas e publicação de alertas de trânsito. Publica `TransitAlertCreated` no exchange RabbitMQ `cityconnect.events`.
- **Appointments Service**: API REST para criar e consultar agendamentos. O exemplo guarda dados em memória.
- **Notification Service**: consome eventos da fila durável `notifications.transit-alerts`.
- **RabbitMQ**: comunicação assíncrona; painel local em `http://localhost:15672` (credenciais apenas para desenvolvimento: `cityconnect` / `cityconnect-dev`).
- **common**: contrato versionado do evento; não contém lógica de negócio compartilhada.

## Executar localmente

Requer Docker com Docker Compose. Na pasta deste projeto:

```bash
docker compose up --build 
docker Finaliza -- Detach
docker compose ps                 # estado de cada contêiner
docker compose logs -f transit-1  # logs de um serviço
docker compose up -d --build      # subir em segundo plano, sem prender o terminal
```

APIs disponíveis em `http://localhost:8080`:

```bash
curl http://localhost:8080/api/transit/routes/23/arrivals
curl -X POST http://localhost:8080/api/transit/alerts -H "Content-Type: application/json" -d '{"routeId":"23","message":"Atraso na linha"}'
curl -X POST http://localhost:8080/api/appointments -H "Content-Type: application/json" -d '{"citizenId":"cid-42","officeId":"centro","startsAt":"2026-11-01T14:00:00Z"}'
```

As duas instâncias de transporte demonstram distribuição de requisições. A fila desacopla a publicação do alerta do processamento pelo serviço de notificações. O RabbitMQ configurado aqui é uma única instância e, portanto, não oferece alta disponibilidade do broker.

## Fundamentos demonstrados

| Fundamento | Onde aparece | Próximo passo para produção |
|---|---|---|
| Nós independentes | Contêineres por serviço; duas instâncias de transporte | Orquestrador com distribuição por zonas e autoscaling |
| REST | APIs de transporte e agendamento | Contratos OpenAPI, autenticação e limites por cliente |
| Event-driven | Evento de alerta publicado/consumido via AMQP | Outbox transacional, deduplicação, retry e dead-letter queue |
| Transparência de localização | Gateway encaminha para o upstream sem expor réplicas ao cliente | DNS/service discovery e balanceador redundante |
| Tolerância a falhas | Timeouts do proxy, health endpoints e fila persistente | Circuit breaker, bulkheads, broker em cluster e testes de caos |
| Replicação/escala | Réplicas do Transit Service atrás do Gateway | Réplicas sem estado e armazenamento externo compartilhado |
| Consistência | Contrato de evento com versão e ID | Política por domínio; consistência forte para reserva de vagas |

## Limites do protótipo e decisões necessárias

- Agendamentos são guardados em memória: reiniciar ou escalar o serviço perde dados e cada réplica teria uma visão diferente. Substituir por banco durável com restrição de unicidade/controle concorrente.
- A publicação RabbitMQ usa o fluxo simples de demonstração. Em produção, usar outbox transacional, publisher confirms, política de retry e dead-letter queue; consumidores devem deduplicar por `eventId`.
- A fila está configurada como durável, mas isso não substitui redundância do broker. Implantar cluster gerenciado ou RabbitMQ replicado, com backups e recuperação testada.
- O Nginx desta amostra é um único Gateway; em produção, replicá-lo e colocá-lo atrás de balanceador redundante.
- Integrações externas ainda são simuladas. Implementar adaptadores por provedor, timeouts, circuit breaker, cache com validade explícita e fallback com indicação de dado desatualizado.
- Adicionar segurança (OIDC/OAuth2, TLS, gestão de segredos), auditoria, métricas, tracing e SLOs antes de expor a plataforma.
- RPC/gRPC pode ser adotado para chamadas internas de baixa latência e contratos tipados; não é requisito para o primeiro corte. REST é mantido na borda e eventos são usados onde o acoplamento temporal deve ser reduzido.

## Estrutura

```text
common/                         evento compartilhado e versionado
services/transit-service/       API REST e produtor AMQP
services/appointments-service/  API REST de agendamentos
services/notification-service/ consumidor AMQP
gateway/nginx.conf              roteamento e balanceamento local
docker-compose.yml              topologia local multi-nó
```

## Desenvolvimento no VS Code

Veja [VSCODE.md](VSCODE.md) para o passo a passo com Java 25, depuração e testes das APIs (`api.http`).
