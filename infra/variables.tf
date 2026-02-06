variable "gmail_password" {
  description = "Senha de App do Gmail (16 digitos)"
  type        = string
  sensitive   = true
}

variable "aws_region" {
  default = "us-east-1"
}