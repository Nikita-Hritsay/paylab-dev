data "aws_vpc" "default" {
  default = true
}

resource "aws_security_group" "ec2" {
  name        = "${var.project}-${var.env}-ec2-sg"
  description = "SG for backend EC2"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_egress_rule" "ec2_https_out" {
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_ipv4         = "0.0.0.0/0"
  security_group_id = aws_security_group.ec2.id
}