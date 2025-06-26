#!/bin/bash

echo "🚀 Запуск тестовой сборки LorForOrl v2.0..."
echo "=========================================="

# Проверка наличия Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven не найден! Установите Maven для сборки проекта."
    exit 1
fi

# Проверка наличия Java
if ! command -v java &> /dev/null; then
    echo "❌ Java не найдена! Установите Java 17+ для сборки проекта."
    exit 1
fi

echo "✅ Проверка зависимостей прошла успешно"
echo ""

# Очистка предыдущих сборок
echo "🧹 Очистка предыдущих сборок..."
mvn clean

echo ""
echo "🔨 Компиляция проекта..."

# Компиляция с подробным выводом ошибок
mvn compile -X > build.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Компиляция прошла успешно!"
    echo ""
    echo "📦 Создание JAR файла..."
    
    mvn package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "✅ Сборка завершена успешно!"
        echo "📁 JAR файл создан в target/LorForOrl-2.0.jar"
        
        # Показать размер файла
        if [ -f "target/LorForOrl-2.0.jar" ]; then
            SIZE=$(du -h target/LorForOrl-2.0.jar | cut -f1)
            echo "📊 Размер файла: $SIZE"
        fi
        
        echo ""
        echo "🎉 Проект готов к использованию!"
        echo "📋 Для установки скопируйте JAR файл в папку plugins вашего сервера"
        
    else
        echo "❌ Ошибка при создании JAR файла"
        echo "📋 Проверьте лог сборки: build.log"
        exit 1
    fi
    
else
    echo "❌ Ошибки компиляции обнаружены!"
    echo "📋 Подробности в файле: build.log"
    echo ""
    echo "🔍 Последние ошибки:"
    tail -20 build.log | grep -E "\[ERROR\]|\[WARN\]"
    exit 1
fi
