# Terraform GCP - inside-free-coding-notes

Infraestrutura simples para publicar o site estatico no Google Cloud:

- Cloud Storage bucket (objetos estaticos)
- External HTTP Load Balancer global (IP fixo)
- Cloud CDN via backend bucket
- HTTPS opcional com certificado gerenciado
- DNS opcional (Cloud DNS)

## Pre-requisitos

- Terraform >= 1.6
- `gcloud auth application-default login`
- Projeto GCP com billing ativo

## Uso rapido

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
```

Edite `terraform.tfvars` e ajuste no minimo:

- `project_id`
- `domains` (se for habilitar HTTPS)
- `managed_zone` (somente se usar Cloud DNS)

Aplicar:

```bash
terraform init
terraform plan
terraform apply
```

## Notas de deploy

- Os arquivos sao lidos de `site_dir` (padrao `../../public`).
- Sempre que o conteúdo mudar, rode `terraform apply` novamente para sincronizar os objetos.
- Certificado gerenciado pode levar alguns minutos para ficar `ACTIVE` apos DNS apontado.
