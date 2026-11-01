provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project = var.project
      Env     = var.env
      Stage   = "1"
    }
  }
}

resource "aws_sqs_queue" "webhook_events_dlq" {
  name                      = "${var.project}-${var.env}-webhook-events-dlq"
  message_retention_seconds = 1209600
}

resource "aws_sqs_queue" "webhook_events" {
  name                      = "${var.project}-${var.env}-webhook-events"
  message_retention_seconds = 345600

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.webhook_events_dlq.arn
    maxReceiveCount     = 3
  })
}

terraform {
  backend "s3" {
    bucket = "384351444433-eu-central-1-terraform-state-bucket"
    key = "paylab/dev/terraform.tfstate"
    region = "eu-central-1"
    encrypt = true
    use_lockfile = true
  }
}