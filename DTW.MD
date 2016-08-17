# Route Matching using Dynamic Time Warping with the JavaML library

Some preliminary remarks: there are other distance similarity algorithms - dynamic time warping is widely used in speech and gesture recognition but there is some promise in adapting it for route matching (Other algorithms available: http://java-ml.sourceforge.net/api/0.1.7/net/sf/javaml/distance/package-summary.html). Other algorithms to consider may include

+ Euclidean Distance
+ Fastest Similarity Search (http://www.cs.unm.edu/~mueen/FastestSimilaritySearch.html)

There is a way to compute a similarity measure based on the raw DTW computation using the `DTWSimilarity` class that may be of interest

```java
double similarityIndex = new DTWSimilarity().measure(Instance a, Instance b);
```

where our instances are defined as a `double` array (though this is typically a static-framed object with a number of properties), we can use this instead as an order-important sequence, a time series, to be compared.

Using the original DTW computations we can get a distance measure like this

```java
double distance = new DTW().getWarpDistanceBetween(TimeSeries a, TimeSeries b);
```

There may need to be some work done to map a time series to a JavaML instance and vice versa - ~~right now the exact nature of the `TimeSeries` class is still unclear.~~

You can create a time series from an instance:
```java
TimeSeries t = new TimeSeries(Instance a);
```

It will be worthwhile to also mention that there is a `FastDTW` class. We can grab that index like

```java
double similarityIndex = new FastDTW(int radius).measure(Instance a, Instance b)
```

Read more about what `radius` means in this context here: http://www.cs.fit.edu/~pkc/papers/tdm04.pdf and here: http://www.cs.fit.edu/~pkc/papers/ida07.pdf

## Classification

As we've seen before, we can add our training instances (time series) to the dataset and then build our classifier from that dataset. The next steps regarding how to classify with the distance/similarity measures are still unclear and require discussion. 

When we have our desired set of series, we need to compute a distance matrix, the distances being the indices computed from DTW - from there we need to feed the distance matrix to the dataset in a way that is interpretable by a clusterer that we can then build and begin clustering alike time series and identify common routes
