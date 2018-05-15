@echo OFF
javac -cp aws-java-sdk/lib/aws-java-sdk-1.11.313.jar;.;aws-java-sdk/third-party/lib/* ../../../EC2LaunchWaitTerminate.java -d .
java -cp aws-java-sdk/lib/aws-java-sdk-1.11.313.jar;.;aws-java-sdk/third-party/lib/*;aws-java-sdk/samples/AmazonDynamoDB/ EC2LaunchWaitTerminate 1