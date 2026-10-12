# Google flavor — configuração externa

Este documento descreve somente a configuração necessária para o flavor opcional `google`. O APK destinado ao F-Droid não usa esta integração.

## Escopo

A sincronização utiliza exclusivamente:

`https://www.googleapis.com/auth/drive.appdata`

O Num esquece não usa a Gmail API e não solicita acesso geral aos arquivos do Google Drive.

## Google Cloud

1. habilite a **Google Drive API**;
2. configure o Google Auth Platform;
3. use **Num esquece** como identidade do aplicativo;
4. adicione apenas o escopo `drive.appdata`;
5. crie um cliente OAuth **Android** para `com.cybertran.numesquece`;
6. associe ao cliente o SHA-1 do certificado que assina a distribuição correspondente.

Não versione arquivos de credenciais, chaves privadas, secrets ou keystores de release.

## Privacidade

A Conta Google é opcional. A política de privacidade deve deixar claro que os dados sincronizados ficam na área privada do aplicativo no Google Drive e que o flavor F-Droid permanece totalmente local.

## Validação antes de distribuir

Em um aparelho real, confirme conexão, criação e sincronização de lembretes, conclusão, exclusão, troca de aparelho e desconexão da Conta Google antes de publicar qualquer APK com esse flavor.
