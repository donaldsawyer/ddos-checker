# ddos-checker
An application that reads an Apache Access Log and determines if there is a DDOS attach occurring.  The rules for the attacks are when a threshold (default is 60) of requests for the same Class B IP in a window (default 1 second).  If a violation is detected, the IP Addresses are logged to disk.  

## Steps of Processing  
1. Produce records to Kafka (asynchronous thread)
   1. Read apache log line
   1. Convert line to json
   1. Attach metadata for `publish_timestamp` and a unique UUID, `publish_uuid`
1. Read records from Kafka for processing
   1. Get time window(s) for the log from the message timestamp
   1. Add meessage to the applicable DDOS check windows for the first two octets of the IP Address
   1. If the window count for an octet breaches the threshold:
      1. Publish all IP Address read for that octet in the window to publish previous IPs
      1. Publish IP address being processed
1. Output total kafka messages read
1. Output Octets that violated DDOS rules

# Expected Output

## File Output
A file will be appended to called `ip_violations.txt` that contains all of the kafka messages that included an IP violation.  There will only be one line per Kafka message, even if the same Kafka message gets reprocessed.

The JSON written includes the time the log line was published to Kafka (received), and when a violation was determined and written.
```json
{
	"remote_user":"-",
	"log_timestamp":"2015-05-25T23:11:53Z",
	"referrer":"-",
	"metadata":{
		"publish_uuid":"9195cb5c-1db7-4743-a68d-82988ec3f403",
		"violation_discover_timestamp":"2017-12-14T06:45:25.151Z",
		"publish_timestamp":"2017-12-14T06:44:54.157Z"
	},
	"status_code":200,
	"request_first_line":"GET / HTTP/1.0",
	"remote_identity":"-",
	"ip_address":"155.156.187.89",
	"response_size":3557,
	"user_agent":"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)"
}
```

## Console Output
The expected output to the console would be the number of records read from Kafka and the IP Octets in Violation.
```
FINISHED READING 163416 RECORDS
--------------- OCTETS PUBLISHED  -------------------
200.004
155.157
209.112
155.156
129.192
063.219
```
