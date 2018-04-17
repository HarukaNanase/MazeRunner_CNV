@echo OFF
title MazeRunner On the CLOUD 2018
javac -cp ..\..\..\src/ ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.java
javac -cp aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* ..\..\..\DynamoController.java -d .
javac -cp BIT/;;..\..\..\src\;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\ ..\..\..\TestMetrics.java
copy /y ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
copy /y ..\..\..\TestMetrics.class .
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main output/
del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
copy /y .\output\Main.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* WebServer