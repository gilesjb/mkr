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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import mkr.build.controller.BuildTarget;
import mkr.build.controller.Loggers;

/**
 * A build container with a controller that generates targets from methods
 * whose names start 'make', eg
 * <pre>
 * void makeJars() { ... }
 * </pre>
 * builds the target 'jars'.
 * <p/>
 * The method for the default (unnamed) target is called just 'make'
 *
 * @author gilesjb
 */
public class MethodTargetBuild extends TargetBuild {
	
	/**
	 * Target method annotation that indicates the names of dependency targets.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Depends {
		/**
		 * The names of dependency targets
		 * @return target names separated with commas
		 */
		String value();
	}

	static final String PREFIX = "make";
	
	private BuildTarget targetMethod(final Method method) {
		return new BuildTarget() {
			
			public String name() {
				String name = method.getName().substring(PREFIX.length());
				return name.length() == 0?
						TargetBuild.DEFAULT_TARGET :
						name.substring(0, 1).toLowerCase() + name.substring(1);
			}
			
			public String[] dependencies() {
				Depends depends = method.getAnnotation(Depends.class);
				return depends == null? new String[0] : depends.value().split(",");
			}
			
			public void execute() {
				try {
					method.setAccessible(true);
					try {
						method.invoke(MethodTargetBuild.this);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				} catch (RuntimeException i) {
					throw i;
				} catch (Throwable i) {
					throw new RuntimeException(i);
				}
			}
		};
	}
	
	@Override public Map<String, BuildTarget> getTargets() {
		Map<String, BuildTarget> targets = new HashMap<String, BuildTarget>();
		
		final Loggers loggers = using(Loggers.class);
		for (Class<?> type = this.getClass(); type != null; type = type.getSuperclass()) {
			for (Method method : type.getDeclaredMethods()) {
				if (method.getName().startsWith(PREFIX)) {
					if (method.getReturnType() != void.class || method.getParameterTypes().length != 0) {
						loggers.error().log("Illegal target method: " + method);
					} else {
						final BuildTarget tm = targetMethod(method);
						loggers.verbose().log(
								(targets.put(tm.name(), tm) == null? "Found" : "Overriding") + " target: " + method);
					}
				}
			}
		}
		if (loggers.error().used()) throw new RuntimeException("Illegal target method errors");
		return targets;
	}
}
