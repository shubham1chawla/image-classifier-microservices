# Image Classification Microservices

This repository contains services and scripts to classify images. The project contains three maven modules: `library-module`, containing shared code and configuration; `gateway-module`, exposing an endpoint to accept images; and `classifier-module`, which executes the `classifier.py` script and returns to the result. These modules can be independently hosted on _AWS_'s _EC2_ instances, and use _SQS_ to communicate with each other, while uploading requests' images and their classifications in _AWS_'s _S3_ buckets.

## Run Arguments

Please provide following arguments to run these modules.

```
	access-key - AWS's account's access key
	secret-key - AWS's account's secret key
	region - AWS's region, for instance, us-east-1
	req-queue-url - AWS's standard SQS queue's URL to post requests' key
	res-queue-url - AWS's standard SQS queue's URL to post responses' key
	input-bucket - AWS's S3 bucket's name to store requests' images
	output-bucket - AWS's S3 bucket's name to store classification results
```