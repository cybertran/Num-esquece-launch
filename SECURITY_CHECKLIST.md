# SECURITY_CHECKLIST

**Baseline:** Security Baseline v1.1  
**ASVS:** 5.0.0  
**Alvo padrão:** Level 2  
**Regra:** código produzido ou modificado por IA permanece não confiável até a conclusão das verificações aplicáveis.

> Este checklist deve ser copiado e adaptado ao projeto consumidor. Não marque um item como concluído sem evidência. Quando um item não se aplicar, registre a justificativa.

## 1. Perfil do projeto

- [ ] Stack identificada.
- [ ] Superfícies expostas identificadas.
- [ ] Dados armazenados identificados.
- [ ] Dados sensíveis identificados.
- [ ] Autenticação identificada.
- [ ] Autorização identificada.
- [ ] Integrações externas identificadas.
- [ ] Fluxos administrativos identificados.
- [ ] Upload/download de arquivos identificado.
- [ ] Multitenancy identificado.
- [ ] Alvo ASVS definido.
- [ ] Necessidade de requisitos L3 avaliada.
- [ ] Modelo de ameaça criado ou atualizado.

## 2. Git e processo

- [ ] Alteração realizada fora da branch principal quando Git está disponível.
- [ ] Mudança pequena e revisável.
- [ ] Nenhuma proteção existente foi removida sem justificativa.
- [ ] Arquivos sensíveis não foram adicionados ao Git.
- [ ] Diff revisado antes do merge.
- [ ] Mudanças críticas têm revisão reforçada.
- [ ] Security gate configurado ou ausência justificada.

## 3. Autenticação

- [ ] Autenticação ocorre em camada confiável.
- [ ] Não existe confiança em estado de login controlado apenas pelo cliente.
- [ ] Senhas não são armazenadas em texto puro.
- [ ] Senhas não são armazenadas por criptografia reversível.
- [ ] Password hashing adequado quando gerenciado pela aplicação.
- [ ] Provedor de identidade consolidado é usado quando apropriado.
- [ ] Proteção contra tentativas automatizadas existe.
- [ ] Enumeração de usuários foi avaliada.
- [ ] Recuperação de conta foi revisada.
- [ ] Alterações de credenciais exigem autenticação adequada.
- [ ] Reautenticação é usada para operações sensíveis quando necessário.
- [ ] MFA foi avaliado conforme risco.
- [ ] Contas administrativas possuem proteção reforçada.
- [ ] Passkeys/FIDO2/WebAuthn foram avaliados para ambientes de maior risco.

## 4. Senhas

- [ ] Algoritmo de password hashing é apropriado.
- [ ] Argon2id é preferido quando a aplicação gerencia hashes e a plataforma permite.
- [ ] Salt é adequado.
- [ ] Pepper, quando usado, fica fora do banco de hashes.
- [ ] Hashes rápidos isolados não são usados para senhas.
- [ ] Política permite senhas longas.
- [ ] Senhas conhecidas como comprometidas são bloqueadas quando aplicável.
- [ ] Gerenciadores de senha e colagem não são artificialmente impedidos.
- [ ] Troca periódica sem evidência de comprometimento não é imposta por padrão.

## 5. Recuperação de conta

- [ ] Tokens de recuperação são aleatórios e imprevisíveis.
- [ ] Tokens expiram.
- [ ] Tokens são de uso único.
- [ ] Tokens sensíveis não ficam reutilizáveis em texto puro no banco.
- [ ] Fluxo não depende de perguntas de segurança frágeis.
- [ ] Troca de senha pode invalidar sessões existentes.
- [ ] Mudança de e-mail/método de recuperação gera notificação quando apropriado.
- [ ] Bypass administrativo gera trilha de auditoria.

## 6. Sessões e tokens

