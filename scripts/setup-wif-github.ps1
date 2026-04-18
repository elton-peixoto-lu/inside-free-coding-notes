param(
  [string]$ProjectId = "prod-461714",
  [string]$Repo = "elton-peixoto-lu/inside-free-coding-notes",
  [string]$PoolId = "github-pool",
  [string]$ProviderId = "github-provider",
  [string]$ServiceAccountId = "github-deployer",
  [string]$Location = "global"
)

$ErrorActionPreference = "Stop"

Write-Host "Configuring project $ProjectId"
gcloud config set project $ProjectId | Out-Null

$projectNumber = gcloud projects describe $ProjectId --format="value(projectNumber)"
if (-not $projectNumber) {
  throw "Could not resolve project number for $ProjectId"
}

$saEmail = "$ServiceAccountId@$ProjectId.iam.gserviceaccount.com"
$providerResource = "projects/$projectNumber/locations/$Location/workloadIdentityPools/$PoolId/providers/$ProviderId"
$principalSet = "principalSet://iam.googleapis.com/projects/$projectNumber/locations/$Location/workloadIdentityPools/$PoolId/attribute.repository/$Repo"

Write-Host "Enabling required APIs..."
gcloud services enable `
  iam.googleapis.com `
  iamcredentials.googleapis.com `
  cloudresourcemanager.googleapis.com `
  serviceusage.googleapis.com `
  compute.googleapis.com `
  storage.googleapis.com `
  dns.googleapis.com `
  --project $ProjectId | Out-Null

Write-Host "Creating service account (if not exists)..."
gcloud iam service-accounts create $ServiceAccountId `
  --project $ProjectId `
  --display-name "GitHub deployer for $Repo" 2>$null

Write-Host "Creating workload identity pool (if not exists)..."
gcloud iam workload-identity-pools create $PoolId `
  --project $ProjectId `
  --location $Location `
  --display-name "GitHub Actions Pool" 2>$null

Write-Host "Creating workload identity provider (if not exists)..."
gcloud iam workload-identity-pools providers create-oidc $ProviderId `
  --project $ProjectId `
  --location $Location `
  --workload-identity-pool $PoolId `
  --display-name "GitHub OIDC Provider" `
  --issuer-uri "https://token.actions.githubusercontent.com" `
  --attribute-mapping "google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.ref=assertion.ref,attribute.actor=assertion.actor" `
  --attribute-condition "assertion.repository=='$Repo'" 2>$null

Write-Host "Granting workloadIdentityUser to service account..."
gcloud iam service-accounts add-iam-policy-binding $saEmail `
  --project $ProjectId `
  --role "roles/iam.workloadIdentityUser" `
  --member $principalSet | Out-Null

Write-Host "Granting deploy roles to service account..."
$roles = @(
  "roles/storage.admin",
  "roles/compute.loadBalancerAdmin",
  "roles/dns.admin",
  "roles/serviceusage.serviceUsageAdmin",
  "roles/iam.serviceAccountUser"
)

foreach ($role in $roles) {
  gcloud projects add-iam-policy-binding $ProjectId `
    --member "serviceAccount:$saEmail" `
    --role $role | Out-Null
}

Write-Host "Setting GitHub repository variables..."
gh variable set GCP_PROJECT_ID --repo $Repo --body $ProjectId
gh variable set GCP_PROJECT_NUMBER --repo $Repo --body $projectNumber
gh variable set GCP_WIF_PROVIDER --repo $Repo --body $providerResource
gh variable set GCP_SERVICE_ACCOUNT --repo $Repo --body $saEmail

Write-Host ""
Write-Host "Done."
Write-Host "Provider: $providerResource"
Write-Host "Service Account: $saEmail"
