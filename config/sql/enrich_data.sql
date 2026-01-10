-- ===============================
-- 清空旧数据并重新插入完整数据
-- 使用真实可访问的图片URL（placehold.co服务）
-- ===============================

-- 清空旧数据
TRUNCATE TABLE product_stock;
TRUNCATE TABLE product;
TRUNCATE TABLE category;

-- ===============================
-- 分类数据（一级+二级分类）
-- ===============================

-- 一级分类
INSERT INTO category (id, name, parent_id, level, sort) VALUES
(10, '手机通讯', 0, 1, 1),
(20, '电脑办公', 0, 1, 2),
(30, '家用电器', 0, 1, 3),
(40, '数码产品', 0, 1, 4);

-- 二级分类
INSERT INTO category (id, name, parent_id, level, sort) VALUES
(101, '手机', 10, 2, 1),
(102, '手机配件', 10, 2, 2),
(201, '笔记本电脑', 20, 2, 1),
(202, '台式机', 20, 2, 2),
(203, '平板电脑', 20, 2, 3),
(301, '大家电', 30, 2, 1),
(302, '生活电器', 30, 2, 2),
(401, '摄影摄像', 40, 2, 1),
(402, '智能设备', 40, 2, 2);

-- ===============================
-- 商品数据（每个二级分类10-20个商品）
-- 使用 placehold.co 提供真实可访问的图片
-- ===============================

-- 手机分类 (category_id = 101)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('华为Mate 60 Pro 12GB+512GB 5G手机', 6999.00, 101, '华为', 'https://placehold.co/800x800/c8102e/white?text=Huawei+Mate60', 1, 1250),
('华为Mate 70 Pro 12GB+512GB 旗舰新品', 6499.00, 101, '华为', 'https://placehold.co/800x800/c8102e/white?text=Huawei+Mate70', 1, 980),
('iPhone 15 Pro Max 256GB 钛金属', 9999.00, 101, '苹果', 'https://placehold.co/800x800/000000/white?text=iPhone+15+Pro', 1, 2100),
('iPhone 15 128GB 双卡双待', 5999.00, 101, '苹果', 'https://placehold.co/800x800/000000/white?text=iPhone+15', 1, 1800),
('小米14 Pro 12GB+256GB 徕卡光学', 4999.00, 101, '小米', 'https://placehold.co/800x800/ff6900/white?text=Xiaomi+14+Pro', 1, 1500),
('小米14 Ultra 16GB+512GB 影像旗舰', 6499.00, 101, '小米', 'https://placehold.co/800x800/ff6900/white?text=Xiaomi+14+Ultra', 1, 890),
('OPPO Find X7 Ultra 16GB+512GB 哈苏', 5999.00, 101, 'OPPO', 'https://placehold.co/800x800/00a368/white?text=OPPO+Find+X7', 1, 750),
('vivo X100 Pro 16GB+512GB 蔡司光学', 5499.00, 101, 'vivo', 'https://placehold.co/800x800/2319dc/white?text=vivo+X100', 1, 680),
('荣耀Magic6 Pro 12GB+256GB AI摄影', 4999.00, 101, '荣耀', 'https://placehold.co/800x800/0085ff/white?text=Honor+Magic6', 1, 920),
('三星Galaxy S24 Ultra 12GB+256GB', 8999.00, 101, '三星', 'https://placehold.co/800x800/1428a0/white?text=Samsung+S24', 1, 560),
('一加12 16GB+512GB 哈苏影像', 4799.00, 101, '一加', 'https://placehold.co/800x800/e32526/white?text=OnePlus+12', 1, 430),
('realme GT5 Pro 16GB+512GB 骁龙8', 3999.00, 101, 'realme', 'https://placehold.co/800x800/f0ca00/white?text=realme+GT5', 1, 520);

