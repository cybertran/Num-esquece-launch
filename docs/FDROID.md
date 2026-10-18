# Preparação para F-Droid

O Num esquece mantém um único flavor de distribuição chamado `fdroid`.

## Estado da v1.1.0

A versão `1.1.0` usa `versionCode 10100` e é publicada pela tag `v1.1.0`. A metadata upstream preserva também a receita da `1.0.0` (`versionCode 10003`), mantendo o histórico de builds.

O F-Droid deve reconstruir o aplicativo a partir do código-fonte público e aplicar sua própria assinatura de distribuição. Nenhuma chave privada de produção é necessária no repositório público. Por isso, a GitHub Release da versão estável publica o código-fonte e a tag, mas não publica um APK oficial alternativo assinado pelo projeto.

A v1.1.0 acrescenta limite opcional de recorrência por data, com atalhos para fim da semana e fim do mês e escolha manual da data final, que é inclusiva.

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

## Metadata

A receita atual inclui:

```yaml
- versionName: 1.1.0
  versionCode: 10100
  commit: v1.1.0
  subdir: app
  gradle:
    - fdroid
```

`CurrentVersion` é `1.1.0` e `CurrentVersionCode` é `10100`. O arquivo correspondente no `fdroiddata`, quando presente no repositório oficial, é `metadata/com.cybertran.numesquece.yml` e deve apontar para a mesma tag e código.

O projeto usa `UpdateCheckMode: Tags`, portanto novas versões estáveis devem manter `versionName`, `versionCode`, tag e metadata coerentes. Novos recursos visuais ou sonoros devem ter licença e proveniência registradas antes da publicação.
