# Preparação para F-Droid

O Num esquece mantém um único flavor de distribuição chamado `fdroid`.

## Estado da v1.0.0

A versão `1.0.0` usa `versionCode 10003` e está marcada pela tag `v1.0.0`. O repositório contém `.fdroid.yml` com a receita upstream correspondente para facilitar a inclusão no repositório oficial do F-Droid.

O F-Droid deve reconstruir o aplicativo a partir do código-fonte público e aplicar sua própria assinatura de distribuição. Nenhuma chave privada de produção é necessária no repositório público.

## Regras do APK F-Droid

O APK:

- não inclui serviços de sincronização remota;
- não declara `INTERNET` nem `ACCESS_NETWORK_STATE`;
- funciona sem conta;
- não contém anúncios, rastreamento, analytics ou telemetria;
- não exige pagamento para liberar funcionalidades;
- desativa backup automático e exclui os dados das regras de backup e transferência do Android;
- inclui metadados em português do Brasil, inglês, espanhol e francês.

As licenças e a proveniência dos recursos distribuídos estão documentadas em `docs/ASSETS.md`. O projeto inclui Gradle Wrapper com checksum da distribuição fixado.

Build esperado:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

## Submissão

A receita upstream usa:

```yaml
versionName: 1.0.0
versionCode: 10003
commit: v1.0.0
gradle:
  - fdroid
```

O arquivo correspondente a ser usado no `fdroiddata` é `metadata/com.cybertran.numesquece.yml`, baseado no conteúdo de `.fdroid.yml`.

Novos recursos visuais ou sonoros devem ter licença e proveniência registradas antes de serem publicados. Cada nova versão estável deve atualizar `versionName`, `versionCode`, tag e metadata de forma consistente.
