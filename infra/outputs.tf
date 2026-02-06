output "ecr_repo" {
  value = aws_ecr_repository.notification_service.repository_url
}

output "api_url" {
  value = "http://${aws_lb.notification_alb.dns_name}"
}