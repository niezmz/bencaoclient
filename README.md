# 本草识物 (Bencao Client)

基于 Jetpack Compose 的 Android 植物识别应用。拍照识别植物物种，获取栽种养护指南，管理个人本草收藏。

APP 的设计思路、UI、基础代码由人类完成，复杂或细节代码由 AI 辅助完成。

## 功能

### 1. 拍摄和云端 AI 识别

调用手机原生相机拍摄，将图片上传至云端 AI（豆包视觉模型）进行识别。默认适配竖屏 3:4 比例，9:16 兼容性正常，其他比例可能需要调整。

### 2. 获取植物信息

云端 AI 返回以下信息：

- 名称
- 描述
- 所属的科、属、种
- 稀有度评定（1-5）
- 是否有毒
- 是否保护物种（中国大陆标准）
- 是否入侵物种（中国大陆标准）

### 3. 获取栽种信息

使用文本模型分析，返回简要养护指南：

- 光照
- 温度
- 水分
- 土壤
- 施肥
- 病虫害防治

### 4. 保存、查阅和分享

植物保存后可当作"物种档案"查阅，支持分类和画廊两种浏览模式。单张图片可分享——原始图片会被包装为简约海报，包含名称、时间、随机 slogan。

### 5. 导入导出

图片和植物信息独立保存（不直接存入系统相册，可手动另存）。通过 ZIP 导入导出，数据可在多台设备间转移或汇总。

### 6. 二十四节气海报

仓库附带 76 张二十四节气海报，可自行替换。播放规则：邻近某节气时循环播放该节气的海报（一般 3-4 张）。

## 技术栈

| 类别 | 技术 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 数据库 | Room (KSP) |
| 网络 | OkHttp 4 |
| 图像 | Coil 2.5 + ExifInterface |
| Markdown | mikepenz multiplatform-markdown-renderer |
| AI 接口 | 火山方舟 (Volcengine Ark) OpenAI 兼容 API |

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17+
- Android SDK 36
- Gradle 9.3+（项目自带 wrapper）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/niezmz/bencaoclient.git
cd bencaoclient
```

### 2. 配置 API KEY

```bash
cp local.properties.example local.properties
```

编辑 `local.properties`，填入你的配置：

```properties
sdk.dir=/path/to/Android/Sdk
DOUBAO_API_KEY=你的火山方舟API-KEY
```

API-KEY 在 [火山方舟控制台](https://console.volcengine.com/ark) 创建。也可以启动 APP 后在「AI 设置」页面动态配置。

### 3. （可选）更换 AI 模型

在 `local.properties` 或 `gradle.properties` 中添加：

```properties
# 视觉识别模型（默认 doubao-seed-1-6-vision-250815）
DOUBAO_MODEL=your-vision-model-id

# 栽种方式文本模型（默认 doubao-seed-2-0-mini-260428）
DOUBAO_PLANTING_MODEL=your-text-model-id
```

### 4. 构建运行

```bash
./gradlew assembleDebug
```

或用 Android Studio 打开项目直接运行。

## 项目结构

```
app/src/main/java/com/example/bencaoclient/
├── MainActivity.kt           # 主 Activity + 页面导航
├── Bencao.kt                 # 领域模型
├── BencaoRepository.kt       # 数据仓库（Room ↔ 领域模型）
├── ApiKeyStore.kt            # API-KEY 本地持久化
├── BencaoSpeciesBackupZip.kt # ZIP 导入导出
├── GalleryScreen.kt          # 图库页面
│
├── ai/                       # AI 接口层
│   ├── DoubaoAi.kt            # 豆包 API 客户端
│   └── BackendAiSuggestionJson.kt  # 响应解析
│
├── db/                       # Room 数据库
│   ├── AppDatabase.kt
│   ├── BencaoDao.kt
│   ├── BencaoEntity.kt
│   └── DbConverters.kt
│
├── model/                    # UI 模型
│   └── UiModels.kt
│
├── ui/
│   ├── component/            # 可复用组件
│   ├── screen/               # 独立页面
│   └── theme/                # 主题（Color/Dimens/Type）
│
├── util/                     # 工具类
│   ├── AppUtils.kt
│   ├── ImageUtils.kt
│   ├── ImagePainterUtils.kt
│   ├── PosterUtils.kt
│   ├── SearchUtils.kt
│   └── ShareUtils.kt
│
└── activity/                 # 活动海报
    ├── ActivityPosterResolver.kt
    └── HomeActivityPosterCarousel.kt
```

## 二次开发

### 接入其他 AI 服务商

APP 默认接入火山方舟平台，API 与 OpenAI Chat Completions 兼容。其他兼容服务（如 DeepSeek、通义千问）可直接替换：

1. 修改 `app/build.gradle.kts` 中的 `DOUBAO_BASE_URL`
2. 修改模型名称 `DOUBAO_MODEL` / `DOUBAO_PLANTING_MODEL`
3. 如果响应格式不同，修改 `BackendAiSuggestionJson.kt`
4. 替换整个 `ai/` 包也可

### 扩展数据库字段

1. 在 `BencaoEntity.kt` 中添加字段
2. 在 `AppDatabase.kt` 中添加迁移（递增版本号 + 编写 MIGRATION）
3. 在 `Bencao.kt` 领域模型中添加对应字段
4. 更新 `BencaoRepository.kt` 中的映射逻辑

### 添加新页面

1. 在 `ui/screen/` 下创建新文件
2. 在 `MainActivity.kt` 的 `NavHost` 中添加 `composable` 路由
3. 如需底部导航入口，在 `NavigationItem` 列表中添加

### 更换海报

替换 `app/src/main/assets/activity/posters/` 下的图片即可。命名规则：`{月份}-{上下月，0为上班月，1为下半月}-{序号}.jpg`

## License

本项目仅供学习和研究使用。
