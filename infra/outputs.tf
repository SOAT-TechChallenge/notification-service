output "ecr_repo" {
  value = aws_ecr_repository.notification_service.repository_url
}

output "api_url" {
  value = "http://${aws_lb.notification_alb.dns_name}"
}

output "ssm_parameter_name" {
  value       = aws_ssm_parameter.notification_alb_dns.name
  description = "Nome do parâmetro SSM que armazena o DNS do ALB"
}