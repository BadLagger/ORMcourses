# Проект ORMcourses #

## Краткое описание ##

Проект является учебным сервисом по работе с БД.

## Стэк ##

- Java 17+
- Spring Boot
- PostgreSQL 17

## Особенности запуска приложения ##

Перед запуском приложения должен быть поднят PostgreSQL. Шпаргалка для быстрого разворачивания.
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