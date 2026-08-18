resource "aws_dynamodb_table" "franchise_tb" {
  name         = "tb-franchise"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
}