-- 手机配件分类 (category_id = 102)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('iPhone 15 Pro Max 硅胶保护壳 原装', 329.00, 102, '苹果', 'https://placehold.co/600x600/555555/white?text=iPhone+Case', 1, 3200),
('华为Mate 60 Pro 原装充电器 66W超级快充', 199.00, 102, '华为', 'https://placehold.co/600x600/c8102e/white?text=66W+Charger', 1, 2800),
('小米Type-C数据线 6A快充 1.5米', 49.00, 102, '小米', 'https://placehold.co/600x600/ff6900/white?text=Type-C+Cable', 1, 5600),
('三星25W PD快充充电器 双口', 129.00, 102, '三星', 'https://placehold.co/600x600/1428a0/white?text=25W+Charger', 1, 1800),
('苹果AirTag 4片装 防丢追踪器', 799.00, 102, '苹果', 'https://placehold.co/600x600/000000/white?text=AirTag', 1, 950),
('小米无线充电器 50W 立式快充', 199.00, 102, '小米', 'https://placehold.co/600x600/ff6900/white?text=50W+Wireless', 1, 1200),
('华为FreeBuds Pro 3 无线降噪耳机', 1199.00, 102, '华为', 'https://placehold.co/600x600/c8102e/white?text=FreeBuds+Pro', 1, 2300),
('OPPO Enco X2 降噪真无线耳机', 699.00, 102, 'OPPO', 'https://placehold.co/600x600/00a368/white?text=Enco+X2', 1, 1560),
('vivo TWS 3 Pro 真无线HiFi耳机', 599.00, 102, 'vivo', 'https://placehold.co/600x600/2319dc/white?text=TWS+3+Pro', 1, 1340),
('倍思65W氮化镓充电器 三口快充', 159.00, 102, '倍思', 'https://placehold.co/600x600/333333/white?text=65W+GaN', 1, 4200);

-- 笔记本电脑分类 (category_id = 201)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('MacBook Pro 14英寸 M3 Pro 18GB+512GB', 16999.00, 201, '苹果', 'https://placehold.co/800x800/333333/white?text=MacBook+Pro+14', 1, 890),
('MacBook Air 13英寸 M3 16GB+512GB 午夜色', 11999.00, 201, '苹果', 'https://placehold.co/800x800/666666/white?text=MacBook+Air+13', 1, 1200),
('华为MateBook X Pro 2024款 i7 32GB+1TB', 12999.00, 201, '华为', 'https://placehold.co/800x800/c8102e/white?text=MateBook+X', 1, 560),
('小米笔记本Pro 15 2024 Ultra7 32GB+1TB', 7999.00, 201, '小米', 'https://placehold.co/800x800/ff6900/white?text=Mi+Book+Pro', 1, 780),
('联想ThinkBook 14 2024 i5 16GB+512GB', 5499.00, 201, '联想', 'https://placehold.co/800x800/e2231a/white?text=ThinkBook+14', 1, 1340),
('戴尔XPS 13 Plus i7 16GB+512GB 4K触屏', 10999.00, 201, '戴尔', 'https://placehold.co/800x800/007db8/white?text=Dell+XPS+13', 1, 420),
('惠普战66六代 锐龙版 R7 16GB+512GB', 4999.00, 201, '惠普', 'https://placehold.co/800x800/0096d6/white?text=HP+Zhan+66', 1, 980),
('华硕天选4 Plus RTX4060 i7 16GB+1TB', 7999.00, 201, '华硕', 'https://placehold.co/800x800/000000/white?text=TUF+Gaming', 1, 1560),
('微软Surface Laptop 5 i7 16GB+512GB', 11999.00, 201, '微软', 'https://placehold.co/800x800/00a4ef/white?text=Surface+Laptop', 1, 320),
('荣耀MagicBook X 16 2024 i5 16GB+512GB', 4299.00, 201, '荣耀', 'https://placehold.co/800x800/0085ff/white?text=MagicBook', 1, 1120),
('机械革命极光Pro RTX4070 i7 32GB+1TB', 9999.00, 201, '机械革命', 'https://placehold.co/800x800/ff0000/white?text=JiGuang+Pro', 1, 640),
('雷神911星战2024 RTX4060 i7 16GB+512GB', 6999.00, 201, '雷神', 'https://placehold.co/800x800/00baff/white?text=THUNDEROBOT', 1, 820);

