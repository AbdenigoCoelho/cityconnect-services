# Guia: desenvolver o CityConnect no VS Code com Java 25

## 1. Pré-requisitos (Windows)

Abra o PowerShell e instale o que faltar:

```powershell
winget install EclipseAdoptium.Temurin.25.JDK
winget install Apache.Maven          # opcional se você usar o Maven integrado do VS Code
winget install Docker.DockerDesktop  # com a integração WSL2 ativada
```

Feche e reabra o terminal e confira:

```powershell
java -version      # deve mostrar 25.x
mvn -v             # "Java version: 25..." na saída
docker --version
docker compose version
```

Se `mvn -v` mostrar outra versão do Java, ajuste o `JAVA_HOME` para a pasta do JDK 25:

```powershell
# exemplo: ajuste o caminho para onde o JDK 25 foi instalado
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-25.0.x-hotspot", "User")
```

Reabra o VS Code depois disso. Se preferir trabalhar dentro do WSL2, instale JDK e Maven na distro (`sudo apt install openjdk-25-jdk maven`) e abra a pasta com a extensão **WSL** (`code .` dentro do WSL).

## 2. Extensões do VS Code

Ao abrir a pasta, o VS Code sugere as extensões de `.vscode/extensions.json`. Aceite todas:

- **Extension Pack for Java** (`vscjava.vscode-java-pack`): suporte a Java, Maven, debug e testes
- **Spring Boot Extension Pack** (`vmware.vscode-boot-dev-pack`): painel de apps Spring e autocomplete do `application.yml`
- **Docker** (`ms-azuretools.vscode-docker`)
- **REST Client** (`humao.rest-client`): executa o arquivo `api.http`
- **YAML** (`redhat.vscode-yaml`)

## 3. Apontar o VS Code para o JDK 25

O projeto declara `java.version=25` no `pom.xml`, então o VS Code precisa de um JDK 25 para compilar.

1. `Ctrl+Shift+P` → **Java: Configure Java Runtime**.
2. Em *Installed JDKs*, confirme que o JDK 25 aparece. Se não aparecer, abra `settings.json` do **usuário** (`Ctrl+Shift+P` → *Preferences: Open User Settings (JSON)*) e adicione, com o seu caminho real:

```json
"java.configuration.runtimes": [
  {
    "name": "JavaSE-25",
    "path": "C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.x-hotspot",
    "default": true
  }
]
```

3. `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → *Restart and delete*.

## 4. Abrir e importar o projeto

1. **File → Open Folder…** e escolha a pasta `cityconnect-services` (a que contém o `pom.xml` raiz).
2. Aguarde o canto inferior mostrar a importação do Maven terminando (a primeira vez baixa as dependências).
3. Na barra lateral, **Java Projects** deve listar `cityconnect-common`, `transit-service`, `appointments-service` e `notification-service`.

## 5. Primeiro build

`Ctrl+Shift+B` roda a tarefa **Maven: build + testes**. Ou no terminal:

```powershell
mvn -B package
```

O build roda os testes do Appointments e gera um `.jar` em `services/*/target/`.

## 6. Dois jeitos de rodar

### A) Tudo em contêineres (igual ao README)

```powershell
docker compose up --build
```

Quando os health checks passarem, as APIs respondem em `http://localhost:8080`. O painel do RabbitMQ fica em `http://localhost:15672` (`cityconnect` / `cityconnect-dev`, só para desenvolvimento).

### B) Serviços pelo VS Code, com depuração

1. Suba só o broker: `Ctrl+Shift+P` → **Tasks: Run Task** → *Docker: só o RabbitMQ*.
2. Abra **Run and Debug** (`Ctrl+Shift+D`), escolha **Todos os serviços (local)** e aperte F5.
3. Coloque breakpoints nos controllers e no `TransitAlertListener`.

Neste modo as portas são: Transit `8081`, Appointments `8082`, Notification `8083` (sem gateway).

## 7. Testar as APIs

Abra o `api.http` e clique em **Send Request** acima de cada bloco.

> **Windows:** no PowerShell, `curl` é um alias de `Invoke-WebRequest` e os comandos do README não funcionam como estão. Use `curl.exe` no lugar, por exemplo:
>
> ```powershell
> curl.exe http://localhost:8080/api/transit/routes/23/arrivals
> curl.exe -X POST http://localhost:8080/api/transit/alerts -H "Content-Type: application/json" -d "{\"routeId\":\"23\",\"message\":\"Atraso na linha\"}"
> ```
>
> Se você já usa o Insomnia, pode criar as mesmas requisições nele.

## 8. Checklist de validação

1. **Balanceamento:** chame `GET /api/transit/routes/23/arrivals` várias vezes pelo gateway e observe o header `X-Served-By` alternar entre `transit-1` e `transit-2`.
2. **Evento:** `POST /api/transit/alerts` e veja a linha `NOTIFICAÇÃO ...` em `docker compose logs -f notification`.
3. **Fila durável:** `docker compose stop notification`, publique alguns alertas, confira as mensagens acumuladas em *Queues* no painel do RabbitMQ e rode `docker compose start notification`: elas são consumidas.
4. **Falha de réplica:** `docker compose stop transit-1` e continue chamando as chegadas; o gateway responde pela outra.
5. **Concorrência no agendamento:** repita o mesmo `POST /api/appointments` e verifique o `409 Conflict`.

## 9. Problemas comuns

| Sintoma | Causa provável e solução |
|---|---|
| `release version 25 not supported` | O VS Code ou o Maven está usando um JDK mais antigo. Refaça as seções 1 e 3. |
| `Connection refused` para `localhost:5672` | RabbitMQ não está de pé. Rode a tarefa *Docker: só o RabbitMQ*. |
| Porta `8080` ou `15672` em uso | Pare o processo que a usa ou altere o mapeamento de portas no `docker-compose.yml`. |
| Classes não resolvem no editor | `Java: Clean Java Language Server Workspace` e reimporte o Maven. |
| `docker compose` não encontra o daemon | Abra o Docker Desktop e aguarde o status *Running*. |

## 10. Próximos passos

Siga a tabela "Limites do protótipo" do `README.md`. O primeiro passo mais valioso é trocar a memória do Appointments por PostgreSQL com restrição única em `(office_id, starts_at)`.
