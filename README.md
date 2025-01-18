# AnimalsControl
动物繁殖控制，游戏平衡修改

## 功能
- 动物饥饿系统
- 繁殖控制

## 项目结构
```
AnimalsControl/
├── build.gradle.kts          # Gradle 构建配置
├── settings.gradle.kts       # Gradle 项目设置
├── gradle.properties         # Gradle 属性配置
└── src/
    └── main/
        ├── java/com/github/cinnaio/animalscontrol/
        │   ├── AnimalsControl.java              # 插件主类
        │   ├── data/
        │   │   └── AnimalControlData.java       # 数据管理
        │   ├── handlers/
        │   │   ├── AnimalHandler.java           # 动物处理
        │   │   └── TaskManager.java             # 任务管理
        │   └── listeners/
        │       └── AnimalControlListener.java    # 事件监听
        └── resources/
            ├── config.yml    # 插件配置文件
            └── plugin.yml    # 插件信息文件
```

## 构建方法
### 环境
- JDK 21
- Gradle 8.4

### 构建
```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

构建后的插件在 `build/libs/AnimalsControl.jar`

## 命令
- `/acreload` - 重载配置（需要权限：`animalscontrol.reload`）

## 版本要求
- Paper 1.21+

