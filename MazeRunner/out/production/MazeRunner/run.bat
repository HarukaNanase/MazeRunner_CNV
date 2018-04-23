@echo OFF
title MazeRunner On the CLOUD 2018
javac -cp ..\..\..\src/ ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.java -d .
javac -cp aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* ..\..\..\DynamoController.java -d .
javac -cp BIT/;;..\..\..\src\;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\ ..\..\..\TestMetrics.java -d .
javac -cp BIT/;;..\..\..\src\;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\ ..\..\..\MetricsData.java -d .


javac -cp BIT/;..\..\..\src\; ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Maze.java -d .
javac -cp BIT/;..\..\..\src\; ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\datastructure\Coordinate.java -d .
javac -cp BIT/;..\..\..\src\; ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\AStarStrategy.java -d .
javac -cp BIT/;..\..\..\src\; ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\BreadthFirstSearchStrategy.java -d .
javac -cp BIT/;..\..\..\src\; ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\DepthFirstSearchStrategy.java -d .
javac -cp BIT/;..\..\..\src\; ..\..\..\src\pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\RobotController.java -d .

javac -cp javac -cp ..\..\..\;..\..\..\src\;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* ..\..\..\WebServer.java -d .

java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main output/
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\AStarStrategy output/
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\BreadthFirstSearchStrategy output/
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\DepthFirstSearchStrategy output/
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\datastructure\Coordinate output/
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\RobotController output/
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\* TestMetrics pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Maze output/



del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\datastructure\Coordinate.class
copy /y .\output\Coordinate.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\datastructure\Coordinate.class
del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\AStarStrategy.class
copy /y .\output\AStarStrategy.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\AStarStrategy.class
del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\BreadthFirstSearchStrategy.class
copy /y .\output\BreadthFirstSearchStrategy.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\BreadthFirstSearchStrategy.class
del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\DepthFirstSearchStrategy.class
copy /y .\output\DepthFirstSearchStrategy.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\strategies\DepthFirstSearchStrategy.class

del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\RobotController.class
copy /y .\output\RobotController.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\RobotController.class
del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Maze.class
copy /y .\output\Maze.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Maze.class



del pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
copy /y .\output\Main.class pt\ulisboa\tecnico\meic\cnv\mazerunner\maze\Main.class
java -cp .;BIT/;aws-java-sdk\lib\aws-java-sdk-1.11.313.jar;aws-java-sdk\third-party\lib\*;pt\*; WebServer