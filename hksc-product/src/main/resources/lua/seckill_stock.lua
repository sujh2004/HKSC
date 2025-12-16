-- 参数1: seckillId (用来拼装 Key)
-- 参数2: userId (当前用户)
local seckillId = KEYS[1]
local userId = ARGV[1]

-- 定义 Key 名称
local stockKey = "seckill:stock:" .. seckillId
local userKey = "seckill:users:" .. seckillId

-- 1. 校验：用户是否已经买过？(利用 Set 的不可重复性)
if redis.call("sismember", userKey, userId) == 1 then
    return 2 -- 返回码 2 代表：重复购买
end

-- 2. 校验：库存是否充足？
local stock = tonumber(redis.call("get", stockKey))
if stock == nil then
    return -1 -- 返回码 -1 代表：Key 不存在(未预热)
end
if stock <= 0 then
    return 0 -- 返回码 0 代表：库存不足
end

-- 3. 执行扣减
redis.call("decr", stockKey)       -- 库存 -1
redis.call("sadd", userKey, userId) -- 记录该用户已买
return 1 -- 返回码 1 代表：秒杀成功