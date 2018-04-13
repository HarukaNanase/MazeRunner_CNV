@echo OFF
javac -cp aws-java-sdk/lib/aws-java-sdk-1.11.313.jar;.;aws-java-sdk/third-party/lib/* aws-java-sdk/samples/AmazonDynamoDB/AmazonDynamoDBSample.java
java -cp aws-java-sdk/lib/aws-java-sdk-1.11.313.jar;.;aws-java-sdk/third-party/lib/*;aws-java-sdk/samples/AmazonDynamoDB/ AmazonDynamoDBSample