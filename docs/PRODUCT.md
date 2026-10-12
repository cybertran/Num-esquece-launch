# Direção de produto — Num esquece

## Promessa

> O lembrete só termina quando você resolve o assunto.

Num esquece deve ser menor e mais focado que um gerenciador de tarefas. O objetivo não é organizar projetos; é evitar que algo importante desapareça depois que uma notificação foi dispensada.

## Linguagem

A interface deve falar português natural. Evitar termos como “prioridade”, “workflow” ou códigos de urgência quando uma frase simples resolve.

Os três níveis de comportamento são:

- **Normal** — avisa uma vez e continua aparecendo como pendente no app.
- **Insistente** — se a pessoa dispensar a notificação, o app volta a avisar depois.
- **Num deixa eu esquecer** — comportamento mais insistente, explicitamente escolhido pelo usuário.

## Estrutura principal

A navegação base tem três destinos:

- **Agora** — pendências e o próximo lembrete;
- **Lembretes** — todos os lembretes ativos;
- **Histórico** — itens concluídos.

Configurações ficam fora da barra inferior. A ação principal é sempre **+ Lembrar**.

## Princípios

Criação rápida, pendência explícita, linguagem sem culpa, confiabilidade visível e privacidade simples.

O aplicativo é livre, gratuito, sem anúncios e local-first. A distribuição principal planejada é pelo F-Droid.

O flavor `fdroid` funciona sem conta e sem rede. O flavor `google`, mantido separadamente, pode oferecer sincronização opcional pelo Google Drive fora do APK destinado ao F-Droid.

Doações são voluntárias, não desbloqueiam recursos e não alteram o funcionamento do aplicativo.

## Próximas etapas

Validar o comportamento em aparelhos reais; revisar licenças de sons e artes; testar acessibilidade, fonte ampliada e TalkBack; aprimorar backup/exportação local; preparar screenshots e metadados do F-Droid; publicar a primeira versão estável e abrir a solicitação de inclusão.
