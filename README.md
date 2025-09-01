# Система управления банковскими картами

## Ходжаев Абдужалол

## Запуск

Создадите `.env`. Должны быть указаны переменные окружения как в примере:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=bankcardDB
JWT_SECRET=cmVlmKuXwjIuT5jJAhm8k4um7zrT75DvhMD15R1HsH1UM5pYWdz3OPZN7AzJIyyBG1essmhRKtyVBXCqIeq63sM1fSgfrwXfg7RhpFA6Autm8Vfn41cVH7880mFnDihFO516aDo9OMhOHZ3REux3Rh1djOAIOCr9LuCftcasyHUOXiO3GfLu11Dhr4zgjVO0OdjSbZhR79uF25BWT1hfdTJ04aD5iu4kCMXX9haA6WUBqBbnnFOHMRmqePoQK65Q
ADMIN_NAME=admin
ADMIN_PASS=adminpass
ENCRYPTION_SECRET=8279c8fbc5a6be2c
```

Собрать `jar` (Также произойдёт тестирование)

```bash
mvn clean package
```

Запуск:

```bash
docker compose up
```

Приложение теперь запущено на `localhost:8080`

Swagger-UI доступен по адресу `http://localhost:8080/swagger-ui.html`. С его помощью можно легко проверить всё эндпойнты
