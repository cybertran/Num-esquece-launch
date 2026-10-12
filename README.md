# Num esquece

**Num esquece** é um aplicativo Android livre e gratuito de lembretes para uma ideia simples: o lembrete não termina quando a notificação aparece; ele termina quando a pessoa resolve o assunto.

O nome vem da forma coloquial como “não esquece” soa quando é dito rapidamente em português.

## Proposta

O app não quer ser agenda, gerenciador de projetos ou suíte de produtividade. Ele existe para as coisas que a pessoa realmente não pode esquecer.

O MVP inclui criação e edição de lembretes, tela **Agora**, intensidades **Normal**, **Insistente** e **Num deixa eu esquecer**, ações **Fiz** e **Adiar 10 min**, recorrência, histórico, teste de notificação, funcionamento local com Room e sons próprios.

Não há anúncios, assinatura ou conta obrigatória.

## Software livre

Este repositório contém o código-fonte preparado para publicação das versões do Num esquece. A branch `main` representa o estado público atual do projeto e as tags identificam versões distribuídas.

O código do Num esquece é distribuído sob a licença **GNU GPL v3 ou posterior (GPL-3.0-or-later)**.

A distribuição principal planejada é pelo **F-Droid**. O flavor `fdroid` é totalmente local, não inclui Google Play Services e não declara permissões de Internet ou estado da rede.

Existe também um flavor `google` separado para experimentação com sincronização opcional via Google Drive. Esse flavor não faz parte do APK destinado ao F-Droid.

## Doações

O aplicativo é gratuito e todas as funcionalidades do flavor F-Droid permanecem disponíveis sem pagamento.

Uma opção de doação voluntária pode ser exibida em **Configurações > Apoiar o Num esquece**. O botão só aparece quando `BuildConfig.DONATION_URL` recebe uma URL válida. Até uma página de apoio ser definida, a opção permanece oculta.

Doações não desbloqueiam recursos e não alteram o comportamento do aplicativo.

## Idiomas

O aplicativo inclui interface e notificações em:

- Português do Brasil (`pt-BR`);
- Inglês (`en`, idioma-base de fallback);
- Espanhol (`es`);
- Francês (`fr`).

No Android 13 ou superior, os idiomas declarados também podem aparecer no seletor de idioma por aplicativo do sistema. Os metadados de apresentação para o F-Droid acompanham os mesmos quatro idiomas.

## Tecnologia

- Kotlin
- Jetpack Compose + Material 3
- Room
- AlarmManager
- minSdk 26 / targetSdk 36

O flavor `google` adiciona WorkManager e Google Identity Services apenas para a sincronização opcional.

## Builds

Build F-Droid:

```bash
./gradlew --no-daemon :app:assembleFdroidRelease
```

Build de desenvolvimento F-Droid:

```bash
./gradlew --no-daemon :app:assembleFdroidDebug
```

Build com integração Google:

```bash
./gradlew --no-daemon :app:assembleGoogleDebug
```

A versão atual de preparação é **1.0.0-rc3** (`versionCode 10002`).

## Privacidade

O flavor F-Droid funciona localmente e não possui permissão de Internet. Os lembretes permanecem no dispositivo, salvo pelos mecanismos de backup do próprio Android quando habilitados pelo usuário/sistema.

O flavor Google é separado e, quando usado, pode sincronizar voluntariamente os dados pela área privada `appDataFolder` do Google Drive.

Veja também [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md), [docs/FDROID.md](docs/FDROID.md) e [docs/ASSETS.md](docs/ASSETS.md).

## Contribuições

Issues, correções e melhorias são bem-vindas. Pull requests são validados exclusivamente em runners hospedados pelo GitHub e não recebem credenciais de assinatura ou outros secrets de publicação.

Antes da primeira submissão oficial ao F-Droid, o projeto deverá publicar uma tag estável e os metadados correspondentes.
