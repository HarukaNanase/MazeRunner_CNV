@echo OFF
java -cp .;BIT/ TestMetrics WebServer output/
del WebServer.class
copy /y .\output\WebServer.class WebServer.class
java -cp .;BIT/; WebServer