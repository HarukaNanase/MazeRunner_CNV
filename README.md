# Computação na Nuvem e Virtualização (CNV)
# Final Grade: 18,5

MazeRunner_CNV
Maze Runner in the cloud 2018 CNV

### Classes and What they do

TestMetrics - Class that contains the BIT logic to instrument other classes from the MazeRunner package.

MetricsData - Container class that keeps the data for a specific MazeRunner request. Also contains the formulae to estimate 
RobotController BasicBlocks ran. The class is also mapped into DynamoDB.

WebServer - Class that contains the Web Server logic, which works in a multi-threaded fashion. Receives a request, solves a maze with the parameters and sends back the resulting image. Also saves the request information to DynamoDB.

DynamoController - Class that controls the AWS Dynamo service, at start it creates a table called 'metrics' if it does not exist. By uncommenting the lines for delete, it can also delete the table, wait for the delete and then create it a new, to clean it up. In order to get it working, you need to setup the ~/.aws/credentials file.



### Notes

The metrics we have collected through static and dynamic instrumentation and examination of the compiled code has revealed the following:

 - Most of the time, the execution is spent doing the RobotController simulation, where most of the BasicBlocks executed are found. (99,99%+)

 - MFor the same Maze, starting with different coordinates, results in a different number of executions needed to solve the problem.

 - For different velocity parameters, all the strategies and mazes are affected linearly, being that a higher velocity results in less executed Basic Blocks.

 - To avoid huge performance impact with the instrumentation overhead, we have decided to derive formulas from the bytecode of the RobotController class, which estimate the Basic Blocks executed without actually instrumenting it in a dynamic way. This saves a LOT of overhead, while keeping our examination correct.

 - Instrumenting the Main, Maze and Strategies class in a dynamic way, seems to be enough to find which request needs more computational power, but does not indicate the impact that velocity has or the strategy chosen (directly).

 - We acknowledge that this way of calculating the power required by a request, loses on flexibility, being that changes to the code will require changes to the instrumentation tool, but we avoid having huge overhead where the code is indeed taking the longest time and requiring the most power to solve.

From our results, we can say that the weight of a request follows an inverse relation with the velocity parameter. 

We can also say that the BFS strategy seems to consume more than the AStar strategy which in turns seems to take longer than the DFS strategy, for a 100x100 maze. In other cases, the samething does not apply, but the metrics we have calculated and estimated seem to hold true.

For the Load Balancer and Auto Scaling, we have decided to go with the following:


 - For the Auto Scaler: Scale between 1 and 5 instances. 

 - Scale with average CPU utilization in mind. 

 - Target value of 90, so if it exceeds, we will create a new instance to keep it up below the 90 threshold.

 - 30 Seconds warmup after scaling.
