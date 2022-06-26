/*
 *  Copyright 2009,2010,2011 Giles Burgess
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


/**
 * Packages the mkr distribution.
 * The build targets are: 
 * <dl>
 * <dt>compile</dt><dd>Compiles the java files in build</dd>
 * <dt>jar</dt><dd>Builds mkr.jar file</dd>
 * <dt>docs</dt><dd>Creates javadocs</dd>
 * <dt>dist</dt><dd>Creates a distribution</dd>
 * <dt>tidy</dt><dd>Deletes temporary files</dd>
 * <dt>clean</dt><dd>Deletes all targets</dd>
 * </dl>
 * 
 * @author gilesjb
 */
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
			.beginSrc()
				.location(build).end()
			.beginClasspath()
				.beginFileset()
					.dir(build)
					.includes("ant*.jar")
					.end()
				.end()
			.run();
	}
	
	@Depends("compile") void
	makeJar() {
		task(mkdir).dir(targetJar).run();
		task(jar)
			.destFile(targetJar.file(String.format("mkr-%s.jar", mkrVer)))
			.beginFileset()
				.dir(build)
				.excludes("*.java")
				.excludes("**.class")
				.end()
			.beginFileset()
				.dir(targetClass)
				.excludes("*.class")
				.end()
			.run();
	}
	
	void
	makeDistDir() {
		task(mkdir).dir(targetDist).run();
	}
	
	@Depends("jar,docs") void
	makeDist() {
		task(copy)
			.todir(targetDist)
			.beginFileset()
				.dir(baseDir())
				.includes("mkr*")
				.includes("*.txt")
				.includes("build/*.jar")
				.end()
			.run();
		task(copy)
			.todir(targetDist.subDir("build"))
			.beginFileset()
				.dir(targetJar)
				.end()
			.run();
		task(zip)
			.destFile(target.file(String.format("mkr-%s.dist.zip", mkrVer)))
			.beginFileset()
				.dir(targetDist)
				.end()
			.run();
	}
	
	@Depends("distDir") void
	makeDocs() {
		task(javadoc)
			.destdir(targetDocs).packagenames("mkr.*").failonerror(true)
			.beginFileset()
				.dir(build)
				.includes("**/*.java")
				.end()
			.run();
	}
	
	void
	makeTidy() {
		task(delete)
			.includeEmptyDirs(true)
			.beginFileset().dir(targetClass).end()
			.beginFileset().dir(targetJar).end()
			.run();
	}
	
	void
	makeClean() {
		task(delete).dir(target).run();
	}
}
