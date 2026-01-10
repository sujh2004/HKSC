---

1.1用户

注册登录个人

### 1.2 商品服务测试

能点商品列表和详情



---

### 1.3 购物车服务测试

#### 测试8：添加商品到购物车
```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "token: <替换为accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "count": 2
  }'
```

**预期结果：**

```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

**验证点：**
- [ ] 返回成功消息
- [ ] Redis中存储购物车数据

**Redis验证：**
```bash
redis-cli
HGETALL cart:user:1
# 应该返回商品ID和数量的键值对
```

---

#### 测试9：查看购物车
```bash
curl -X GET http://localhost:8080/api/cart/list \
  -H "token: <替换为accessToken>"
```

**预期结果：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro Max",
      "price": 8999.00,
      "count": 2,
      "image": "http://example.com/iphone.jpg",
      "checked": true
    }
  ]
}
```

**验证点：**
- [ ] 返回购物车商品列表
- [ ] 数量与添加时一致
- [ ] 商品信息完整（通过Feign调用product服务获取）

---

#### 测试10：更新购物车数量
```bash
curl -X POST http://localhost:8080/api/cart/update \
  -H "token: <替换为accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "count": 5
  }'
```

**预期结果：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

**验证点：**
- [ ] 返回成功消息
- [ ] Redis中数量已更新

---

#### 测试11：删除购物车商品
```bash
curl -X POST http://localhost:8080/api/cart/delete \
  -H "token: <替换为accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1
  }'
```

**预期结果：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**验证点：**
- [ ] 返回成功消息
- [ ] Redis中商品已删除
- [ ] 再次查询购物车不包含该商品

---

### 1.4 搜索服务测试

---

#### 测试13：搜索自动补全
```bash
curl -X GET "http://localhost:8080/api/search/suggest?prefix=手"
```

没有正确的对应语料库 输入小但是本来没有米的 有华为却化

---

## 阶段2：核心业务流程测试

### 2.1 普通订单流程测试

#### 测试14：获取订单防重令牌

---

#### 测试15：创建普通订单

#### 测试16：查看订单列表

#### 测试17：查看订单详情

#### 测试18：订单支付

### 2.2 秒杀流程测试（重点）

---

#### 测试23：并发秒杀（使用JMeter或ab工具）

**预期结果：**
- [ ] 只有10个请求成功（库存只有10）
- [ ] 其他90个请求返回"库存不足"
- [ ] Redis库存最终为0
- [ ] 数据库订单数量为10
- [ ] 无超卖现象

## 阶段3：高级功能测试

### 3.1 Redis缓存测试

### 3.2 Sentinel熔断降级测试

#### 测试25：服务降级测试

---

### 3.3 RabbitMQ延迟队列测试

#### 测试26：订单超时自动关闭

**步骤1：创建订单**
```bash
curl -X POST http://localhost:8080/api/order/create \
  -H "token: <替换为accessToken>" \
  -H "Order-Token: <新的orderToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "count": 1
  }'
```

**步骤2：记录订单ID和创建时间**

**步骤3：访问RabbitMQ管理后台**
```
http://localhost:15672
用户名密码：guest/guest

进入Queues标签页，查看：
- order.delay.queue（延迟队列）
- order.dead.queue（死信队列）
```

**验证点：**
- [ ] order.delay.queue中有1条消息
- [ ] 消息TTL为1800000ms（30分钟）
- [ ] 消息包含订单ID

**步骤4：等待30分钟后检查**
```sql
-- 查询订单状态
SELECT id, order_sn, status, cancel_time
FROM hksc_order.order_info
WHERE id = <订单ID>;
```

**预期结果：**
- [ ] 订单状态变为"已关闭"（status=5）
- [ ] cancel_time已更新
- [ ] cancel_reason为"订单超时自动关闭"

**⚠️ 注意：如果不想等30分钟，可以修改配置文件将TTL改为60秒进行测试**

---

#### 测试27：秒杀订单5分钟关闭
```bash
# 执行秒杀
curl -X POST http://localhost:8080/api/product/seckill/1 \
  -H "token: <新用户的accessToken>"

# 等待5分钟后检查订单状态
```

**验证点：**
- [ ] 5分钟后订单自动关闭
- [ ] 秒杀库存恢复（回滚到Redis）

---

### 3.4 AI功能测试

---

#### 测试29：AI协同过滤推荐
```bash
curl -X GET "http://localhost:8080/api/ai/recommend?userId=1"
```

**预期结果：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "productId": 2,
      "title": "iPhone 15 Pro保护壳",
      "score": 0.95
    },
    {
      "productId": 3,
      "title": "AirPods Pro 2代",
      "score": 0.88
    }
  ]
}
```

**验证点：**
- [ ] 返回推荐商品列表
- [ ] 按评分降序排列
- [ ] 推荐商品与用户购买历史相关

