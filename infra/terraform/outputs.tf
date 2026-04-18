output "bucket_name" {
  value       = google_storage_bucket.site.name
  description = "Bucket onde os arquivos estaticos sao publicados."
}

output "load_balancer_ip" {
  value       = google_compute_global_address.site.address
  description = "IP global do load balancer."
}

output "http_url" {
  value       = "http://${google_compute_global_address.site.address}"
  description = "URL HTTP por IP."
}

output "https_ready" {
  value       = var.enable_https
  description = "Indica se o stack foi criado com HTTPS habilitado."
}
