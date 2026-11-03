# IAM role for Lambda execution
data "aws_iam_policy_document" "assume_role" {
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
  name               = "lambda_execution_role"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

# Package the Lambda function code
data "archive_file" "lambda_code" {
  type        = "zip"
  source_file = "${path.module}/lambda/index.js"
  output_path = "${path.module}/lambda/function.zip"
}

# Lambda function
resource "aws_lambda_function" "lambda_processor" {
  filename      = data.archive_file.lambda_code.output_path
  function_name = "example_lambda_function"
  role          = aws_iam_role.lambda_execution_role.arn
  handler       = "index.handler"
  code_sha256   = data.archive_file.lambda_code.output_base64sha256

  runtime = "nodejs24.x"
}

# Lambda source mapping
resource "aws_lambda_event_source_mapping" "example" {
  event_source_arn = aws_sqs_queue.webhook_events.arn
  function_name    = aws_lambda_function.lambda_processor.arn
  batch_size       = 10

  scaling_config {
    maximum_concurrency = 50
  }
}