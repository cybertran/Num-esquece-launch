# Assinatura e builds de distribuição

O Num esquece possui dois flavors de distribuição.

## F-Droid

O flavor `fdroid` é o build destinado ao repositório oficial do F-Droid. Ele não depende da chave privada externa do projeto para ser compilado pelos servidores do F-Droid.

Build local:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

Sem credenciais de assinatura, o Gradle produz um APK release não assinado adequado para validação do build.

## Flavor Google

O flavor `google` preserva a integração opcional com Google Drive para distribuições externas ao F-Droid.

A assinatura externa usa uma chave de produção mantida fora do repositório. As variáveis esperadas são:

- `NUM_ESQUECE_RELEASE_STORE_FILE`
- `NUM_ESQUECE_RELEASE_STORE_PASSWORD`
- `NUM_ESQUECE_RELEASE_KEY_ALIAS`
- `NUM_ESQUECE_RELEASE_KEY_PASSWORD`

Build:

```bash
./gradlew --no-daemon :app:assembleGoogleRelease
```

A chave privada e as senhas nunca devem ser versionadas.

## Regra de continuidade

Qualquer canal que distribua APKs assinados diretamente pelo projeto deve manter o mesmo certificado entre atualizações. O F-Droid gerencia sua própria cadeia de assinatura conforme o método de publicação adotado.
