docker compose -f docker-compose.yml up --detach
chmod a+x ./tests/.github/workflows/wait-for-it.sh
./tests/.github/workflows/wait-for-it.sh -t 60 localhost:8080
docker compose -f docker-compose.yml logs
