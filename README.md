## mkr

Does anyone still use Ant? I have no idea, but if Maven annoys you and would like to write Ant builds in Java rather than XML, you might be interested in MKR. It uses the fluent Ant task interfaces from ANTSY and adds a script that compiles and excutes your `Make.java` script automatically.

MKR employs some highly dubious coding practices (deep inheritance, inheritance of `main()`, default package) in order to avoid Java verbosity and keep scripts as short as possible. As a result, the following example requires no package declaration or imports.

### Example

This is the beginning of the build file for the MKR project:

	public class Make extends mkr.AntBuild {
	    mkr.Dir
	        build = dir("build"),
	        target = dir("target"),
	        targetClass = target.subDir("class"),
	        targetDist = target.subDir("dist"),
	        targetDocs = targetDist.subDir("docs"),
	        targetJar = target.subDir("jar");
	    String 
	        jdkVer = "1.5",
	        mkrVer = "0.9";
	    
	    /**
	     * Default target, builds distribution
	     */
	    @Depends("dist,tidy") void
	    make() {
	        task(echo).message("Completed default build").run();
	    }
	    
	    void
	    makeCompile() {
	        task(mkdir).dir(targetClass).run();
	        task(javac)
	            .destdir(targetClass).excludes("*.java").source(jdkVer).target(jdkVer)
	            .addSrc()
	                .location(build).end()
	            .addClasspath()
	                .addFileset()
	                    .dir(build)
	                    .includes("ant*.jar")
	                    .end()
	                .end()
	            .run();
	    }


Some notes:

* Targets are generate by `make` methods. The default target is executed by `make()`, and the 'compile' target is executed by `makeCompile()`
* `@Depends("target1, target2")` specify other targets that this one depends on
* Ant Task objects are created by the inherited `task()` factory method
* A Task is executed by calling its `run()` method
* Tasks may contain sub-elements; calling `end()` on a sub-element returns the parent element or target. Java's generics type system keeps track of the correct type of all the objects.

This code is packaged as

	build/Make.java

under the working directory, and is executed by typing

	$ mkr <targets>

at the command line.

Alternatively it can be run from within an IDE if `build` is included as a source folder.

(Eclipse also requires that "Include inherited mains when searching for a main class" is checked in _Run Configurations_)

### Under the hood

The `mkr` script will:

* Compile `build/Make.java` with `javac`
* Execute `java Make <user params>`
* Clean up the temporary class files

Any jar files placed in the `build` directory are included in the compilation and execution classpath.

The `java` runtime searches up through parent classes for a `main` method to execute. It finds `mkr.Build.main()`, which does the following:

* Creates an instance of `Make`
* Retrieves a controller object referenced by the static variable `CONTROLLER` inherited by `Make`
* Invokes `run()` on the controller, passing the make instance

The controller that is inherited from `AntBuilder` treats methods with names beginning `make` as targets, and executes targets specified by the command-line `<params>`.