# Preparação para F-Droid

O Num esquece mantém um flavor de distribuição chamado `fdroid`.

## Regras do APK F-Droid

O APK:

- não inclui serviços de sincronização remota;
- não declara `INTERNET` nem `ACCESS_NETWORK_STATE`;
- funciona sem conta;
- não contém anúncios, rastreamento, analytics ou telemetria;
- não exige pagamento para liberar funcionalidades;
- desativa backup automático e exclui os dados das regras de backup e transferência do Android;
- inclui metadados em português do Brasil, inglês, espanhol e francês.

As licenças e a proveniência dos recursos distribuídos estão documentadas em `docs/ASSETS.md`. O projeto inclui Gradle Wrapper 9.6.0 com checksum da distribuição fixado.

Build esperado:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

## Antes da submissão oficial

1. Concluir a auditoria e os testes em aparelho real.
2. Promover o snapshot aprovado para o repositório público.
3. Criar a primeira versão estável e a tag Git correspondente.
4. Adicionar screenshots upstream ao metadata do F-Droid.
5. Definir a página de doação, caso desejada, e registrar o mesmo destino no README ou em `.github/FUNDING.yml`.
6. Abrir a solicitação de inclusão no repositório oficial do F-Droid.

Novos recursos visuais ou sonoros devem ter licença e proveniência registradas antes de serem publicados.
