resource "null_resource" "build_lambda" {
  triggers = {
    source_hash = sha1(join("", [
      for f in fileset("${path.module}/../src", "**/*.java") :
      filesha1("${path.module}/../src/${f}")
    ]))
    pom_hash = filemd5("${path.module}/../pom.xml")
  }

  provisioner "local-exec" {
    command = "cd ${path.module}/.. && mvn clean package"
  }
}

resource "aws_lambda_function" "save_nba_franchises" {
  function_name    = "SaveNbaFranchisesLambda"
  runtime          = "java21"
  handler          = "com.gasfgrv.franchises.handler.SaveNbaFranchisesLambda::handleRequest"
  role             = aws_iam_role.lambda_role.arn
  filename         = "../target/nba-franchises-lambda.jar"
  source_code_hash = filebase64sha256("${dirname(path.module)}/../target/nba-franchises-lambda.jar")

  environment {
    variables = {
      TABLE_NAME       = "tb-franchise"
      AWS_ENDPOINT_URL = "http://host.docker.internal:4566"
      AWS_REGION       = "us-east-1"
    }
  }
}

resource "aws_lambda_function" "get_nba_franchises" {
  function_name    = "GetNbaFranchiseLambda"
  runtime          = "java21"
  handler          = "com.gasfgrv.franchises.handler.GetNbaFranchiseLambda::handleRequest"
  role             = aws_iam_role.lambda_role.arn
  filename         = "../target/nba-franchises-lambda.jar"
  source_code_hash = filebase64sha256("${dirname(path.root)}/../target/nba-franchises-lambda.jar")
  environment {
    variables = {
      TABLE_NAME       = "tb-franchise"
      AWS_ENDPOINT_URL = "http://host.docker.internal:4566"
      AWS_REGION       = "us-east-1"
    }
  }
}

resource "aws_lambda_permission" "api_gateway_save_invoke" {
  statement_id  = "apigateway-save-invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.save_nba_franchises.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.nba_franchises_api.execution_arn}/*/POST/franchises"
}

resource "aws_lambda_permission" "api_gateway_get_invoke" {
  statement_id  = "apigateway-get-invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_nba_franchises.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.nba_franchises_api.execution_arn}/*/GET/franchises"
}
