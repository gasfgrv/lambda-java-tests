locals {
  # lambda
  runtime         = "java21"
  jar_file        = "../target/nba-franchises-lambda.jar"
  docker_host     = "http://host.docker.internal:4566"
  handler_method  = "handleRequest"
  handler_package = "com.gasfgrv.franchises.handler"

  # API
  endpoint   = "/franchises"
  stage_name = "local"
}
