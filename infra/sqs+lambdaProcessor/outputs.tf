# exposing URL of the main webhook queue so producers know where to send messages
output "webhook_queue_url" {
  value = aws_sqs_queue.webhook_events.url
}