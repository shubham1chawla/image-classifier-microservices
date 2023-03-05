# Generator

Use `generator.py` to send requests to your application. The following command sends three requests to any endpoint.

```
python generator.py --max 3 --api 'http://host/endpoint' --dir "/path/to/dataset/"
```

To send requests using multiple threads, you can use `--multithread` flag.
