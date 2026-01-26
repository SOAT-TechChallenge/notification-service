terraform {
  required_version = ">= 1.0"
  backend "s3" {
    bucket = "challenge-hackathon"
    key    = "notification-service/terraform.tfstate"
    region = "us-east-1"
  }
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

# --- Variáveis (Onde recebemos a senha externamente) ---
variable "gmail_password" {
  description = "Senha de App do Gmail (16 digitos)"
  type        = string
  sensitive   = true
}

# --- Data Sources ---

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "all" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# --- SSM Parameter (Cria o segredo com a senha) ---
resource "aws_ssm_parameter" "email_pass" {
  name        = "/notification-service/EMAIL_PASS"
  description = "Senha de App do Gmail"
  type        = "SecureString"
  value       = var.gmail_password
  overwrite   = true
}

# --- ECR ---
resource "aws_ecr_repository" "notification_service" {
  name                 = "notification-service-repo"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

# --- Security Groups ---

resource "aws_security_group" "alb_sg" {
  name        = "notification-alb-sg"
  description = "Security group for Notification ALB"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTP from world"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "notification-alb-sg"
  }
}

resource "aws_security_group" "ecs_sg" {
  name        = "notification-ecs-sg"
  description = "Security group for ECS tasks"
  vpc_id      = data.aws_vpc.default.id

  # Entrada: Só aceita vindo do ALB
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  # Saída: Liberada para a Internet (SMTP Gmail)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "notification-ecs-sg"
  }
}

# --- Load Balancer (ALB) ---

resource "aws_lb" "notification_alb" {
  name                       = "notification-service-alb"
  internal                   = false
  load_balancer_type         = "application"
  security_groups            = [aws_security_group.alb_sg.id]
  subnets                    = slice(data.aws_subnets.all.ids, 0, min(2, length(data.aws_subnets.all.ids)))
  enable_deletion_protection = false

  tags = {
    Name = "notification-service-alb"
  }
}

resource "aws_lb_target_group" "notification_tg" {
  name        = "notification-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    interval            = 60
    timeout             = 30
    healthy_threshold   = 2
    unhealthy_threshold = 5
    matcher             = "200"
  }

  tags = {
    Name = "notification-tg"
  }
}

resource "aws_lb_listener" "notification_listener" {
  load_balancer_arn = aws_lb.notification_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Acesso Negado (Token invalido ou ausente)."
      status_code  = "403"
    }
  }
}

# Regra de Segurança do Gateway (Token Header)
resource "aws_lb_listener_rule" "allow_gateway" {
  listener_arn = aws_lb_listener.notification_listener.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.notification_tg.arn
  }

  condition {
    http_header {
      http_header_name = "x-apigateway-token"
      values           = ["tech-challenge-hackathon"]
    }
  }
}

# --- ECS Cluster & Task ---

resource "aws_ecs_cluster" "notification_cluster" {
  name = "notification-cluster"

  setting {
    name  = "containerInsights"
    value = "disabled"
  }

  tags = {
    Name = "notification-cluster"
  }
}

resource "aws_ecs_task_definition" "notification_task" {
  family                   = "notification-service-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = data.aws_iam_role.lab_role.arn

  container_definitions = jsonencode([{
    name  = "notification-service"
    image = "${aws_ecr_repository.notification_service.repository_url}:latest"
    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
      protocol      = "tcp"
    }]
    essential = true

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = "/ecs/notification-service"
        awslogs-region        = "us-east-1"
        awslogs-stream-prefix = "ecs"
        awslogs-create-group  = "true"
      }
    }

    environment = [
      {
        name  = "SERVER_PORT"
        value = "8080"
      },
      {
        name  = "EMAIL_USER"
        value = "leyner09henrique@gmail.com"
      },
      {
        name  = "EMAIL_FROM"
        value = "TechChallenge <leyner09henrique@gmail.com>"
      }
    ]

    secrets = [
      {
        name      = "EMAIL_PASS"
        valueFrom = aws_ssm_parameter.email_pass.arn
      }
    ]
  }])

  tags = {
    Name = "notification-service-task"
  }
}

resource "aws_ecs_service" "notification_service" {
  name            = "notification-service"
  cluster         = aws_ecs_cluster.notification_cluster.id
  task_definition = aws_ecs_task_definition.notification_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 300

  network_configuration {
    security_groups  = [aws_security_group.ecs_sg.id]
    subnets          = slice(data.aws_subnets.all.ids, 0, min(2, length(data.aws_subnets.all.ids)))
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.notification_tg.arn
    container_name   = "notification-service"
    container_port   = 8080
  }

  deployment_controller {
    type = "ECS"
  }

  tags = {
    Name = "notification-service"
  }

  depends_on = [aws_lb_listener.notification_listener]
}

# --- Outputs ---

output "ecr_repo" {
  value = aws_ecr_repository.notification_service.repository_url
}

output "api_url" {
  value = "http://${aws_lb.notification_alb.dns_name}"
}