var=${CLASSPATH:-../../../aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:../../../aws-java-sdk/third-party/lib/*}
java -cp .;BIT/ TestMetrics WebServer output/
rm WebServer.class
cp ./output/WebServer.class WebServer.class
java -cp .;BIT/; WebServer