- [ ] Identificadores de sessão são criptograficamente aleatórios.
- [ ] Tokens possuem expiração adequada.
- [ ] Refresh tokens possuem rotação/revogação quando aplicável.
- [ ] Logout invalida a sessão quando aplicável.
- [ ] Mudanças críticas podem invalidar sessões.
- [ ] Tokens não aparecem em logs.
- [ ] Tokens não aparecem em URLs.
- [ ] Cookies usam `Secure`, `HttpOnly` e `SameSite` adequadamente.
- [ ] Session fixation foi avaliada.
- [ ] Replay foi avaliado.
- [ ] Revogação foi testada.

## 7. Autorização

- [ ] Autorização ocorre no backend ou camada confiável.
- [ ] Negação por padrão.
- [ ] Menor privilégio.
- [ ] Funções administrativas são separadas.
- [ ] Propriedade dos objetos é verificada.
- [ ] Alteração manual de IDs foi testada.
- [ ] Alteração de role foi testada.
- [ ] `userId`, `ownerId`, `tenantId`, `role`, `isAdmin` e equivalentes vindos do cliente não são confiados.
- [ ] Usuário A não acessa dados do usuário B.
- [ ] Usuário comum não executa ações administrativas.
- [ ] Autorização foi testada por endpoint protegido.

## 8. Multitenancy / RLS

- [ ] Isolamento entre tenants é explícito.
- [ ] Tenant é derivado de contexto confiável.
- [ ] SELECT foi validado.
- [ ] INSERT foi validado.
- [ ] UPDATE foi validado.
- [ ] DELETE foi validado.
- [ ] Tentativa de acessar tenant diferente foi testada.
- [ ] Credencial administrativa que ignora RLS não está no cliente.
- [ ] Funções privilegiadas possuem escopo mínimo.
- [ ] Policies de RLS foram revisadas para casos positivos e negativos.

## 9. Banco de dados

- [ ] Banco não está exposto desnecessariamente à Internet.
- [ ] Credenciais possuem privilégios mínimos.
- [ ] Aplicação não usa conta administrativa global rotineiramente.
- [ ] Queries são parametrizadas.
- [ ] Migrations são versionadas.
- [ ] Migrations sensíveis foram revisadas.
- [ ] Alterações destrutivas possuem estratégia de recuperação.
- [ ] Backups foram avaliados.
- [ ] Logs do banco não expõem segredos.
- [ ] Dados reais não são usados desnecessariamente em desenvolvimento/homologação.

## 10. Dados e privacidade

- [ ] Só são coletados dados necessários.
- [ ] Finalidade de cada dado relevante está documentada.
- [ ] Retenção está definida.
- [ ] Exclusão lógica/física está definida.
- [ ] Dados em trânsito usam canal protegido.
- [ ] Dados sensíveis em repouso recebem proteção adequada.
- [ ] Backups possuem proteção compatível.
- [ ] Logs e analytics minimizam dados pessoais.
- [ ] Testes usam dados fictícios quando possível.

## 11. Entradas

- [ ] Entradas externas são validadas na camada confiável.
- [ ] Tipos são validados.
- [ ] Limites de tamanho são definidos.
- [ ] Quantidade/profundidade são limitadas quando aplicável.
- [ ] Valores permitidos são restringidos quando possível.
- [ ] SQL injection foi avaliada.
- [ ] Command injection foi avaliada.
- [ ] Template injection foi avaliada.
- [ ] Mass assignment foi avaliado.
- [ ] SSRF foi avaliado quando aplicável.
- [ ] Path traversal foi avaliado quando aplicável.
- [ ] XML/XXE foi avaliado quando aplicável.
- [ ] Desserialização insegura foi avaliada quando aplicável.
- [ ] `eval` ou execução dinâmica não é usada sem necessidade excepcional.

## 12. Arquivos

- [ ] Extensão fornecida pelo cliente não é confiada isoladamente.
- [ ] MIME fornecido pelo cliente não é confiado isoladamente.
- [ ] Tipo real é validado quando necessário.
- [ ] Tamanho é limitado.
- [ ] Quantidade é limitada.
- [ ] Nome do arquivo é normalizado ou substituído.
- [ ] Path traversal é impedido.
- [ ] Arquivos enviados não são executados pelo servidor.
- [ ] Tipos ativos/executáveis são bloqueados quando não necessários.
- [ ] Download privado exige autorização.
- [ ] Content-Type e headers de download são adequados.
- [ ] Arquivos temporários são tratados e removidos adequadamente.

