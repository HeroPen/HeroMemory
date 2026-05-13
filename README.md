# HeroMemory - 本地离线密码保险箱

一个基于 Java 8 + Swing 开发的 Windows 本地离线密码保险箱，数据使用 AES-128 加密存储，纯离线无网络连接。

## 功能特性

- **主密码保护**：启动时需验证主密码，首次使用需设置主密码
- **AES-128 加密**：使用 PBKDF2 派生密钥，每次加密生成随机 IV
- **分类管理**：支持社交、工作、娱乐、其他四个分类
- **条目管理**：增删改查密码条目（标题、网址、用户名、密码、备注）
- **搜索功能**：按标题、网址、用户名快速搜索
- **一键复制**：点击按钮复制用户名或密码到剪贴板
- **备份恢复**：支持数据备份和从备份恢复
- **纯离线**：所有数据仅存储在本地，无网络连接

## 技术栈

- Java 8
- Swing (Windows 风格)
- AES-128/CBC/PKCS5Padding
- PBKDF2WithHmacSHA256 (100000 次迭代)
- Maven
- fastjson

## 构建方式

### 前置要求

- JDK 8 或更高版本
- Maven 3.6+

### 构建命令

```bash
mvn clean package -DskipTests
```

构建完成后，fat-jar 位于 `target/java-local-password-vault-1.0.0-jar-with-dependencies.jar`

### 运行

```bash
java -jar target/java-local-password-vault-1.0.0-jar-with-dependencies.jar
```

或者直接运行不带依赖的 jar（需要 classpath 包含 fastjson）：

```bash
java -jar target/java-local-password-vault-1.0.0.jar
```

### 打包为 exe

使用 launch4j 将 fat-jar 打包为 Windows exe：

1. 下载安装 [launch4j](http://launch4j.sourceforge.net/)
2. 使用 launch4j GUI 或命令行配置：
   - 输入 jar: `target/java-local-password-vault-1.0.0-jar-with-dependencies.jar`
   - 输出 exe: `PasswordVault.exe`
   - 主类: `com.vault.VaultMain`
   - 最低 JRE 版本: `1.8.0`
3. 点击构建

或使用 `jpackage`（需要 JDK 14+）：

```bash
jpackage --input target/ --name PasswordVault --main-jar java-local-password-vault-1.0.0-jar-with-dependencies.jar --main-class com.vault.VaultMain --type exe
```

## 数据存储

所有数据存储在 `%USER_HOME%/.password-vault/` 目录：

- `vault.dat` - 加密的密码数据文件
- `salt.dat` - PBKDF2 盐值
- `master.hash` - 主密码验证哈希
- `backup/` - 备份文件目录

## 安全说明

- 明文数据仅在内存中存在，不写入文件
- 主密码通过 PBKDF2 加盐哈希验证
- 每条数据使用 AES-128/CBC 加密，每次生成随机 IV
- 不打印日志、不记录明文
- 纯离线运行，无网络请求
