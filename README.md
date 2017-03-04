# Big-Data-Analytics-Web-Service

This is a RESTful web service which displays Twitter user demographics and performs sentiment analysis.

It deployed all services and resources on AWS EC2 and S3, using Cloud Watch and Load Balancer to monitor.

The source code include:

1. Mapper and Reducer code to perform ETL for more than 1 TB of raw data. The final output inlcudes well-formatted Tweet posts associated with sentiment scores.
2. RESTful implementation to perform CRUD operations on different backend databases, which are MySQL and HBase.
3. LRU cache implementation to tune the performance.
