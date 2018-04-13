@echo OFF
javac -cp ..\..\..\src/ ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.java
javac -cp ..\..\..\BIT/;..\..\..\;..\..\..\src ..\..\..\TestMetrics.java
copy /y ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
copy /y ..\..\..\TestMetrics.class .
java -cp .;BIT/; TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main output/
del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
copy /y .\output\Main.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
java -cp .;BIT/; WebServer