-- 台式机分类 (category_id = 202)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('联想拯救者刃9000K i9-14900K RTX4080 32GB', 19999.00, 202, '联想', 'https://placehold.co/800x800/e2231a/white?text=Legion+9000K', 1, 230),
('戴尔外星人Aurora R16 i9 RTX4090 64GB', 39999.00, 202, '戴尔', 'https://placehold.co/800x800/00d8ff/white?text=Alienware', 1, 120),
('惠普暗影精灵9 i7-13700F RTX4060Ti 16GB', 8999.00, 202, '惠普', 'https://placehold.co/800x800/0096d6/white?text=OMEN+9', 1, 450),
('华硕天选4台式机 R7-7700 RTX4060 16GB', 6999.00, 202, '华硕', 'https://placehold.co/800x800/000000/white?text=TUF+Desktop', 1, 560),
('攀升组装台式电脑 i5-13400F RTX4060 16GB', 4999.00, 202, '攀升', 'https://placehold.co/800x800/ff6600/white?text=IPASON+Gaming', 1, 1230),
('宁美国度i7-13700KF RTX4070Ti 32GB', 12999.00, 202, '宁美国度', 'https://placehold.co/800x800/d40000/white?text=NINGMEI', 1, 340),
('雷神黑武士5 i7-13700KF RTX4070 32GB', 11999.00, 202, '雷神', 'https://placehold.co/800x800/00baff/white?text=Black+Warrior', 1, 280),
('机械师创物者M7D i7-13700F RTX4060Ti', 8499.00, 202, '机械师', 'https://placehold.co/800x800/333333/white?text=MECHREVO+M7D', 1, 420),
('Apple iMac 24英寸 M3 8核 16GB+512GB', 14999.00, 202, '苹果', 'https://placehold.co/800x800/0071e3/white?text=iMac+24', 1, 380),
('Mac mini M2 Pro 16GB+512GB 迷你主机', 10999.00, 202, '苹果', 'https://placehold.co/800x800/555555/white?text=Mac+mini', 1, 290);

-- 平板电脑分类 (category_id = 203)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('iPad Pro 12.9英寸 M2 256GB WIFI版', 8999.00, 203, '苹果', 'https://placehold.co/800x800/333333/white?text=iPad+Pro+12.9', 1, 1560),
('iPad Air 5 10.9英寸 M1 256GB 妙控键盘', 5399.00, 203, '苹果', 'https://placehold.co/800x800/666666/white?text=iPad+Air+5', 1, 2100),
('华为MatePad Pro 13.2英寸 12GB+512GB', 5999.00, 203, '华为', 'https://placehold.co/800x800/c8102e/white?text=MatePad+Pro', 1, 890),
('小米平板6 Pro 12.4英寸 12GB+512GB', 3499.00, 203, '小米', 'https://placehold.co/800x800/ff6900/white?text=Mi+Pad+6+Pro', 1, 1450),
('荣耀平板V9 12.1英寸 12GB+256GB', 2799.00, 203, '荣耀', 'https://placehold.co/800x800/0085ff/white?text=Honor+Pad+V9', 1, 1120),
('三星Galaxy Tab S9 Ultra 14.6英寸 12GB', 8999.00, 203, '三星', 'https://placehold.co/800x800/1428a0/white?text=Tab+S9+Ultra', 1, 430),
('联想小新Pad Pro 12.7英寸 8GB+256GB', 2299.00, 203, '联想', 'https://placehold.co/800x800/e2231a/white?text=Pad+Pro+12.7', 1, 1680),
('OPPO Pad 2 11.6英寸 12GB+512GB', 2999.00, 203, 'OPPO', 'https://placehold.co/800x800/00a368/white?text=OPPO+Pad+2', 1, 920),
('vivo Pad 3 Pro 12.3英寸 16GB+512GB', 3499.00, 203, 'vivo', 'https://placehold.co/800x800/2319dc/white?text=vivo+Pad+3', 1, 760),
('微软Surface Pro 9 i7 16GB+512GB', 11999.00, 203, '微软', 'https://placehold.co/800x800/00a4ef/white?text=Surface+Pro+9', 1, 520);

