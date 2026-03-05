# 私人动漫创作自动化工作网站

## 项目介绍
这是一个面向个人创作者的私人动漫创作自动化工作平台，整合AI生成工具与小红书自动发布，实现"输入需求→AI生成→成果管理→自动发布→全流程监控"的闭环，提升个人动漫创作效率。

## 技术栈
- 后端：Java + Spring Boot + MyBatis-Plus
- 前端：Vue3 + Vite + Element Plus
- 数据库：MySQL
- 任务调度：Xxl-Job

## 环境要求
1. JDK 1.8+
2. Node.js 16+
3. MySQL 8.0+
4. Xxl-Job 2.4.0 (调度中心)
5. Stable Diffusion WebUI (可选，用于本地AI生成，开启API模式)

## 部署步骤

### 1. 数据库初始化
1. 登录MySQL，执行 `sql/init.sql` 脚本，自动创建数据库和初始化表结构
```bash
mysql -u root -p < sql/init.sql
```

### 2. 后端配置与启动
1. 修改配置文件 `backend/src/main/resources/application.yml`
   - 修改 `spring.datasource` 中的MySQL用户名和密码
   - 修改 `xxl.job.admin.addresses` 为你的Xxl-Job调度中心地址
2. 进入后端目录，安装依赖并启动
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
后端服务将启动在 `http://localhost:8081`

### 3. 前端配置与启动
1. 进入前端目录，安装依赖并启动
```bash
cd frontend
npm install
npm run dev
```
前端服务将启动在 `http://localhost:8080`

### 4. Xxl-Job配置
1. 启动Xxl-Job调度中心（参考官方文档部署）
2. 在调度中心中，新增执行器，AppName填写 `anime-creator-executor`
3. 注册成功后，即可正常使用任务调度功能

### 5. AI工具配置
1. 如果你使用本地Stable Diffusion，启动SD WebUI时添加参数 `--api`，开启API模式
2. 进入系统的【系统设置】->【AI工具配置】，添加你的Stable Diffusion工具，API地址填写 `http://127.0.0.1:7860`

### 6. 小红书配置
1. 前往小红书开放平台注册开发者账号，创建应用
2. 进入系统的【系统设置】->【小红书配置】，填写AppID和AppSecret
3. 绑定你的小红书账号，即可使用自动发布功能

## 默认登录账号
- 用户名：admin
- 密码：123456

## 功能说明
1. **动漫自动生成**：输入提示词，自动调用AI工具生成动漫图片
2. **成果管理**：管理所有生成的成果，支持预览、下载、删除
3. **自动发布**：一键将成果自动发布到小红书，无需手动操作
4. **任务监控**：实时监控生成和发布任务的状态，失败任务支持重试
5. **系统配置**：支持自定义AI工具、小红书账号、存储路径等配置
