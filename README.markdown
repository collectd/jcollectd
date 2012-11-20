## jcollectd - Java integration for collectd

### collectd

[collectd](http://collectd.org) is a lightweight, extensible system statistics gathering
daemon written in C for performance and portability.  In-process
plugins can be written in C or Perl.  Out-of-process plugins are
executed on each collection interval or can put values via the
unixsock plugin.

### jcollectd

The jcollectd package implements the collectd protocol in Java, making
it possible for Java applications to push data into collectd over the
wire.  A Java app can configure JMX MBean attributes to be collected,
or use the Java client API directly. 

The listener side of the protocol is also supported, including an
implementation that registers JMX MBeans making it possible to view
collectd data using a graphical tool such as jconsole.

There is no installation process for jcollectd, as all required files
are self-contained in *collectd.jar* with no dependecies other than
Java version 5 or higher.  The command-line and java configuration
options required to use the package are described below.
    
### MBeanReceiver

The **MBeanReceiver** class listens for collectd packets and publishes the
data as JMX MBeans.  A summary MBean is created to aggregate metrics, where
the attributes values are the average across all instances.
The MBeans can be viewed using a tool such as the standard jconsole.
The MBeanReceiver can be started using the following command:

        java -jar collectd.jar

#### Examples

* Listen on the default IPv4 multicast group **239.192.74.66:25826**:

        java -jar collectd.jar -jconsole

* Listen on the IPv4 unicast address **127.0.0.1:25827** and start
  jconsole attached locally to the collectd.jar process:

        java -Djcd.laddr=127.0.0.1:25827 -jar collectd.jar -jconsole

* Listen on the default IPv4 multicast group and allow remote JMX
  connections to RMI/TCP port *25826*:

        java -Dcom.sun.management.jmxremote.port=25826 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar collectd.jar

* Connect to the MBeanServer example above from another machine:

        jconsole the-remote-hostname:25826

#### Configuration

* jcd.laddr - UDP listen address, the default is **239.192.74.66:25826**.
  Example:

        jcd.laddr=localhost:25826

* jcd.ifaddr - The multicast network interface, default is any.
  Example:

        jcd.ifaddr=eth2

* jcd.typesdb - collectd types database files, default is types.db from collectd.jar
  Example:

        jcd.typesdb=/opt/collectd/lib/types.db:./mytypes.db

* jcd.mx.summary - Enable summary MBean aggregator, the default is true.
  Example:

        jcd.mx.summary=false

* jcd.mx.hosts - Filter displayed hosts using a regex, the default is .*.
  Example:

        jcd.mx.hosts=dev|qa

### MBeanSender

Any Java application can be configured to publish MBean data without
any code changes.  The following example will push java.lang:* MBeans
to the default collectd multicast group:

        java -Djcd.tmpl=javalang -javaagent:collectd.jar YourMainClass

Tomcat example:

        export CATALINA_OPTS="-Djcd.instance=hudson -Djcd.dest=udp://10.1.0.102 -Djcd.tmpl=javalang,tomcat -javaagent:collectd.jar"
        ./bin/catalina.sh start

#### Configuration

The jcd.* properties can be defined as System properties or in the
jcd.properties file:

* jcd.dest - UDP destination(s), the default is
  **239.192.74.66:25826**.  Example:

        jcd.dest=udp://localhost:25826

* jcd.tmpl - jcollectd.xml MBean filter templates (see etc/).  Example:

        jcd.tmpl=javalang,tomcat

* jcd.host - The collectd host (Hostname in collectd.conf) defaults to InetAddress.getLocalHost().getHostName().  Example:

        jcd.host=myhost

* jcd.instance - The collectd Plugin Instance, defaults to
  *java.lang:type=Runtime:Name*.  Example:

        jcd.instance=tomcat01

* jcd.beans - ObjectName(s) which are not defined in a filter
  template.  The ObjectName can be a pattern or fully qualified.
  Multiple ObjectNames are delimited using `#`.  All attributes
  for the matching MBeans will be collected where the attribute value
  is a *Number*.  Example:
  
        jcd.beans=mydomain:*#foo:type=Stats#bar:type=App,*

* jcd.sendinterval - Interval in seconds between each send, default of 60 seconds.  Example:
	
        jcd.properties=60

* jcd.properties - Path to the jcd.properties file which can be used
  to define all of the above properties.  Example:

        jcd.properties=/path/to/my-jcd.properties

#### XML MBean filters

The MBean filters provide a way to define exactly which MBean
attributes should be collected, how they should be named in collectd
and what type the metric is.  See the etc/ directory for examples.
The format is currently subject to change pending feedback and
documentation will follow.

### Download

* [git repo](http://github.com/hyperic/jcollectd)

* [tarball](http://support.hyperic.com/download/attachments/60621258/jcollectd-0.1.0.tar.gz)

### See Also

* [collectd](http://collectd.org) - the system statistics collection daemon

* [collectd jmx plugin](http://marc.info/?l=collectd&w=2&r=1&s=jmx&q=b) - Embeds a JVM in collectd

* [jmxetric](http://code.google.com/p/jmxetric/) - JMX injector for Ganglia

### About

Copyright (c) 2009 [Hyperic, Inc.](http://www.hyperic.com/) - See LICENSE

For feedback and discussion, please use collectd's mailinglist: collectd at verplant.org
