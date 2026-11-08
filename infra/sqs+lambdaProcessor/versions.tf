# pinning minimum Terraform version and the AWS provider version range
terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.92"
    }
  }
}

# storing Terraform state remotely in S3 (encrypted, with S3 native lock file to prevent concurrent runs)
terraform {
  backend "s3" {
    bucket       = "384351444433-eu-central-1-terraform-state-bucket"
    key          = "paylab/dev/terraform.tfstate"
    region       = "eu-central-1"
    encrypt      = true
    use_lockfile = true
  }
}