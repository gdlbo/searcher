# Searcher

## Описание
Searcher — это веб-приложение для поиска файлов на сервере. Разработанное с использованием Java 21 (Spring Boot) и базы данных PostgreSQL, оно предоставляет пользователям возможность поиска по различным критериям и типам сортировки.

## Требования
- Java 21
- PostgreSQL 16
- Gradle

## Установка

### Настройка базы данных
1. Установите PostgreSQL и создайте новую базу данных для вашего приложения.
2. Создайте таблицу `files` с помощью SQL-команды:
```sql
CREATE ROLE searcher LOGIN PASSWORD 'searcher';
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO searcher;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO searcher;
DO $$
DECLARE
    seq record;
BEGIN
    FOR seq IN SELECT sequence_schema, sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public'
    LOOP
        EXECUTE format('GRANT ALL PRIVILEGES ON SEQUENCE %I.%I TO searcher;', seq.sequence_schema, seq.sequence_name);
    END LOOP;
END $$;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO searcher;
```

### Сборка и запуск приложения
1. Склонируйте репозиторий:
   ```
   git clone https://github.com/gdlbo/searcher.git
   ```
2. Перейдите в директорию проекта и выполните сборку с помощью Gradle:
   ```
   cd searcher
   ./gradlew bootJar
   ```
3. Запустите собранное приложение из директории `/build/libs/` с файлом `searcher-1.0.0.jar`.
   ```
   java -jar build/libs/searcher-1.0.0.jar
   ```
4. Зарегистрируйте пользователя `admin` из веб панели регистрации перейдя по URL развернутого сервера для дальнейшей инициализации сервера.

## Использование
Чтобы начать работу с приложением, откройте ваш веб-браузер и перейдите по URL, где развернут сервер. Воспользуйтесь формой поиска для нахождения файлов по различным параметрам.