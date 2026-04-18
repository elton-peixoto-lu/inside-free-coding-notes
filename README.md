# inside-free-coding-notes

Site editorial tecnico em ClojureScript com `re-frame`, `Reagent` e `shadow-cljs`, focado em leitura longa, semantica HTML e acessibilidade.

## Objetivo

Publicar textos tecnicos longos, benchmarks, analises e papers curtos com:

- baixo ruido visual
- manutencao simples
- boa experiencia em teclado e leitor de tela

## Stack

- ClojureScript
- re-frame
- Reagent
- shadow-cljs

Sem bibliotecas extras de roteamento ou UI para manter simplicidade.

## Estrutura

```text
inside-free-coding-notes/
  public/
    index.html
    styles/main.css
  src/
    app/
      main.cljs
      router.cljs
    state/
      db.cljs
      events.cljs
      subs.cljs
    ui/
      components/
        layout.cljs
        controls.cljs
        article_card.cljs
      pages/
        home.cljs
        about.cljs
        articles.cljs
        article.cljs
        experiments.cljs
        accessibility.cljs
        contact.cljs
    content/
      articles.cljs
    a11y/
      preferences.cljs
    styles/
      README.md
```

## Sistema de conteudo

As publicacoes agora vivem em **EDN real**:

- `public/content/index.edn` lista os arquivos de publicacao
- `public/content/articles/*.edn` contem cada artigo

No boot, o app carrega esses arquivos e popula o `app-db` (`:content :items`).

Cada arquivo EDN usa metadados:

- titulo
- resumo
- data
- tags
- categoria
- tempo de leitura

Cada artigo inclui:

- indice
- metodologia
- bloco de codigo
- conclusao
- como reproduzir
- referencias
- descricao textual de diagrama

O projeto ja inclui 3 exemplos:

1. benchmark-cljs-data-structures-2026
2. paper-short-semantic-html-reading
3. analise-custo-renderizacao-reagent

### Como publicar novo artigo em EDN

1. Criar um novo arquivo em `public/content/articles/<slug>.edn`
2. Seguir a mesma estrutura dos exemplos (titulo, resumo, data, tags etc.)
3. Adicionar o nome do arquivo em `public/content/index.edn`
4. Salvar; com `shadow-cljs watch` o conteúdo aparece sem alterar código de UI

## Rotas

Hash routing simples:

- `#/` Home
- `#/sobre`
- `#/artigos`
- `#/artigos/:slug`
- `#/experimentos`
- `#/acessibilidade`
- `#/contato`

## Acessibilidade implementada

- Skip link para `#conteudo-principal`
- Navegacao por teclado com `focus-visible`
- HTML semantico (`header`, `nav`, `main`, `article`, `section`, `footer`, `address`)
- Tema claro/escuro
- Alto contraste
- Ajuste de tamanho de fonte
- `prefers-reduced-motion` + sincronizacao com estado da app
- Estrutura de headings consistente por pagina
- Descricao textual para diagramas em cada publicacao

Nota: evitamos ARIA desnecessario; so usamos quando agrega contexto real.

## Rodando localmente

```bash
npm install
npx shadow-cljs watch app
```

Abra `http://localhost:8080`.

Build de producao:

```bash
npx shadow-cljs release app
```

## Deploy e dominios

Estrategia desejada:

- `insidefreecoding.com.br` -> Vercel (site atual, trilha stable/prod)
- `labs.insidefreecoding.com.br` -> deploy Clojure (trilha experimental/lab)

Este projeto foi preparado para ser o novo repositorio da trilha `lab`, mantendo o `prod` separado.

## Infraestrutura GCP (Terraform)

Infra pronta em:

- `infra/terraform`

Fluxo:

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
terraform apply
```

## GitHub -> GCP sem chave (WIF)

Para configurar Workload Identity Federation para este repo:

1. Autentique no GCP com uma conta que tenha permissao no projeto `prod-461714`:

```bash
gcloud auth login pluizelton@gmail.com
gcloud auth application-default login
```

2. Rode o setup automatizado:

```bash
powershell -ExecutionPolicy Bypass -File scripts/setup-wif-github.ps1
```

Esse script cria pool/provider, service account, bindings IAM e variaveis do GitHub repo.

Workflow pronto:

- `.github/workflows/terraform-cicd.yml`
- `.github/workflows/deploy-site.yml` (deploy de codigo/conteudo sem Terraform, com invalidação de CDN)

Variaveis obrigatorias no repo GitHub (`Settings > Secrets and variables > Actions > Variables`):

- `GCP_PROJECT_ID`
- `GCP_WIF_PROVIDER`
- `GCP_SERVICE_ACCOUNT`
- `TF_STATE_BUCKET` (bucket GCS do estado remoto do Terraform)

Exemplo para criar bucket de state:

```bash
gcloud storage buckets create gs://prod-461714-tfstate-notes --project=prod-461714 --location=US --uniform-bucket-level-access
```

Desenho da arquitetura:

- `docs/architecture.md`
