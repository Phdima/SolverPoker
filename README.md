# Poker Preflop Trainer [![RuStore Badge](https://img.shields.io/badge/Download-RuStore-blue?logo=android)](https://www.rustore.ru/catalog/app/com.example.solverpoker)

Kotlin-приложение для тренировки префлоп-решений в NLHE 6-max с интегрированными чартами

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose Version](https://img.shields.io/badge/Jetpack%20Compose-1.7.5-brightgreen)](https://developer.android.com/jetpack/compose)


## 🖼️ Скриншоты

### Основные экраны
| Симулятор префлопа | Чарты решений |
|--------------------|---------------|
| <img src="https://github.com/user-attachments/assets/f85f3c5d-f76f-4efd-a880-2aeba89c6e45" width="300"> | <img src="https://github.com/user-attachments/assets/9a421a3e-2d1e-455e-acad-6373fb31247f" width="300"> |


## 🎯 Функционал
- **Префлоп-симулятор**:
  - Динамическая генерация 6-max столов
  - Адаптивные боты на основе GTO-чартов
  - Валидация решений пользователя
- **Система чартов**:
  - Визуализация диапазонов открытий/ответов
  - Фильтрация по позициям (UTG, HJ, CO, BTN, SB, BB)
  - Offline-доступ к базе решений
 

## ⚙️ Технологии
**Архитектура**:  
`Clean Architecture` · `MVVM` 

**Основной стек**:  
[![Kotlin](https://img.shields.io/badge/-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org) 
[![Compose](https://img.shields.io/badge/-Jetpack%20Compose-4285F4)](https://developer.android.com/jetpack/compose)
[![Coroutines](https://img.shields.io/badge/-Coroutines-5E97D0)](https://kotlinlang.org/docs/coroutines.html)
[![Hilt](https://img.shields.io/badge/-Hilt-8E24AA)](https://dagger.dev/hilt/)

**Дополнительно**:  
`GSON` · `Lifecycle Components` · `Material Design 3`


## ⬇️ Установка
1. Скачайте из RuStore:  

      [![RuStore Badge](https://img.shields.io/badge/Download-RuStore-blue?logo=android)](https://www.rustore.ru/catalog/app/com.example.solverpoker)

2. Или соберите из исходников:
```bash
git clone https://github.com/Phdima/SolverPoker.git
./gradlew assembleDebug
