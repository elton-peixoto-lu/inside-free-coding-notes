variable "project_id" {
  description = "ID do projeto GCP."
  type        = string
}

variable "region" {
  description = "Regiao principal para recursos regionais."
  type        = string
  default     = "us-central1"
}

variable "site_name" {
  description = "Nome base do site para naming dos recursos."
  type        = string
  default     = "inside-free-coding-notes"
}

variable "site_dir" {
  description = "Caminho local dos arquivos estaticos a publicar."
  type        = string
  default     = "../../public"
}

variable "bucket_name" {
  description = "Nome global do bucket. Se vazio, usa site_name + random suffix."
  type        = string
  default     = ""
}

variable "enable_https" {
  description = "Ativa HTTPS com certificado gerenciado (requer domains)."
  type        = bool
  default     = false
}

variable "domains" {
  description = "Dominios para certificado gerenciado e DNS A records (ex: [\"labs.insidefreecoding.com.br\"])."
  type        = list(string)
  default     = []
}

variable "managed_zone" {
  description = "Nome da Cloud DNS managed zone. Se vazio, nao cria DNS records."
  type        = string
  default     = ""
}
