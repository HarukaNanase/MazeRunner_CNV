@echo OFF
copy /y .\output\WebServer.class WebServer.class
java -cp .;BIT/; WebServer