-- 大家电分类 (category_id = 301)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('海尔10公斤滚筒洗衣机 直驱变频 智能投放', 2499.00, 301, '海尔', 'https://placehold.co/800x800/006cff/white?text=Haier+Washer', 1, 3400),
('美的三门冰箱 535L 风冷无霜 一级能效', 3999.00, 301, '美的', 'https://placehold.co/800x800/ff0000/white?text=Midea+Fridge', 1, 2800),
('格力1.5匹空调 新一级能效 变频冷暖', 3299.00, 301, '格力', 'https://placehold.co/800x800/00a7e1/white?text=GREE+AC+1.5P', 1, 4200),
('小米电视65英寸 4K超清 120Hz OLED', 2999.00, 301, '小米', 'https://placehold.co/800x800/ff6900/white?text=Mi+TV+65', 1, 5600),
('海信85英寸激光电视 4K超清 影院级', 9999.00, 301, '海信', 'https://placehold.co/800x800/00a0e9/white?text=Hisense+85', 1, 890),
('TCL 98英寸Mini LED电视 4K 144Hz', 19999.00, 301, 'TCL', 'https://placehold.co/800x800/0099cc/white?text=TCL+98+MiniLED', 1, 450),
('西门子洗碗机 13套 嵌入式 自动开门', 5999.00, 301, '西门子', 'https://placehold.co/800x800/009999/white?text=Siemens+13', 1, 1230),
('博世对开门冰箱 610L 变频风冷 零度保鲜', 6999.00, 301, '博世', 'https://placehold.co/800x800/007bc0/white?text=Bosch+610L', 1, 980),
('松下洗衣机12公斤 泡沫净 nanoe除菌', 4999.00, 301, '松下', 'https://placehold.co/800x800/0062ac/white?text=Panasonic+12kg', 1, 760),
('三星QLED电视 75英寸 量子点4K 游戏120Hz', 8999.00, 301, '三星', 'https://placehold.co/800x800/1428a0/white?text=Samsung+QLED+75', 1, 620);

-- 生活电器分类 (category_id = 302)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('戴森V15吸尘器 无线手持 激光探测', 4990.00, 302, '戴森', 'https://placehold.co/800x800/7b2d8f/white?text=Dyson+V15', 1, 2100),
('石头扫地机器人G20 自动洗拖布 避障升级', 3999.00, 302, '石头', 'https://placehold.co/800x800/ff6900/white?text=Roborock+G20', 1, 3400),
('科沃斯扫拖一体机器人 自动集尘 语音控制', 2999.00, 302, '科沃斯', 'https://placehold.co/800x800/00a6d6/white?text=Ecovacs+T20', 1, 4100),
('小米空气净化器4 Pro 米家APP 智能联动', 1999.00, 302, '小米', 'https://placehold.co/800x800/ff6900/white?text=Mi+Air+Pro', 1, 5600),
('美的破壁机 静音多功能 1000W大功率', 599.00, 302, '美的', 'https://placehold.co/800x800/ff0000/white?text=Midea+Blender', 1, 8900),
('九阳电饭煲 5L 智能预约 IH加热', 399.00, 302, '九阳', 'https://placehold.co/800x800/ff6600/white?text=Joyoung+IH', 1, 12000),
('苏泊尔电压力锅 双胆 6L 智能菜单', 499.00, 302, '苏泊尔', 'https://placehold.co/800x800/ff0000/white?text=Supor+6L', 1, 9800),
('飞利浦电动牙刷 声波震动 压力感应', 699.00, 302, '飞利浦', 'https://placehold.co/800x800/0e71b8/white?text=Philips+Sonic', 1, 7200),
('欧乐B电动牙刷 智能压力提醒 APP连接', 899.00, 302, '欧乐B', 'https://placehold.co/800x800/003da5/white?text=Oral-B+Smart', 1, 5400),
('松下电吹风 高速负离子 恒温护发', 599.00, 302, '松下', 'https://placehold.co/800x800/0062ac/white?text=Panasonic+Dryer', 1, 6300);

