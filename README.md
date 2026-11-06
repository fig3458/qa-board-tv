# QA专用（电视看板壳）

红米/小米电视专用全屏看板 APK。打开即沉浸式加载：

`http://192.168.98.205/mes/#/qaNewBoard`

## 功能

- 应用名：**QA专用**，图标为 **QA**
- 启动即全屏沉浸、屏幕常亮
- 桌面版 User-Agent + 大屏 viewport，避免手机版挤压
- 遥控器 **菜单键 / 信息键**：打开刷新菜单
- 红色快捷键 / 播放键：快速刷新
- 开机自动启动（可按需关掉）

## 遥控器

| 按键 | 作用 |
|------|------|
| 菜单 / 信息 | 打开/关闭菜单 |
| 刷新看板 | 普通刷新 |
| 强制重载 | 清缓存后重开看板 |
| 返回 | 关闭菜单 |

## 用 Android Studio 打包

1. 安装 [Android Studio](https://developer.android.com/studio)
2. Open 本目录 `qa-board-tv`
3. 等待 Gradle Sync
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. 产物：`app/build/outputs/apk/debug/app-debug.apk`

## 装到红米电视

1. 电视：设置 → 账号与安全 → 允许安装未知来源
2. 把 APK 拷进 U 盘，电视文件管理安装
3. 打开 **QA专用**

## 改地址

编辑 `app/src/main/res/values/strings.xml` 里的 `board_url` 后重新打包。
