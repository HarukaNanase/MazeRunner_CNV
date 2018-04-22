# MazeRunner_CNV
Maze Runner in the cloud 2018 CNV


The metrics we have collected through static and dynamic instrumentation and examination of the compiled code has revealed the following:

 - Most of the time, the execution is spent doing the RobotController simulation, where most of the BasicBlocks executed are found. (99,99%+);
 - For the same Maze, starting with different coordinates, results in a different number of executions needed to solve the problem.
 - For different velocity parameters, all the strategies and mazes are affected linearly, being that a higher velocity results in less executed Basic Blocks.
 - To avoid huge performance impact with the instrumentation overhead, we have decided to derive formulas from the bytecode of the RobotController class, which estimate the Basic Blocks executed without actually instrumenting it in a dynamic way. This saves a LOT of overhead, while keeping our examination correct.
 - Instrumenting the Main, Maze and Strategies class in a dynamic way, seems to be enough to find which request needs more computational power, but does not indicate the impact that velocity has.

We acknowledge that this way of calculating the power required by a request, loses on flexibility, being that changes to the code will require changes to the instrumentation tool, but we avoid having huge overhead where the code is indeed
taking the longest time and requiring the most power to solve.

From our results, we can say that the weight of a request follows an inverse relation with the velocity parameter.
We can also say that 

For the Load Balancer and Auto Scaling, we have decided to go with the following:

For the Auto Scaler:
	Scale between 1 and 5 instances.
	Scale with average CPU utilization in mind.
	Target value of 90.
	30 Seconds warmup after scaling.
