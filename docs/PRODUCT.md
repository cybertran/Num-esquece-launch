# Direção de produto — Num esquece

## Promessa

> O lembrete só termina quando você resolve o assunto.

Num esquece deve ser menor e mais focado que um gerenciador de tarefas. O objetivo não é organizar projetos; é evitar que algo importante desapareça depois que uma notificação foi dispensada.

## Linguagem

A interface deve falar de forma natural. Evitar termos como “prioridade”, “workflow” ou códigos de urgência quando uma frase simples resolve.

Os três níveis de comportamento são:

- **Normal** — avisa uma vez e continua aparecendo como pendente no app.
- **Insistente** — se a pessoa dispensar a notificação, o app volta a avisar depois.
- **Num deixa eu esquecer** — enquanto o lembrete continuar pendente, volta a alertar periodicamente até a pessoa marcar **Fiz** ou **Adiar**.

## Estrutura principal

A navegação base tem três destinos:

- **Agora** — pendências e o próximo lembrete;
- **Lembretes** — todos os lembretes ativos;
- **Histórico** — itens concluídos.

Configurações ficam fora da barra inferior. A ação principal é sempre **+ Lembrar**.

## Princípios

Criação rápida, pendência explícita, linguagem sem culpa, confiabilidade visível e privacidade verificável.

O aplicativo é livre, gratuito, sem anúncios e **offline por construção**. Não possui integração de rede nem sincronização remota. A distribuição principal planejada é pelo F-Droid.

O campo de prazo significa **resolver até**. Adiar um alerta muda apenas o próximo aviso e não altera o prazo definido.

Doações são voluntárias, não desbloqueiam recursos e não alteram o funcionamento do aplicativo.

## Próximas etapas

Validar o comportamento em aparelhos reais; testar acessibilidade, fonte ampliada e TalkBack; avaliar exportação e importação local sob ação explícita do usuário; preparar screenshots e metadados do F-Droid; publicar a primeira versão estável e abrir a solicitação de inclusão.
