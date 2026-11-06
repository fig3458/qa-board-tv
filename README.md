# 通用看板（电视 WebView 空壳）

安卓电视 / 盒子上的**全屏看板壳**：第一次启动输入看板网址并保存在本机，之后开机直接打开。  
仓库与发布包**不包含任何公司内网地址**，别人下载后是空白壳，需自行填写网址。

## 功能

- 应用名：**通用看板**
- 首次启动：输入完整网址（`http://` / `https://`），本地 SharedPreferences 保存
- 遥控器菜单：**刷新** / **强制重载** / **设置网址**
- 启动即全屏沉浸、屏幕常亮
- 桌面版 User-Agent + 大屏 viewport，适合 1920×1080 看板页
- 开机自动启动（可按需在系统里关掉）

## 遥控器

| 按键 | 作用 |
|------|------|
| 菜单 / 信息 | 打开/关闭菜单 |
| 刷新看板 | 普通刷新 |
| 强制重载 | 清缓存后重开 |
| 设置网址 | 修改已保存的看板地址 |
| 返回 | 关闭菜单；设置页在已有网址时可返回 |

## 下载与安装

1. 到 [Releases](https://github.com/fig3458/qa-board-tv/releases) 下载 APK
2. 电视：设置 → 账号与安全 → 允许安装未知来源
3. U 盘安装后打开 **通用看板**
4. 首次填写你们自己的看板 URL，点「保存并打开」

## 用 Android Studio 打包

1. 安装 [Android Studio](https://developer.android.com/studio)
2. Open 本目录 `qa-board-tv`
3. 等待 Gradle Sync
4. Build → Build Bundle(s) / APK(s) → Build APK(s)

## 隐私说明

- 看板网址只存在用户设备本地，不会上传
- 源码与 Release **不含默认业务地址**

## 许可证

[MIT](LICENSE)
