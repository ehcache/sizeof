ehcache-sizeofengine
====================

What is this?
-------------

Simply by being on your application's classpath, ehcache-sizeofengine will automagically be picked up by [Ehcache](http://www.ehcache.org).
It replaces the SizeOfEngine implementation that ships with Ehcache (2.8.0 onwards) with the EhcacheSizeOfEngine implementation of this project, which lets you control what is then being sized.

Using existing filter configurators
-----------------------------------

You simply need to add the jars of the modules that you want along side this project's jar

 - Hibernate
    - [https://github.com/alexsnaps/ehcache-sizeofengine-hibernate](https://github.com/alexsnaps/ehcache-sizeofengine-hibernate v4+)
    - [https://github.com/alexsnaps/ehcache-sizeofengine-hibernate3](https://github.com/alexsnaps/ehcache-sizeofengine-hibernate3 v3+)
 - Groovy
    - [https://github.com/alexsnaps/ehcache-sizeofengine-groovy](https://github.com/alexsnaps/ehcache-sizeofengine-groovy)

Configuring the Filter yourself
-------------------------------

In order to ignore fields or instances of certain classes when sizing object graphs, you'll have to
 1. Create a [ServiceLoader](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) project, for [net.sf.ehcache.sizeofengine.FilterConfigurator](http://terracotta-oss.github.io/ehcache-sizeofengine/apidocs/net/sf/ehcache/sizeofengine/FilterConfigurator.html)
   - Have your jar contain a text file named META-INF/services/net.sf.ehcache.sizeofengine.FilterConfigurator
   - The file should contain the fully qualified class name of your implementation
 2. Implement [FilterConfigurator](http://terracotta-oss.github.io/ehcache-sizeofengine/apidocs/net/sf/ehcache/sizeofengine/FilterConfigurator.html)'s configure method to configure the [Filter](http://terracotta-oss.github.io/ehcache-sizeofengine/apidocs/net/sf/ehcache/sizeofengine/Filter.html) of classes and fields
 3. put your jar on your application's classpath, along side of the ehcache jar and this ehcache-sizeofengine jar
 4. Use Ehcache's [Automatic Resource Control](http://ehcache.org/documentation/arc) for your heap tier

Example
-------

        public static final class StupidConfigurator implements FilterConfigurator {

            @Override
            public void configure(final Filter ehcacheFilter) {
                // Will not size any instance of Number, and given the second arg, no subtype neither
                ehcacheFilter.ignoreInstancesOf(Number.class, false);
            }
        }

There can be as many FilterConfigurator on the classpath as required, they'll have configure the filter once.
The Filter is shared across all SizeOfEngine instances created.

Using it
========

Maven
-----

Releases are available from Maven Central.

Snapshots are available from the Sonatype OSS snapshot repository.
In order to access the snapshots, you need to add the following repository to your pom.xml:
```
<repository>
    <id>sonatype-nexus-snapshots</id>
    <name>Sonatype Nexus Snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