## 13. Segredos

- [ ] Nenhum segredo hardcoded.
- [ ] `.env` real não é versionado.
- [ ] Secret scanning foi executado.
- [ ] Histórico do Git foi verificado quando necessário.
- [ ] Segredos têm privilégios mínimos.
- [ ] Segredos de produção são separados de desenvolvimento.
- [ ] Rotação é possível.
- [ ] Revogação é possível.
- [ ] Segredo comprometido foi revogado, não apenas removido do código.
- [ ] Credenciais administrativas nunca são distribuídas ao cliente.

## 14. Criptografia

- [ ] Não existe criptografia própria.
- [ ] Bibliotecas consolidadas são usadas.
- [ ] Algoritmos obsoletos não são usados.
- [ ] Chaves são protegidas.
- [ ] Chaves não são armazenadas junto aos dados de forma que elimine a proteção.
- [ ] Rotação/revogação foi avaliada.
- [ ] Senhas usam hashing, não criptografia reversível.
- [ ] TLS é usado nas comunicações sensíveis.
- [ ] Validação de certificado não foi desabilitada.

## 15. APIs e rede

- [ ] Endpoints estão classificados como públicos ou protegidos.
- [ ] Autenticação é verificada por endpoint protegido.
- [ ] Autorização é verificada por operação.
- [ ] Rate limiting foi avaliado.
- [ ] Abuso de recursos foi avaliado.
- [ ] CORS foi revisado.
- [ ] Respostas não expõem campos desnecessários.
- [ ] Mass assignment é impedido.
- [ ] APIs externas são tratadas como não confiáveis.
- [ ] Timeouts existem.
- [ ] Retries não geram abuso ou duplicidade insegura.
- [ ] Webhooks possuem autenticação/assinatura quando aplicável.
- [ ] SSRF foi avaliado em chamadas controláveis por usuário.

## 16. Aplicações web

- [ ] XSS foi avaliado.
- [ ] CSRF foi avaliado.
- [ ] CORS foi avaliado.
- [ ] Cookies foram avaliados.
- [ ] CSP foi avaliada quando aplicável.
- [ ] Headers de segurança foram avaliados.
- [ ] Clickjacking foi avaliado.
- [ ] Operações de mudança de estado não dependem de GET.
- [ ] Frameworks usam escape/encoding apropriado.
- [ ] Templates não renderizam conteúdo não confiável de forma insegura.

## 17. Aplicações móveis

- [ ] Nenhum segredo privilegiado está incorporado ao APK/app.
- [ ] Armazenamento local sensível usa recursos seguros da plataforma.
- [ ] Keystore/Keychain ou equivalente foi avaliado.
- [ ] Logs não expõem dados sensíveis.
- [ ] Backups automáticos foram avaliados.
- [ ] Clipboard foi avaliado.
- [ ] Screenshots foram avaliados quando necessário.
- [ ] Notificações não expõem dados sensíveis indevidamente.
- [ ] Cache foi avaliado.
- [ ] Permissões seguem menor privilégio.
- [ ] Comunicação de rede é protegida.
- [ ] Certificate pinning só é usado quando há justificativa e estratégia de rotação.
- [ ] Build de produção não contém debug indevido.

## 18. Dependências e supply chain

- [ ] Dependências novas são justificadas.
- [ ] Origem é confiável.
- [ ] Manutenção está ativa ou risco foi avaliado.
- [ ] Vulnerabilidades conhecidas foram verificadas.
- [ ] Lockfile é preservado quando aplicável.
- [ ] Dependências abandonadas foram avaliadas.
- [ ] Atualizações críticas possuem processo.
- [ ] Pacotes desnecessários não foram adicionados.
- [ ] Scripts de instalação/build de terceiros foram avaliados quando relevantes.

## 19. Infraestrutura

