# configuring AWS provider: region comes from var.region
provider "aws" {
  region = var.region

  # tags applied automatically to every taggable resource created by this config
  default_tags {
    tags = {
      Project = var.project
      Env     = var.env
      Stage   = "1"
    }
  }
}
