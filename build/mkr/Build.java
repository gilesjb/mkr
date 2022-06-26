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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import mkr.build.Behavior;
import mkr.build.Logger;
import mkr.build.controller.BuildEvents;
import mkr.build.controller.Loggers;
import mkr.build.controller.Parameterized;
import mkr.build.controller.Runner;

/**
 * The base class for build scripts.
 * 
 *
 * @author gilesjb
 */
public class Build {
	
	/**
	 * Creates an instance of the build's controller.
	 * Build classes with specialized controllers override this method
	 * @return a <code>Behavior</code> that is the controller for this build
	 */
	public Behavior createController() {
		return new Controller();
	}
	
	private static Behavior mainController = null;

	/**
	 * Retrieves the specified interface of the global build controller,
	 * calling <code>createController</code> to create the global
	 * controller if it does not already exist.
	 * 
	 * @param <X>
	 * @param type the desired interface or class of the global controller
	 * @return a Behavior of the desired class or interface
	 * @throws ClassCastException if the global controller does not contain
	 * an implementation of the required interface
	 */
	public final <X> X using(Class<X> type) throws ClassCastException {
		if (mainController == null) {
			mainController = createController();
		}
		return mainController.as(type);
	}
	
	private static final Logger error = new Logger() {
		protected void write(String msg) {
			System.err.println("ERROR: " + msg);
		}
	};
	
	private static final Logger warn = new Logger() {
		protected void write(String msg) {
			System.err.println("WARNING: " + msg);
		}
	};
	
	private static final Logger info = new Logger() {
		protected void write(String msg) {
			System.out.println(msg);
		}
	};
	
	private static final Logger verbose = new Logger() {
		protected void write(String msg) {
			System.out.println('(' + msg + ')');
		}
	}.enabled(false);

	private static class Controller extends Behavior implements Runner, Runner.Callback, BuildEvents, Parameterized, Loggers {

		public void run(Object build, Runner.Callback exec, String... args) {
			try {
				exec.invoke(build, args);
			} catch (Exception e) {
				as(Loggers.class).error().log("Build failed: " + build.getClass() + ": " + e.getMessage());
				if (as(Loggers.class).verbose().enabled()) {
					e.printStackTrace();
				}
			}
		}
		
		public void invoke(Object build, String[] args) throws Exception {
			as(BuildEvents.class).buildStarting(build);
			
			List<String> params = new LinkedList<String>(Arrays.asList(args));
			int wanted = 1;
			while (!params.isEmpty()) {
				if (params.size() < wanted) {
					throw new IllegalArgumentException("Option " + params.get(0) + 
							" requires " + (wanted - 1) + " parameters");
				}
				List<String> sub = params.subList(0, wanted);
				as(Loggers.class).verbose().log("Processing parameters: " + sub);
				wanted = as(Parameterized.class).parameters(build, sub.toArray(new String[0]));
				if (wanted == 0) {
					sub.clear();
					wanted = 1;
				}
			}

			as(BuildEvents.class).buildFinished(build);
		}
		
		public void buildStarting(Object build) {
			as(Loggers.class).info().log("Build class: " + build.getClass().getCanonicalName());
		}

		public int parameters(Object build, String[] params) {
			if ("-verbose".equals(params[0])) {
				as(Loggers.class).verbose().enabled(true).log("Verbose enabled");
			} else {
				throw new IllegalArgumentException("Unknown parameter: " + params[0]);
			}
			return 0;
		}
		
		public void buildFinished(Object build) {
			as(Loggers.class).info().log("Build complete");
		}

		public Logger error() {
			return error;
		}
		
		public Logger warn() {
			return warn;
		}

		public Logger info() {
			return info;
		}

		public Logger verbose() {
			return verbose;
		}
	}
	
	/**
	 * The build directory
	 */
	public static final String BUILD_DIR = System.getProperty("make.java.dir");

	@Override public String toString() {
		return getClass().getCanonicalName() + " build object";
	}
	
//	public static void addPath(String path) {
//		try {
//			Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
//			addUrl.setAccessible(true);
//			addUrl.invoke(ClassLoader.getSystemClassLoader(), new File(path).toURI().toURL());
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Class<? extends Build> mainClass() throws Exception {
		Field classes = ClassLoader.class.getDeclaredField("classes");
		classes.setAccessible(true);
		Class<? extends Build> main = null;
		Vector<Class<?>> loaded = (Vector<Class<?>>) classes.get(ClassLoader.getSystemClassLoader());
		for (Class<?> cl : loaded) {
			if (Build.class.isAssignableFrom(cl)) {
				main = (Class) cl;
			} else if (main != null) {
				return main;
			}
		}
		return main;
	}
	
	public static void start(Class<? extends Build> type, String[] args) throws Exception {
		Build build = type.newInstance();
		build.using(Runner.class).run(build, build.using(Build.Controller.class), args);
	}

	/**
	 * This main is inherited by derived classes.
	 * It determines which class was specified on the command line,
	 * then launches a build using that class
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		start(mainClass(), args);
	}
}
