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
package mkr.build;

import mkr.Build;


/**
 * Static methods for launching a build
 *
 * @author gilesjb
 */
public class Launcher {
	
	/**
	 * Entry point for running inside contexts that don't support inherited main method;
	 * Expects the first argument to be the name of the build class
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing parameter: build-class name");
		}
		String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, 1, newArgs, 0, args.length - 1);
		Build.start(Class.forName(args[0], true, ClassLoader.getSystemClassLoader()).asSubclass(Build.class), newArgs);
	}
}
