/*
 *  Copyright 2009 Giles Burgess
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
package mkr;

import java.util.List;

/**
 * Static methods for invoking JDK tools
 *
 * @author gilesjb
 */
public class Jdk {
	
	public static void javac(List<String> args) {
		javac(toArray(args));
	}
	
	public static void javac(String... args) {
		int result = com.sun.tools.javac.Main.compile(args);
		if (result != 0) throw new IllegalArgumentException("javac failed, code: " + result);
	}
	
	public static void javadoc(List<String> args) {
		javadoc(toArray(args));
	}
	
	public static void javadoc(String... args) {
		int result = com.sun.tools.javadoc.Main.execute(args);
		if (result != 0) throw new IllegalArgumentException("javadoc failed, code: " + result);
	}
	
	public static void jar(List<String> args) {
		jar(toArray(args));
	}
	
	public static void jar(String... args) {
		if (!new sun.tools.jar.Main(System.out, System.err, "jar").run(args))
			throw new IllegalArgumentException("jar failed");
	}
	
	private static String[] toArray(List<String> list) {
		return list.toArray(new String[0]);
	}
}