- [ ] Desenvolvimento, homologação e produção são separados.
- [ ] Debug não está ativo em produção.
- [ ] Serviços internos não estão publicamente expostos sem necessidade.
- [ ] IAM segue menor privilégio.
- [ ] Security groups/firewall foram revisados.
- [ ] Contas/serviços não usados estão desabilitados.
- [ ] Configurações padrão inseguras foram removidas.
- [ ] Falhas de autorização resultam em negação.
- [ ] Painéis administrativos possuem proteção forte.

## 20. Backups e recuperação

- [ ] Backups possuem controle de acesso.
- [ ] Backups possuem proteção compatível com produção.
- [ ] Restauração foi testada.
- [ ] RPO/RTO foram considerados quando aplicável.
- [ ] Retenção está definida.
- [ ] Descarte está definido.
- [ ] Backups não contêm segredos desnecessários.
- [ ] Perda de chave criptográfica não torna recuperação impossível sem estratégia documentada.

## 21. Logs e auditoria

- [ ] Logins relevantes são auditáveis.
- [ ] Falhas de login relevantes são auditáveis.
- [ ] Alterações de credenciais são auditáveis.
- [ ] Alterações de permissão são auditáveis.
- [ ] Operações administrativas são auditáveis.
- [ ] Eventos críticos de dados são auditáveis quando apropriado.
- [ ] Senhas não são registradas.
- [ ] Tokens não são registrados.
- [ ] Segredos não são registrados.
- [ ] Dados pessoais são minimizados.
- [ ] Logs administrativos são protegidos contra acesso/alteração indevidos.

## 22. Testes negativos obrigatórios

- [ ] Requisição sem autenticação.
- [ ] Token inválido.
- [ ] Token expirado.
- [ ] Token revogado.
- [ ] Usuário errado.
- [ ] Tenant errado.
- [ ] Role errada.
- [ ] ID de outro objeto.
- [ ] Campo administrativo injetado pelo cliente.
- [ ] Entrada malformada.
- [ ] Entrada excessivamente grande.
- [ ] Operação proibida.
- [ ] Repetição/replay quando aplicável.
- [ ] Fluxos concorrentes/race condition quando aplicável.
- [ ] Rate limiting/abuso quando aplicável.

## 23. Validação técnica

- [ ] Testes unitários passaram.
- [ ] Testes de integração passaram.
- [ ] Testes de autorização passaram.
- [ ] Build passou.
- [ ] Lint passou.
- [ ] Type checking passou quando aplicável.
- [ ] Secret scan passou.
- [ ] Dependency scan passou.
- [ ] SAST passou quando disponível.
- [ ] DAST foi avaliado quando aplicável.
- [ ] Scanner de container passou quando aplicável.
- [ ] Scanner de infraestrutura passou quando aplicável.
- [ ] Verificações mobile passaram quando aplicável.
- [ ] Nenhum teste de segurança foi removido apenas para deixar o CI verde.

## 24. Matriz ASVS

Para cada requisito aplicável, registrar:

| Requisito | Aplicável | Estado | Evidência | Teste | Risco residual |
|---|---|---|---|---|---|
| ASVS-x.x.x | Sim/Não | PASS/FAIL/NOT_APPLICABLE/NOT_VERIFIED/ACCEPTED_RISK | link/comando/config | teste | descrição |

## 25. Gate final

### Verificações executadas

Preencher com comandos, ferramentas e resultados.

### Verificações não executadas

Preencher com motivo.

### Problemas encontrados

Preencher.

### Problemas corrigidos

Preencher.

### Riscos residuais

Preencher.

### Resultado

- [ ] `PASS`
- [ ] `PASS_WITH_RESIDUAL_RISK`
- [ ] `BLOCKED`
- [ ] `NOT_FULLY_VERIFIED`

**ASVS target:**  
**Commit:**  
**Data:**  
**Responsável pela decisão de risco aceita, se houver:**  

## 26. Regra de bloqueio

O resultado deve ser `BLOCKED` se houver vulnerabilidade crítica ou alta conhecida sem mitigação, segredo comprometido não revogado, bypass de autenticação/autorização, acesso indevido entre usuários/tenants ou controle de segurança deliberadamente enfraquecido para permitir aprovação.
