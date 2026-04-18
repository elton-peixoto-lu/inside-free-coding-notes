# Arquitetura - Deploy com GitHub OIDC + GCP

```mermaid
flowchart LR
  Dev[Developer push/dispatch] --> GH[GitHub Actions\nrepo: inside-free-coding-notes]
  GH -->|OIDC token| WIF[Workload Identity Federation\nPool: github-pool\nProvider: github-provider]
  WIF -->|STS exchange| SA[Service Account\ngithub-deployer@prod-461714]
  SA --> TF[Terraform Apply]

  TF --> GCS[Cloud Storage Bucket\nsite estatico]
  TF --> LB[Global HTTP(S) Load Balancer]
  TF --> CDN[Cloud CDN\n(backend bucket)]
  TF --> DNS[Cloud DNS A record\nlabs.insidefreecoding.com.br]
  TF --> CERT[Google-managed SSL cert]

  User[Leitor] -->|HTTPS| LB
  LB --> CDN
  CDN --> GCS
```

## Fluxo resumido

- GitHub Actions autentica no GCP sem chave JSON via OIDC.
- O provider WIF valida `assertion.repository == elton-peixoto-lu/inside-free-coding-notes`.
- O workflow assume a service account de deploy.
- Terraform publica estáticos no bucket e atualiza LB/CDN/SSL/DNS.
