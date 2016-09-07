# roovemore-slim3-modelcache

This is a Java library for the Google App Engine Java.  
This service uses slim3's memcache service. and Save the ModelClass to cache.

This is currently in development (2016.09.03) .  
So there is a possibility to change the Method name and Paramerter .

## Usage samplie

If you want to save the data in the cache and datastore.

```
SampleSlim3Model slim3Model = new SampleSlim3Model();
slim3Model.setTitle("Test");

// save the data in the cache and datastore.
ModelCacheManager.putDbCache(SampleSlim3Model.class, slim3Model);
```

If you want to get the data from the cache.  
( If there is no cache , to get from datastore. )

```
// get the data from the cache.
List<SampleSlim3Model> list = ModelCacheManager.getCacheListFromKeyTable(SampleSlim3Model.class);
```

## Setup
Set up using the Maven.  
As the following is How to set up the pom.xml

pom.xml
```
<dependencies>
	<dependency>
		<groupId>com.appspot.roovemore.slim3.modelcache</groupId>
		<artifactId>roovemore-slim3-modelcache</artifactId>
		<version>1.0-SNAPSHOT</version>
	</dependency>
</dependencies>
```

