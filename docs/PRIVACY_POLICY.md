# Política de Privacidade — Num esquece

Última atualização: 12/10/2026

O Num esquece foi projetado para funcionar principalmente de forma local no dispositivo Android.

## Flavor F-Droid

A versão destinada ao F-Droid funciona sem Conta Google, não inclui Google Play Services e não declara as permissões Android `INTERNET` ou `ACCESS_NETWORK_STATE`.

Títulos, observações, horários, histórico, intensidade e demais dados dos lembretes são armazenados localmente pelo aplicativo.

O Num esquece não exibe anúncios, não inclui analytics e não vende dados pessoais.

## Flavor Google

O projeto mantém separadamente um flavor `google`, que não faz parte do APK planejado para o repositório oficial do F-Droid.

Quando o usuário ativa voluntariamente a sincronização nesse flavor, o aplicativo solicita o escopo:

`https://www.googleapis.com/auth/drive.appdata`

Esse acesso é destinado à área privada `appDataFolder` do Google Drive. O aplicativo não usa a Gmail API e não solicita acesso geral aos arquivos pessoais do Google Drive.

## Permissões Android

O flavor F-Droid pode usar permissões necessárias às funções locais, como notificações, alarmes precisos e recebimento da inicialização do aparelho para reagendar lembretes.

As permissões de Internet e estado da rede existem somente no manifesto do flavor `google`.

## Doações

Se uma página de apoio for configurada, o aplicativo pode exibir um botão de doação voluntária que abre essa página no navegador ou aplicativo externo escolhido pelo usuário. A doação não é necessária para usar o aplicativo e não desbloqueia funcionalidades.

## Servidor próprio

O núcleo do Num esquece não depende de servidor próprio para armazenar lembretes.

## Alterações

Esta política poderá ser atualizada quando novas funções forem adicionadas ou quando houver mudanças relevantes no tratamento de dados.

## Contato

O canal oficial de suporte será publicado antes da primeira versão estável.
