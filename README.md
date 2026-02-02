# Проект ORMcourses #

## Краткое описание ##

Проект является учебным сервисом по работе с БД. Реализован в рамках обучения на магистратуре НИЯУ МИФИ по предмету ORM-фреймворки для Java.


## Стэк ##

- Java 17+
- Spring Boot
- PostgreSQL 17

## Особенности запуска приложения ##

Перед запуском приложения должен быть поднят PostgreSQL. 

### Шпаргалка для быстрого разворачивания. ###
- понять на каком порту висит postgres
````
sudo pg_lsclusters
# Смотреть на пути в выводе команды (возможно перед этим треубется остановить службу)
````


- Если требуется поменять пароль postgres
```
sudo systemctl stop postgresql
# Вместо числа 14 установленную версию postgres в системе
sudo -u postgres /usr/lib/postgresql/14/bin/postgres --single -D /var/lib/postgresql/14/main -c config_file=/etc/postgresql/14/main/postgresql.conf
# Потом установить пароль например так
ALTER USER postgres PASSWORD 'postgres';
# для выхода нажать CTRL+D

```
- Подключение
```
psql -h localhost -p 5433 -U postgres -W
# и вводим пароль
```
- Создание нового пользователя 
```
CREATE USER ormmaster WITH PASSWORD 'master';
```
- Создание новой БД для пользователя
```
CREATE DATABASE ormdb OWNER ormmaster;
```
- Для подключения к созданной БД под новым пользователем используем
```
psql -h localhost -p 5433 -U ormmaster -d ormdb -W
```

Кроме этого требуется определить переменные окружения (значения заданы для примера):
+ DB_HOST=localhost:5432/dborm;
+ DB_USERNAME=ormmaster;
+ DB_PASSWORD=master

После этого можно запускать приложение.
После запуска можно обращаться к эндпоинтам на порту 8811.

## Файлы миграций ##

При первом запуске приложения все таблицы будут созданы автоматически с помощью набора файлов миграций.

## Интеграционные тесты ##

Реализован полный набор интеграционных тестов, которые покрывают все реализованные эндпоинты.
Для запуска тестов не требуется никаких дополнительных настроек, БД разворачивается в H2 in-memmory.
При проверке тестов проверяются доступ к эндпоинтам для разных ролей пользователей.

## REST API ##

### User Controller ###

    GET /api/users – Получить всех пользователей (ADMIN)

    POST /api/users – Создать нового пользователя (ADMIN)

    DELETE /api/users/{id} – Удалить пользователя (ADMIN)

    PUT /api/users/{id}/role?role={role} – Изменить роль пользователя (ADMIN)

    PUT /api/users/{id}/change-password – Изменить пароль пользователя (ADMIN)

### Profile Controller ###

    GET /api/profiles/me – Получить свой профиль (требуется аутентификация)

    GET /api/profiles/user/{userId} – Получить профиль пользователя по ID (требуется аутентификация)

    PUT /api/profiles/me – Создать или обновить свой профиль (только владелец)

    PUT /api/profiles/user/{userId} – Обновить профиль любого пользователя (ADMIN)

### Category Controller ###

    GET /api/categories – Получить все категории (доступно всем)

    GET /api/categories/{id} – Получить категорию по ID (доступно всем)

    POST /api/categories – Создать категорию (ADMIN)

    PUT /api/categories/{id} – Обновить категорию (ADMIN)

    DELETE /api/categories/{id} – Удалить категорию (ADMIN)

### Course Controller ###

    GET /api/courses – Получить все курсы (доступно всем)

    GET /api/courses/{id} – Получить курс по ID (доступно всем)

    GET /api/courses/teacher/{teacherId} – Получить курсы преподавателя (доступно всем)

    GET /api/courses/category/{categoryId} – Получить курсы категории (доступно всем)

    GET /api/courses/my – Получить мои курсы (TEACHER)

    POST /api/courses – Создать курс (TEACHER, ADMIN)

    PUT /api/courses/{id} – Обновить курс (TEACHER — свои, ADMIN — все)

    DELETE /api/courses/{id} – Удалить курс (TEACHER — свои, ADMIN — все)

### Module Controller ###

    GET /api/modules – Получить все модули (доступно всем)

    POST /api/modules – Создать модуль (ADMIN, TEACHER)

    GET /api/modules/{id} – Получить модуль по ID (доступно всем)

    PUT /api/modules/{id} – Обновить модуль (ADMIN, TEACHER)

    DELETE /api/modules/{id} – Удалить модуль (ADMIN)

### Enrollment Controller ###

    GET /api/enrollments – Получить все связи пользователей с курсами (доступно всем)

    POST /api/enrollments – Создать связь пользователя с курсом (ADMIN)

    GET /api/enrollments/{id} – Получить связь по ID (доступно всем)

    GET /api/enrollments/my – Получить свои связи (требуется аутентификация)

    PUT /api/enrollments/{id} – Обновить статус связи (ADMIN)

    DELETE /api/enrollments/{id} – Удалить связь (ADMIN)

### Lesson Controller ###

    GET /api/lessons – Получить все уроки (требуется аутентификация)

    POST /api/lessons – Создать урок (ADMIN, TEACHER)

    GET /api/lessons/{id} – Получить урок по ID (требуется аутентификация)

    PUT /api/lessons/{id} – Обновить урок (ADMIN, TEACHER)

    DELETE /api/lessons/{id} – Удалить урок (ADMIN, TEACHER)

### Quiz Controller ###

    GET /api/quizzes – Получить все тесты (требуется аутентификация)

    POST /api/quizzes – Создать тест (ADMIN, TEACHER)

    GET /api/quizzes/{id} – Получить тест по ID (требуется аутентификация)

    PUT /api/quizzes/{id} – Обновить тест (ADMIN, TEACHER)

    DELETE /api/quizzes/{id} – Удалить тест (ADMIN, TEACHER)

### Submission Controller ###

    GET /api/submissions – Получить все ответы (требуется аутентификация)

### Assignment Controller ###

    GET /api/assignments – Получить все задания (требуется аутентификация)

    POST /api/assignments – Создать задание (ADMIN, TEACHER)

    GET /api/assignments/{id} – Получить задание по ID (требуется аутентификация)

    PUT /api/assignments/{id} – Обновить задание (ADMIN, TEACHER)

    DELETE /api/assignments/{id} – Удалить задание (ADMIN, TEACHER)