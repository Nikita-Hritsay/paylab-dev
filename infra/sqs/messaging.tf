resource "aws_sqs_queue" "webhook_events_dlq" {
  name                      = "${var.project}-${var.env}-webhook-events-dlq"
  message_retention_seconds = 1209600

  visibility_timeout_seconds = 30
}

resource "aws_sqs_queue" "webhook_events" {
  name                       = "${var.project}-${var.env}-webhook-events"
  message_retention_seconds  = 345600
  visibility_timeout_seconds = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.webhook_events_dlq.arn
    maxReceiveCount     = 3
  })
}