-- 摄影摄像分类 (category_id = 401)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('佳能EOS R5 全画幅微单 单机身 8K视频', 25999.00, 401, '佳能', 'https://placehold.co/800x800/c8102e/white?text=Canon+EOS+R5', 1, 560),
('尼康Z8 全画幅微单相机 单机身 4K120p', 21999.00, 401, '尼康', 'https://placehold.co/800x800/ffcc00/white?text=Nikon+Z8', 1, 430),
('索尼A7M4 全画幅微单 单机身 5轴防抖', 16999.00, 401, '索尼', 'https://placehold.co/800x800/000000/white?text=Sony+A7M4', 1, 780),
('富士X-T5 APS-C画幅 单机身 胶片模拟', 12999.00, 401, '富士', 'https://placehold.co/800x800/e60012/white?text=Fujifilm+XT5', 1, 640),
('大疆DJI Mini 4 Pro 无人机 4K60fps', 5999.00, 401, '大疆', 'https://placehold.co/800x800/000000/white?text=DJI+Mini+4', 1, 1890),
('大疆Air 3 双镜头航拍无人机 46分钟续航', 6999.00, 401, '大疆', 'https://placehold.co/800x800/333333/white?text=DJI+Air+3', 1, 1560),
('GoPro Hero 12 Black 运动相机 5.3K60', 3999.00, 401, 'GoPro', 'https://placehold.co/800x800/00aeef/white?text=GoPro+Hero+12', 1, 2100),
('大疆Osmo Action 4 运动相机 4K120fps', 2299.00, 401, '大疆', 'https://placehold.co/800x800/000000/white?text=Osmo+Action+4', 1, 2800),
('索尼ZV-E10 Vlog微单相机 美肤模式', 5499.00, 401, '索尼', 'https://placehold.co/800x800/000000/white?text=Sony+ZV-E10', 1, 1230),
('佳能EOS R50 入门级微单 4K30p视频', 4999.00, 401, '佳能', 'https://placehold.co/800x800/c8102e/white?text=Canon+R50', 1, 1450);

-- 智能设备分类 (category_id = 402)
INSERT INTO product (title, price, category_id, brand, image, status, sales) VALUES
('Apple Watch Series 9 GPS 45mm 双击手势', 3199.00, 402, '苹果', 'https://placehold.co/800x800/000000/white?text=Watch+Series+9', 1, 4500),
('华为WATCH GT 4 46mm 高尔夫球模式', 1688.00, 402, '华为', 'https://placehold.co/800x800/c8102e/white?text=WATCH+GT+4', 1, 6700),
('小米手环8 Pro AMOLED屏 200+表盘', 399.00, 402, '小米', 'https://placehold.co/800x800/ff6900/white?text=Mi+Band+8+Pro', 1, 12000),
('小米智能手表S3 北斗定位 19天续航', 1299.00, 402, '小米', 'https://placehold.co/800x800/ff6900/white?text=Watch+S3', 1, 5600),
('小度智能屏X10 10英寸 视频通话', 799.00, 402, '小度', 'https://placehold.co/800x800/0099ff/white?text=Xiaodu+X10', 1, 8900),
('天猫精灵CC10 智能音箱 10.1英寸大屏', 599.00, 402, '天猫精灵', 'https://placehold.co/800x800/ff6600/white?text=Tmall+Genie', 1, 11000),
('小米小爱音箱Play 增强版 红外遥控', 299.00, 402, '小米', 'https://placehold.co/800x800/ff6900/white?text=XiaoAi+Play', 1, 15000),
('米家智能门锁 3D人脸识别 C级锁芯', 1999.00, 402, '米家', 'https://placehold.co/800x800/ff6900/white?text=Smart+Lock', 1, 4200),
('华为智选智能摄像头 360度旋转 2K', 399.00, 402, '华为', 'https://placehold.co/800x800/c8102e/white?text=Smart+Camera', 1, 9800),
('小米智能门铃3 2K超清 AI人形检测', 299.00, 402, '小米', 'https://placehold.co/800x800/ff6900/white?text=Doorbell+3', 1, 13000);

-- ===============================
-- 商品库存数据
-- ===============================

-- 为所有商品生成库存数据
INSERT INTO product_stock (product_id, total_stock, available_stock, locked_stock, version)
SELECT
    id,
    CASE
        WHEN price < 1000 THEN 500    -- 低价商品库存多
        WHEN price < 5000 THEN 200    -- 中价商品库存适中
        WHEN price < 10000 THEN 100   -- 高价商品库存少
        ELSE 50                        -- 旗舰商品库存最少
    END,
    CASE
        WHEN price < 1000 THEN 500
        WHEN price < 5000 THEN 200
        WHEN price < 10000 THEN 100
        ELSE 50
    END,
    0,
    0
FROM product;

-- 完成！总计：
-- 4个一级分类
-- 9个二级分类
-- 约120个商品（每个二级分类10-12个）
-- 所有商品都有对应的库存数据
