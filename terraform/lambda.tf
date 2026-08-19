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
  runtime          = local.runtime
  handler          = "${local.handler_package}.SaveNbaFranchisesLambda::${local.handler_method}"
  role             = aws_iam_role.lambda_role.arn
  filename         = local.jar_file
  source_code_hash = filebase64sha256("${dirname(path.module)}/${local.jar_file}")

  environment {
    variables = {
      TABLE_NAME       = aws_dynamodb_table.franchise_tb.name
      AWS_ENDPOINT_URL = local.docker_host
      AWS_REGION       = data.aws_region.current.id
    }
  }
}

resource "aws_lambda_function" "get_nba_franchises" {
  function_name    = "GetNbaFranchiseLambda"
  runtime          = local.runtime
  handler          = "${local.handler_package}.GetNbaFranchiseLambda::${local.handler_method}"
  role             = aws_iam_role.lambda_role.arn
  filename         = local.jar_file
  source_code_hash = filebase64sha256("${dirname(path.root)}/${local.jar_file}")
  environment {
    variables = {
      TABLE_NAME       = aws_dynamodb_table.franchise_tb.name
      AWS_ENDPOINT_URL = local.docker_host
      AWS_REGION       = data.aws_region.current.id
    }
  }
}

resource "aws_lambda_permission" "api_gateway_save_invoke" {
  statement_id  = "apigateway-save-invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.save_nba_franchises.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.nba_franchises_api.execution_arn}/*/POST${local.endpoint}"
}

resource "aws_lambda_permission" "api_gateway_get_invoke" {
  statement_id  = "apigateway-get-invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_nba_franchises.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.nba_franchises_api.execution_arn}/*/GET${local.endpoint}"
}
