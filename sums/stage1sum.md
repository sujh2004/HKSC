### 一、 Java 代码部分复盘

在阶段一，代码主要分布在三个模块，它们的职责非常清晰：

#### 1. hksc-common (公共模块)

- **代码内容**：
  - **Result<T>**: 统一响应结果封装。
  - **JwtUtils**: JWT 令牌生成与验证工具。
- **核心作用**：
  - **“车同轨，书同文”**：确保所有微服务返回给前端的格式是一样的（都有 code, msg, data）。
  - **代码复用**：Auth 服务负责**造** Token，Gateway 服务将来负责**验** Token，它俩都需要用 JwtUtils，所以必须放在 Common 里。
- **⚠️ 注意事项**：
  - **Install**：每次改了 Common 里的代码，**必须**在 Maven 里执行 install，否则其他服务引用不到最新的代码。
  - **依赖管理**：Common 不应该包含具体的业务依赖（如 MySQL 驱动），只放最通用的工具（如 Lombok, Hutool, JWT）。

#### 2. hksc-gateway (网关服务)

- **代码内容**：
  - 几乎没有 Java 业务代码，核心是一个主启动类。
  - 后续可能会写 GlobalFilter (全局过滤器) 来做 Token 统一校验。
- **核心作用**：
  - **唯一的入口**：前端只知道 8080 端口，不知道后面有 8081, 8082...
  - **路由转发**：根据请求路径（如 /api/auth），把请求“快递”给正确的服务（hksc-auth）。
- **⚠️ 注意事项**：
  - **不要写业务**：网关只做转发、鉴权、限流。不要在网关里查数据库做复杂的业务逻辑。

#### 3. hksc-auth (认证服务)

- **代码内容**：
  - AuthController: 定义 /login 接口。
  - 业务逻辑：接收账号密码 -> (未来查数据库) -> 调用 JwtUtils 生成 Token -> 返回 Result。
- **核心作用**：
  - **发证机关**：整个系统的身份验证中心。
- **⚠️ 注意事项**：
  - **JDK 21 特性**：这是我们开启 **虚拟线程 (Virtual Threads)** 的主战场。因为登录请求通常伴随着数据库 I/O，虚拟线程能极大提升并发吞吐量。

------



### 二、 微服务核心知识点总结

在这一阶段，你们主要实践了 Spring Cloud Alibaba 的以下核心概念：

#### 1. 服务注册与发现 (Service Discovery)

- **角色**：Nacos (Server) 是“通讯录”，Auth 和 Gateway (Client) 是“用户”。
- **流程**：
  1. Auth 启动时，给 Nacos 打电话：“我在 8081，我叫 hksc-auth”。**(注册)**
  2. Gateway 收到请求时，问 Nacos：“hksc-auth 在哪？”。**(发现)**
  3. Nacos 回答：“在 localhost:8081”。
- **价值**：如果 Auth 换了 IP 或启动了 10 个实例，Gateway 不需要改代码，Nacos 会自动告诉它最新的地址。

#### 2. 负载均衡 (Load Balancing)

- **现象**：Gateway 配置里的 lb://hksc-auth。
- **含义**：lb = Load Balance。如果启动了 3 个 Auth 服务，Gateway 会轮流（或按权重）把请求分发给它们，防止某一个累死。
- **变化**：Spring Boot 3 移除了 Ribbon，现在底层用的是 Spring Cloud LoadBalancer。

#### 3. 统一配置管理 (Config Center)

- **现象**：我们把路由规则写在了 Nacos 网页上，而不是代码里。
- **价值**：**热更新**。不需要重启 Gateway，只要在 Nacos 上改一下路由规则并发布，立马生效。这在生产环境非常关键。

------



### 三、 配置文件大总结 (重点！)

这是新手最容易晕的地方，我们按**加载顺序**和**职责**来分类：

#### 1. docker-compose.yml (环境搭建图纸)

- **位置**：项目根目录/docker
- **什么时候用**：**写 Java 代码之前**。
- **作用**：它是**基础设施**的启动说明书。告诉电脑：“请帮我把 MySQL、Redis、Nacos 这些软件跑起来”。
- **核心**：它跟 Java 代码没有直接关系，它只是为 Java 代码提供运行环境（数据库、中间件）。

#### 2. application.yml (微服务的“身份证”)

- **位置**：每个 Java 模块的 src/main/resources
- **什么时候用**：**服务启动的第一瞬间**。
- **作用**：它必须包含连接 Nacos 的**最少必要信息**。
- **写什么**：
  1. spring.application.name: **我是谁？** (Nacos 需要知道名字才能给你发配置)
  2. server.port: **我占哪个端口？**
  3. spring.cloud.nacos.config.server-addr: **Nacos 老板在哪？**
  4. spring.config.import: **我要去拉取远程配置。** (Spring Boot 3 特有写法)

#### 3. Nacos 网页上的配置 (微服务的“工作手册”)

- **位置**：Nacos 控制台 -> 配置管理
- **文件名**：服务名-环境.yaml (如 hksc-gateway-dev.yaml)
- **什么时候用**：服务拿着“身份证”连上 Nacos 后，下载这份配置。
- **作用**：存放**具体的业务配置**。
- **写什么**：
  - Gateway 的路由规则 (routes)。
  - Auth 的数据库连接 (datasource)。
  - Auth 的业务开关、超时时间等。
- **原则**：**能变的都放这里**。因为改这里不需要重启服务，或者重启成本低。

#### 4. bootstrap.yml (时代的眼泪)

- **现状**：在 Spring Boot 2.4 之前，它是必须要写的，用来配置 Nacos 地址。
- **现在 (Spring Boot 3)**：**默认不再需要它**。
- **区别**：
  - 以前：bootstrap.yml (加载极早) -> 连接 Nacos -> application.yml。
  - 现在：application.yml (直接加载) -> 利用 config.import 连接 Nacos。
- **结论**：**在你们的这个新项目中，完全不需要创建 bootstrap.yml**，除非你引入了 spring-cloud-starter-bootstrap 依赖走老路。坚持用 application.yml 即可。