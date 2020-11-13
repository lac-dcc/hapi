# hapi-visualizer

with docker
```bash
docker build -t hapi-visualizer .
docker run -p <local port>:8080 -d --rm hapi-visualizer
```

without docker
```
/bin/bash build.sh run --from-scratch
``` 