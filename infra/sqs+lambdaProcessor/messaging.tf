# creating dead-letter queue that receives webhook events which failed processing
resource "aws_sqs_queue" "webhook_events_dlq" {
  name                      = "${var.project}-${var.env}-webhook-events-dlq"
  message_retention_seconds = 1209600 # keeping failed messages for 14 days (SQS maximum)

  visibility_timeout_seconds = 30
}

# creating main queue that the webhook events are sent to and Lambda polls from
resource "aws_sqs_queue" "webhook_events" {
  name                       = "${var.project}-${var.env}-webhook-events"
  message_retention_seconds  = 345600 # keeping unprocessed messages for 4 days
  visibility_timeout_seconds = 30     # time a message stays hidden from other consumers while being processed

  # moving a message to the DLQ after it was received (and not deleted) 3 times
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.webhook_events_dlq.arn
    maxReceiveCount     = 3
  })
}