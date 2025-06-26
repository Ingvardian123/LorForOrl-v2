# 🚀 Инструкции по сборке LorForOrl v2.0

## Требования

- **Java 17+** (рекомендуется OpenJDK 17)
- **Maven 3.8+**
- **Git** (для клонирования репозитория)

## Быстрая сборка

### 1. Автоматическая сборка (рекомендуется)

\`\`\`bash
# Сделать скрипт исполняемым
chmod +x scripts/build-test.sh

# Запустить сборку
./scripts/build-test.sh
\`\`\`

### 2. Ручная сборка

\`\`\`bash
# Очистка и компиляция
mvn clean compile

# Создание JAR файла
mvn package

# Или все сразу
mvn clean package
\`\`\`

## Результат сборки

После успешной сборки JAR файл будет создан в:
\`\`\`
target/LorForOrl-2.0.jar
\`\`\`

## Установка на сервер

1. Скопируйте `LorForOrl-2.0.jar` в папку `plugins/` вашего сервера
2. Перезапустите сервер
3. Плагин автоматически создаст конфигурационные файлы

## Структура проекта

\`\`\`
LorForOrl/
├── src/main/java/com/lorfororl/     # Исходный код Java
├── src/main/resources/              # Ресурсы (конфиги, модели, звуки)
├── scripts/                         # Скрипты сборки
├── pom.xml                         # Конфигурация Maven
└── README.md                       # Документация
\`\`\`

## Возможные проблемы

### Ошибка компиляции
- Убедитесь что используете Java 17+
- Проверьте подключение к интернету (Maven загружает зависимости)

### Ошибка "Maven не найден"
\`\`\`bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# macOS
brew install maven
\`\`\`

### Ошибка "Java не найдена"
\`\`\`bash
# Проверить версию Java
java -version

# Установить OpenJDK 17
sudo apt install openjdk-17-jdk
\`\`\`

## Разработка

### Импорт в IDE

**IntelliJ IDEA:**
1. File → Open → Выберите папку проекта
2. IDEA автоматически распознает Maven проект

**Eclipse:**
1. File → Import → Existing Maven Projects
2. Выберите папку проекта

### Тестирование

\`\`\`bash
# Запуск тестов
mvn test

# Проверка кода
mvn verify
\`\`\`

## Поддержка

При возникновении проблем:
1. Проверьте лог сборки: `build.log`
2. Убедитесь в правильности версий Java и Maven
3. Проверьте подключение к интернету

---
**LorForOrl v2.0** - Ядерные технологии будущего! 🚀⚛️
