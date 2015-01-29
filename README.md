

<h4>Batch Processing Library</h4>

<p>This library is meant to handle asynchronous batch processing. It solves the following problem: We want to use CPU to perform work on a specific piece of data. However, this data must be pulled from an external source (anything requiring I/O). If we simply perform work on each item and then request the next item, we will be blocking each time, resulting in inefficient code. The solution is to create an asynchronous request to retrieve a batch of data, perform work on our current batch, and by the time we are finished performing work, the next batch will be available. The amount of data pulled in asynchronously can be adjusted, and simple congestion algorithms have been implemented to limit both blocking and memory consumption (because if memory were not an issue, we could just pull in all data to memory at the same time and then process all of it).</p>

<h4>Loading package and dependencies into your project</h4>

<p>The only dependency is slf4j logging and org.specs2 testing. Other than that, simply type sbt package, and copy the resulting jar into the /lib folder of your current project. You can also publish this project if you wish to explicitly manage dependencies with sbt or maven.</p>

<h4>Examples</h4>

<p>For examples, view the test cases in the src/test/scala folder.</p>

<h4>RoadMap</h4>

<p>I plan to use this library as a starting point for adding more batch processing workflows as well as parallelization and distributed computing.</p>