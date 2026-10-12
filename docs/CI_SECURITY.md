# Segurança da CI pública

Este repositório foi preparado para permanecer independente de infraestrutura privada e de credenciais de publicação.

## Validação de contribuições

Pull requests para `main` executam o workflow `Public source validation` exclusivamente em runner hospedado pelo GitHub (`ubuntu-latest`).

A branch `main` recebe snapshots de código já preparados para publicação e não dispara builds automaticamente.

O workflow de pull request:

- usa apenas `permissions: contents: read`;
- não recebe secrets de assinatura ou publicação;
- não usa runners `self-hosted`;
- verifica que o runtime F-Droid não contém dependências Google/Firebase;
- compila os flavors `fdroid` e `google` sem assinatura de produção;
- não publica APKs nem cria releases.

## Credenciais e assinatura

Nenhuma chave privada, keystore de release, senha de assinatura ou credencial de publicação deve ser armazenada neste repositório ou disponibilizada à CI pública.

Nenhuma chave privada de assinatura, inclusive de debug compartilhado, é versionada no repositório público.

## Dependências de Actions

Actions externas usadas pela CI são fixadas por SHA de commit. Atualizações devem ser explícitas e revisadas.

## Proteção da main

Antes de tornar o repositório público, configurar proteção para `main` com:

1. bloqueio de force-push;
2. bloqueio de exclusão da branch;
3. mudanças por pull request para contribuições externas;
4. exigência do check `Public source validation` para pull requests.
