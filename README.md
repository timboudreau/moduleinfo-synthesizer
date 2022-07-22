moduleinfo-synthesizer
======================

Extends the [smart-jar-merge](https://github.com/timboudreau/giulius/tree/master/smart-jar-merge) command-line
utility, and by extension, the [maven-merge-configuration plugin](https://github.com/timboudreau/giulius/tree/master/maven-merge-configuration)
with the ability to coalesce and/or synthesize a `module-info.class` when generating fat-jars from modular dependencies, non-modular dependencies,
or a mix of both.

Features
--------

  * Intercepts module-info.class files from jars being combined, decompiles and reconstructs their contents
  * Detects java packages in _non_-modular jars and includes them in the generated module-infos exports/opens set
    (if you are not building an "open" module, and using strong encapsulation, you will need `opens` entries in order
    for consumers of your combined jar to read those packages)
  * Detects META-INF/services entries in dependencies, coalesces them, and generates `provides` entries for them in
    the generated module-info
  * Optionally, will rewrite the `pom.xml` that gets installed into your local repository (and potentially deployed
    elsewhere) to disinclude those dependencies that have been merged:
    * Do not set `classifier` (you are not attaching a secondary artifact)
    * Do not set `jarName` (i.e. you want to overwrite the project's original JAR output)
    * *Do* set `replacePom` to true

All that is needed to use it via Maven is to use the `maven-merge-configuration` plugin, and add this project in a
`<dependencies>` entry _for the plugin itself_, and then configure to taste.

Usage
-----

The most straightforward way is via the merge configuration maven plugin - the
following three properties may be added to `<extensionProperties>` in its configuration,
along with adding this project as a plugin dependency:

  * `moduleName` - spell out the module name you want it to put in the module declaration
  * `openModule` - if true, declare the module as `open` - if false, `opens` entries from
    any modular jars you include will be used, and entries will by synthesized for every
    package in any non-modular jars you are combining with them
  * `checkServiceConstructors` - we generate `provides` entries from `META-INF/services`
    service registration files in the JARs we scan.  However, some projects (hadoop, for one)
    include service registration files for types that do not have a default constructor
    and cannot be loaded from the JDK's `ServiceLoader` (they likely use their own mechanism
    for loading them).  `provides` entries for such classes will be uncompilable.  If this
    property is set to true, we will examine the bytecode of each service implementation
    being registered (assuming it exists) and omit any that do not have a public, no-arg
    constructor - at the price of slowing down jar-merging slightly.

Note:  _If you are trying to merge a bunch of non-modular JARs and turn them into a module,
there needs to be at least _one_ `module-info.class` present for generation to happen
at all - assuming you have a java project that is declaring all the dependencies you want
merged, just add an empty `module-info.java` in the source root of that to get a modular
jar for output.

```xml
            <plugin>
                <groupId>com.mastfrog</groupId>
                <artifactId>maven-merge-configuration</artifactId>
                <version>2.8.3.1</version>
                <dependencies>
                    <dependency>
                        <groupId>com.mastfrog</groupId>
                        <artifactId>jarmerge-moduleinfo-synthesizer</artifactId>
                        <version>1.0</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <!--<jarName>combined-combine</jarName>-->
                    <classifier></classifier>
                    <normalizeMetaInfPropertiesFiles>true</normalizeMetaInfPropertiesFiles>
                    <mainClass>com.x.y.combinethings.CombineThings</mainClass>
                    <skipLicenseFiles>true</skipLicenseFiles>
                    <skipMavenMetadata>true</skipMavenMetadata>
                    <replacePom>true</replacePom>
                    <extensionProperties>
                        <moduleName>org.moo.bwerg</moduleName>
                        <openModule>false</openModule>
                        <checkServiceConstructors>true</checkServiceConstructors>
                    </extensionProperties>
                    <manifestEntries>
                        <Wurble-Snacks>Snugwuzzit</Wurble-Snacks>
                    </manifestEntries>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>merge-configuration</goal>
                        </goals>
                        <phase>package</phase>
                    </execution>
                </executions>
            </plugin>
```

Motivation
----------

Managing non-modular dependencies within modular Maven projects is a source of frequent pain; in
particular, there are cases (hello, Hadoop) where things are almost impossible to build - while
the shade plugin can help with some of it, it does not help with all of the issues that come up
(particularly non-modular jars with no Automatic-Module-Name which contain strings like `_3_7` in
their name, which break the JDK's algorithm for synthesizing file based module names completely).

Being able to simply _make_ a modular fat-jar _and_ actually have it not insist that such
pathological dependencies are still part of its classpath is essential for being able to run things
like Javadoc aggregation across a large build with such dependencies.


Under The Hood
==============

What this does is fairly simple, and `smart-jar-merge` does most of the heavy lifting.  What we do
internally is:

  * Claim any `module-info.class` files encountered while dependency jars are being scanned, and provide
    a `Coalescer` for them which will handle writing the destination file (using NetBeans classfile library
    which has much more complete and mature support for reading module files than BCEL)
  * Claim any files with JAR entries starting with `META-INF/services` (these files will still be present,
    and will be coalesced using smart-jar-merge's own functionality - we just note them so we can also
    create `provides` entries)
  * On completion
    * We coalesce all the information gathered, and generate a `module-info.java` source file into a temporary folder
    * We generate enough of a package structure on disk for the compiler not to complain about exporting packages that aren't there
    * We invoke `javac` programmatically using the `jdk.compiler` API to compile a class file from our `module-info.java` file
    * And include the resulting `module-info.class` file in the merged JAR

Caveats
-------

One thing that is _not_ currently handled is generating `uses` declarations from calls to `ServiceLoader` in _non-modular JARs_,
which would require bytecode-scanning, and, well, some flow-analysis and guesswork to do.  It is _possible_ with a hefty
margin of error, but is not implemented currently.
