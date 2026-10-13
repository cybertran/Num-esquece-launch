# Assinatura e builds de distribuição

O Num esquece mantém um único flavor de distribuição: `fdroid`.

## F-Droid

O flavor `fdroid` é o build destinado ao repositório oficial do F-Droid. Ele não depende da chave privada externa do projeto para ser compilado pelos servidores do F-Droid.

Build local:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

Sem credenciais de assinatura, o Gradle produz um APK release não assinado adequado para validação do build.

## Assinatura externa opcional

Se o projeto distribuir diretamente um APK fora do F-Droid, a assinatura externa usa uma chave de produção mantida fora do repositório. As variáveis esperadas são:

- `NUM_ESQUECE_RELEASE_STORE_FILE`
- `NUM_ESQUECE_RELEASE_STORE_PASSWORD`
- `NUM_ESQUECE_RELEASE_KEY_ALIAS`
- `NUM_ESQUECE_RELEASE_KEY_PASSWORD`

Build:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

A chave privada e as senhas nunca devem ser versionadas.

## Regra de continuidade

Qualquer canal que distribua APKs assinados diretamente pelo projeto deve manter o mesmo certificado entre atualizações. O F-Droid gerencia sua própria cadeia de assinatura conforme o método de publicação adotado.
