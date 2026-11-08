# AWS region to deploy resources into
variable "region" {
  type    = string
  default = "eu-central-1"
}

# project name, used as a prefix in resource names and in the Project tag
variable "project" {
  type    = string
  default = "paylab"
}

# environment name, used in resource names and in the Env tag
variable "env" {
  type    = string
  default = "dev"
}