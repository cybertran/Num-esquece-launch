# Preparação para F-Droid

O Num esquece mantém um flavor específico chamado `fdroid`.

## Regras do flavor F-Droid

O APK F-Droid:

- não inclui Google Play Services;
- não inclui o fluxo de sincronização Google;
- não declara `INTERNET` nem `ACCESS_NETWORK_STATE`;
- funciona sem conta;
- não contém anúncios, rastreamento ou analytics;
- não exige pagamento para liberar funcionalidades;
- inclui metadados em português do Brasil, inglês, espanhol e francês.

As licenças e a proveniência dos recursos distribuídos estão documentadas em `docs/ASSETS.md`. O projeto inclui Gradle Wrapper 9.6.0 com checksum da distribuição fixado.

Build esperado:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

## Antes da submissão oficial

1. Tornar o repositório publicamente acessível.
2. Criar a primeira versão estável e a tag Git correspondente.
3. Adicionar screenshots upstream ao metadata do F-Droid.
4. Definir a página de doação, caso desejada, e registrar o mesmo destino no README ou em `.github/FUNDING.yml`.
5. Abrir a solicitação de inclusão no repositório oficial do F-Droid.

O changelog do `versionCode 10002` já está incluído nos quatro idiomas. Novos recursos visuais ou sonoros devem ter licença/proveniência registradas antes de serem publicados.

A integração Google deve permanecer fora do flavor `fdroid`. O metadata de build do F-Droid deve selecionar explicitamente o flavor `fdroid`.
