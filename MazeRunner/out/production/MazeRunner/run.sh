#!/bin/sh
export _JAVA_OPTIONS=$_JAVA_OPTIONS:"-XX:-UseSplitVerifier"
sudo javac -cp ../../../src/ ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Main.java -d .
sudo javac -cp aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* ../../../DynamoController.java -d .
sudo javac -cp BIT/:../../../src/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/*:. ../../../MetricsData.java -d .
sudo javac -cp ../../../:../../../src/:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* ../../../WebServer.java -d .
sudo javac -cp BIT/:../../../src/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/*:. ../../../TestMetrics.java -d .


sudo javac -cp BIT/:../../../src/: ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Maze.java -d .
sudo javac -cp BIT/:../../../src/: ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/datastructure/Coordinate.java -d .
sudo javac -cp BIT/:../../../src/: ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/AStarStrategy.java -d .
sudo javac -cp BIT/:../../../src/: ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/BreadthFirstSearchStrategy.java -d .
sudo javac -cp BIT/:../../../src/: ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/DepthFirstSearchStrategy.java -d .
sudo javac -cp BIT/:../../../src/: ../../../src/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/RobotController.java -d .
#javac -cp ../../../.:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* ../../../MetricsData.java -d .

#javac -cp ../../../:../../../src/:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* ../../../WebServer.java -d .

sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Main ./output/
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/AStarStrategy output/
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/BreadthFirstSearchStrategy output/
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/DepthFirstSearchStrategy output/
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/datastructure/Coordinate output/
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/RobotController output/
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/* TestMetrics pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Maze output/



sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/datastructure/Coordinate.class
sudo cp ./output/Coordinate.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/datastructure/Coordinate.class
sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/AStarStrategy.class
sudo cp ./output/AStarStrategy.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/AStarStrategy.class
sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/BreadthFirstSearchStrategy.class
sudo cp ./output/BreadthFirstSearchStrategy.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/BreadthFirstSearchStrategy.class
sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/DepthFirstSearchStrategy.class
sudo cp ./output/DepthFirstSearchStrategy.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies/DepthFirstSearchStrategy.class

sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/RobotController.class
sudo cp ./output/RobotController.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/RobotController.class
sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Maze.class
sudo cp ./output/Maze.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Maze.class



sudo rm pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Main.class
sudo cp ./output/Main.class pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/Main.class
sudo java -cp .:BIT/:aws-java-sdk/lib/aws-java-sdk-1.11.313.jar:aws-java-sdk/third-party/lib/*:pt/*: WebServer