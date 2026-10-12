# Configuração da sincronização com Conta Google

O Num esquece usa a Google Drive API apenas para guardar o arquivo de sincronização na área privada do aplicativo (`appDataFolder`). Ele **não usa a Gmail API**, não lê mensagens e não precisa acessar o restante do Drive.

## Configuração externa

Para usar o flavor `google`:

1. crie ou selecione um projeto no Google Cloud;
2. habilite a **Google Drive API**;
3. configure o Google Auth Platform para o aplicativo;
4. declare apenas o escopo `https://www.googleapis.com/auth/drive.appdata`;
5. crie um cliente OAuth do tipo **Android** para o pacote `com.cybertran.numesquece`;
6. registre o SHA-1 do certificado que assina o APK instalado.

O cliente OAuth Android é público e não exige que um client secret seja embutido no APK ou versionado no repositório.

## Assinaturas

O repositório público não versiona chaves privadas de assinatura. Builds debug usam a assinatura de desenvolvimento local padrão do Android.

Qualquer distribuição assinada diretamente pelo projeto deve usar uma chave de release mantida fora do repositório, fornecida apenas por secrets ou variáveis de ambiente.

Para testar o flavor `google`, registre no Google Cloud o SHA-1 do certificado que assina o APK instalado. Se houver mais de um canal de distribuição, registre o SHA-1 de cada certificado correspondente.

## Funcionamento

A Conta Google é opcional. Quando habilitada:

- o banco local continua sendo a fonte de funcionamento do aplicativo;
- o app solicita somente acesso ao `appDataFolder`;
- alterações locais podem ser sincronizadas quando houver rede;
- o usuário pode forçar **Sincronizar agora**;
- conflitos são resolvidos por lembrete usando o registro mais recentemente alterado;
- exclusões são sincronizadas para impedir que itens removidos reapareçam;
- o token OAuth não é gravado pelo Num esquece;
- **Desconectar Conta Google** desativa novas sincronizações e tenta revogar a autorização.

## Arquivo remoto

O app mantém `num-esquece-sync.json` dentro do `appDataFolder`. Cada lembrete usa um `syncId` próprio para reconciliação entre aparelhos.

## Separação do F-Droid

O flavor `fdroid` não inclui Google Play Services, sincronização Google nem permissões de Internet. Toda configuração descrita neste documento se aplica somente ao flavor `google`.
