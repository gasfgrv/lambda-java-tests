resource "aws_api_gateway_rest_api" "nba_franchises_api" {
  name = "nba-franchises-api"
}

resource "aws_api_gateway_resource" "franchises" {
  rest_api_id = aws_api_gateway_rest_api.nba_franchises_api.id
  parent_id   = aws_api_gateway_rest_api.nba_franchises_api.root_resource_id
  path_part   = "franchises"
}

resource "aws_api_gateway_method" "franchises_post" {
  rest_api_id   = aws_api_gateway_rest_api.nba_franchises_api.id
  resource_id   = aws_api_gateway_resource.franchises.id
  http_method   = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "franchises_post" {
  rest_api_id             = aws_api_gateway_rest_api.nba_franchises_api.id
  resource_id             = aws_api_gateway_resource.franchises.id
  http_method             = aws_api_gateway_method.franchises_post.http_method
  type                    = "AWS_PROXY"
  integration_http_method = "POST"
  uri                     = "arn:aws:apigateway:${data.aws_region.current.id}:lambda:path/2015-03-31/functions/${aws_lambda_function.save_nba_franchises.arn}/invocations"
}

resource "aws_api_gateway_method" "franchises_get" {
  rest_api_id   = aws_api_gateway_rest_api.nba_franchises_api.id
  resource_id   = aws_api_gateway_resource.franchises.id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "franchises_get" {
  rest_api_id             = aws_api_gateway_rest_api.nba_franchises_api.id
  resource_id             = aws_api_gateway_resource.franchises.id
  http_method             = aws_api_gateway_method.franchises_get.http_method
  type                    = "AWS_PROXY"
  integration_http_method = "POST"
  uri                     = "arn:aws:apigateway:${data.aws_region.current.id}:lambda:path/2015-03-31/functions/${aws_lambda_function.get_nba_franchises.arn}/invocations"
}

resource "aws_api_gateway_deployment" "local" {
  rest_api_id = aws_api_gateway_rest_api.nba_franchises_api.id

  depends_on = [
    aws_api_gateway_integration.franchises_post,
    aws_api_gateway_integration.franchises_get
  ]
}

resource "aws_api_gateway_stage" "local" {
  rest_api_id   = aws_api_gateway_rest_api.nba_franchises_api.id
  deployment_id = aws_api_gateway_deployment.local.id
  stage_name    = local.stage_name
}

output "api_id" {
  value = aws_api_gateway_rest_api.nba_franchises_api.id
}

output "api_url" {
  value = "http://localhost:4566/restapis/${aws_api_gateway_rest_api.nba_franchises_api.id}/${local.stage_name}/_user_request_${local.endpoint}"
}
