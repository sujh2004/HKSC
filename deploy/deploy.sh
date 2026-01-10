#!/bin/bash
##############################################################
# HKSC 一键部署脚本
# 用途：快速启动或更新HKSC电商平台
##############################################################

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    print_info "检查依赖..."

    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker compose &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装"
        exit 1
    fi

    print_info "✓ Docker 和 Docker Compose 已就绪"
}

# 创建环境变量文件
create_env_file() {
    if [ ! -f ".env" ]; then
        print_warn ".env 文件不存在，从模板创建..."
        cp .env.example .env
        print_warn "请编辑 .env 文件，设置数据库密码等配置"
        read -p "按回车键继续..."
    fi
}

# 选择部署模式
select_mode() {
    echo ""
    echo "请选择部署模式："
    echo "  1) 开发模式 - 仅启动基础设施 (MySQL, Redis, Nacos, RabbitMQ, ES)"
    echo "  2) 生产模式 - 启动全部服务（基础设施 + 应用服务）"
    echo "  3) 仅更新应用服务"
    echo ""
    read -p "请选择 [1-3]: " mode

    case $mode in
        1)
            MODE="dev"
            ;;
        2)
            MODE="prod"
            ;;
        3)
            MODE="update"
            ;;
        *)
            print_error "无效的选择"
            exit 1
            ;;
    esac
}

# 启动开发模式
start_dev() {
    print_info "启动开发环境（基础设施）..."
    docker compose up -d mysql redis nacos rabbitmq elasticsearch
    print_info "等待服务启动..."
    sleep 30
    check_status
}

# 启动生产模式
start_prod() {
    print_info "启动生产环境（全部服务）..."
    docker compose up -d
    print_info "等待服务启动..."
    sleep 60
    check_status
}

# 更新应用服务
update_services() {
    print_info "更新应用服务..."

    # 重新构建镜像
    print_info "重新构建镜像..."
    docker compose build gateway user product cart order search

    # 滚动更新
    print_info "滚动更新服务..."
    docker compose up -d --no-deps gateway
    sleep 10
    docker compose up -d --no-deps user product cart order search

    print_info "清理旧镜像..."
    docker image prune -f

    check_status
}

# 检查服务状态
check_status() {
    print_info "检查服务状态..."
    docker compose ps

    echo ""
    print_info "服务访问地址："
    echo "  - 网关: http://localhost:8080"
    echo "  - Nacos控制台: http://localhost:8848/nacos (nacos/nacos)"
    echo "  - RabbitMQ管理: http://localhost:15672 (guest/guest)"
    echo ""
}

# 查看日志
view_logs() {
    print_info "查看最近日志..."
    docker compose logs --tail=100 -f
}

# 停止所有服务
stop_all() {
    print_info "停止所有服务..."
    docker compose down
    print_info "服务已停止"
}

# 主菜单
main_menu() {
    echo ""
    echo "================================"
    echo "  HKSC 部署管理脚本"
    echo "================================"
    echo ""

    check_dependencies
    create_env_file
    select_mode

    case $MODE in
        dev)
            start_dev
            ;;
        prod)
            start_prod
            ;;
        update)
            update_services
            ;;
    esac

    echo ""
    echo "其他操作："
    echo "  查看日志: docker compose logs -f [service]"
    echo "  停止服务: docker compose down"
    echo "  重启服务: docker compose restart [service]"
    echo ""
}

# 执行主函数
main_menu
