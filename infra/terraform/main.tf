resource "google_project_service" "required" {
  for_each = toset([
    "compute.googleapis.com",
    "storage.googleapis.com",
    "run.googleapis.com",
    "artifactregistry.googleapis.com"
  ])

  project            = var.project_id
  service            = each.key
  disable_on_destroy = false
}

resource "random_id" "suffix" {
  byte_length = 3
}

locals {
  effective_bucket_name = var.bucket_name != "" ? var.bucket_name : "${var.site_name}-${random_id.suffix.hex}"

  content_type_map = {
    ".html" = "text/html; charset=utf-8"
    ".css"  = "text/css; charset=utf-8"
    ".js"   = "application/javascript; charset=utf-8"
    ".mjs"  = "application/javascript; charset=utf-8"
    ".json" = "application/json; charset=utf-8"
    ".map"  = "application/json; charset=utf-8"
    ".svg"  = "image/svg+xml"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".jpeg" = "image/jpeg"
    ".webp" = "image/webp"
    ".ico"  = "image/x-icon"
    ".txt"  = "text/plain; charset=utf-8"
    ".xml"  = "application/xml; charset=utf-8"
    ".pdf"  = "application/pdf"
    ".edn"  = "application/edn; charset=utf-8"
  }

  files = fileset(var.site_dir, "**")
}

resource "google_storage_bucket" "site" {
  name                        = local.effective_bucket_name
  location                    = "US"
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  force_destroy               = true

  website {
    main_page_suffix = "index.html"
    not_found_page   = "index.html"
  }

  depends_on = [google_project_service.required]
}

resource "google_storage_bucket_object" "site_files" {
  for_each = { for file in local.files : file => file if !endswith(file, "/") }

  bucket       = google_storage_bucket.site.name
  name         = each.value
  source       = "${var.site_dir}/${each.value}"
  content_type = can(regex("\\.[^.]+$", each.value)) ? lookup(local.content_type_map, regex("\\.[^.]+$", each.value), "application/octet-stream") : "application/octet-stream"
  cache_control = (
    can(regex("\\.html$", each.value)) || can(regex("\\.edn$", each.value))
    ) ? "no-store, max-age=0" : (
    can(regex("\\.(js|css)$", each.value))
  ) ? "public, max-age=300" : "public, max-age=86400"
}

resource "google_service_account" "run_runtime" {
  account_id   = "ifcnotes-run-runtime"
  display_name = "Cloud Run runtime for ${var.site_name}"
}

resource "google_storage_bucket_iam_member" "run_can_read_site_bucket" {
  bucket = google_storage_bucket.site.name
  role   = "roles/storage.objectViewer"
  member = "serviceAccount:${google_service_account.run_runtime.email}"
}

resource "google_cloud_run_v2_service" "site" {
  name     = "${var.site_name}-run"
  location = var.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.run_runtime.email
    containers {
      image = var.cloud_run_image
      env {
        name  = "SITE_BUCKET"
        value = google_storage_bucket.site.name
      }
    }
  }

  depends_on = [google_project_service.required]
}

resource "google_cloud_run_v2_service_iam_member" "public_invoker" {
  name     = google_cloud_run_v2_service.site.name
  location = google_cloud_run_v2_service.site.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_compute_region_network_endpoint_group" "site" {
  name                  = "${var.site_name}-neg"
  region                = var.region
  network_endpoint_type = "SERVERLESS"
  cloud_run {
    service = google_cloud_run_v2_service.site.name
  }
}

resource "google_compute_backend_service" "site" {
  name                  = "${var.site_name}-backend"
  protocol              = "HTTP"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  enable_cdn            = true

  backend {
    group = google_compute_region_network_endpoint_group.site.id
  }
}

resource "google_compute_global_address" "site" {
  name = "${var.site_name}-ip"
}

resource "google_compute_url_map" "site" {
  name            = "${var.site_name}-url-map"
  default_service = google_compute_backend_service.site.id
}

resource "google_compute_target_http_proxy" "site" {
  name    = "${var.site_name}-http-proxy"
  url_map = google_compute_url_map.site.id
}

resource "google_compute_global_forwarding_rule" "http" {
  name       = "${var.site_name}-http"
  target     = google_compute_target_http_proxy.site.id
  ip_address = google_compute_global_address.site.address
  port_range = "80"
}

resource "google_compute_managed_ssl_certificate" "site" {
  count = var.enable_https ? 1 : 0

  name = "${var.site_name}-cert"
  managed {
    domains = var.domains
  }
}

resource "google_compute_target_https_proxy" "site" {
  count = var.enable_https ? 1 : 0

  name             = "${var.site_name}-https-proxy"
  url_map          = google_compute_url_map.site.id
  ssl_certificates = [google_compute_managed_ssl_certificate.site[0].id]
}

resource "google_compute_global_forwarding_rule" "https" {
  count = var.enable_https ? 1 : 0

  name       = "${var.site_name}-https"
  target     = google_compute_target_https_proxy.site[0].id
  ip_address = google_compute_global_address.site.address
  port_range = "443"
}

resource "google_dns_record_set" "site_a_records" {
  for_each = var.managed_zone != "" ? toset(var.domains) : []

  name         = "${each.value}."
  managed_zone = var.managed_zone
  type         = "A"
  ttl          = 300
  rrdatas      = [google_compute_global_address.site.address]
}
