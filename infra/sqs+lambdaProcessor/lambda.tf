# IAM role for Lambda execution
data "aws_iam_policy_document" "paylab-dev_lambda_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "lambda_execution_role" {
  name               = "paylab-dev_lambda_execution_role"
  assume_role_policy = data.aws_iam_policy_document.paylab-dev_lambda_assume_role.json
}

resource "aws_iam_role_policy_attachment" "lambda_basic_logs" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_execution_role.name
}

resource "aws_iam_role_policy_attachment" "lambda_sqs" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaSQSQueueExecutionRole"
  role       = aws_iam_role.lambda_execution_role.name
}

resource "aws_lambda_function" "paylab-dev-webhook_processing_lambda" {
  function_name    = "paylab-dev-webhook_processing_lambda"
  filename         = "${path.module}/../../lambdas/target/lambdas-0.0.1-SNAPSHOT.jar"
  role             = aws_iam_role.lambda_execution_role.arn
  memory_size      = 512
  timeout          = 15
  runtime          = "java17"
  handler          = "lambdas.handlers.WebhookMessageProcessor::handleRequest"
  source_code_hash = filebase64sha256("${path.module}/../../lambdas/target/lambdas-0.0.1-SNAPSHOT.jar")
}

resource "aws_lambda_event_source_mapping" "paylab-dev-webhook_processing_lambda_sqs_mapping" {
  function_name = aws_lambda_function.paylab-dev-webhook_processing_lambda.arn
  event_source_arn = aws_sqs_queue.webhook_events.arn
  batch_size = 5

  scaling_config {
    maximum_concurrency = 50
  }

  depends_on = [aws_iam_role_policy_attachment.lambda_sqs]
}