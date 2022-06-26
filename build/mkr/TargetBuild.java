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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mkr.build.Behavior;
import mkr.build.Logger;
import mkr.build.controller.BuildTarget;
import mkr.build.controller.Loggers;
import mkr.build.controller.Parameterized;
import mkr.build.controller.Runner;
import mkr.build.controller.TargetEvents;

/**
 * Build container with a controller that treats parameters as target names.
 * A functional controller stack derived from this must implement the Targets interface.
 * 
 * @author gilesjb
 */
public abstract class TargetBuild extends Build {
	
	public abstract Map<String, BuildTarget> getTargets();
	
	@Override public Behavior createController() {
		return new Controller().overrides(super.createController());
	}
	
	public static final String DEFAULT_TARGET = "<root>";
	
	private Map<String, BuildTarget> targets = null;
	private final Set<String> called = new HashSet<String>();
	
	@SuppressWarnings("serial")
	public static class MissingTargetException extends RuntimeException {
		MissingTargetException(String name) {
			super("Target '" + name + "' not found");
		}
	}
	
	private Map<String, BuildTarget> targets() {
		if (targets == null) {
			targets = getTargets();
			if (!targets.containsKey(DEFAULT_TARGET)) {
				targets.put(DEFAULT_TARGET, new BuildTarget() {
					public String[] dependencies() {
						return new String[] {};
					}
					
					public void execute() {
						showTargets();
					}
					
					public String name() {
						return DEFAULT_TARGET;
					}
				});
			}
			final Logger errors = using(Loggers.class).error();
			for (BuildTarget target : targets.values()) {
				for (String pre : target.dependencies()) {
					if (!targets.containsKey(pre)) {
						errors.log("Missing dependency (" + pre + ") for target: " + target.name());
					}
				}
			}
			if (errors.used()) throw new RuntimeException("Missing dependency errors");
		}
		return targets;
	}

	/**
	 * Builds the specified target and its dependencies
	 * @param name the name of the target
	 * @throws MissingTargetException
	 * @throws Exception
	 */
	public void buildTarget(String name) throws MissingTargetException {
		using(Loggers.class).verbose().log("Building target: " + name);
		BuildTarget target = targets().get(name);
		if (target == null) throw new MissingTargetException(name);
		
		called.add(name);
		for (String pre : target.dependencies()) {
			if (!called.contains(pre)) buildTarget(pre);
		}
		
		using(TargetEvents.class).targetStarting(target);
		try {
			target.execute();
		} finally {
			using(TargetEvents.class).targetFinished(target);
		}
	}
	
	private void showTargets() {
		using(Loggers.class).info().log("Available targets:");
		for (BuildTarget target : targets().values()) {
			using(Loggers.class).info().log("  " + toString(target));
		}
	}

	private String toString(BuildTarget target) {
		return target.name();
	}

	private static class Controller extends Behavior implements Runner, Parameterized, TargetEvents {
		
		public void run(Object build, Runner.Callback callback, String... args) {
			parent(Runner.class).run(build, callback, args.length == 0? new String[] {DEFAULT_TARGET} : args);
		}
		
		public int parameters(Object build, String[] params) throws Exception {
			TargetBuild tb = (TargetBuild) build;
			if (params[0].equals("-targets")) {
				tb.showTargets();
			} else if (!params[0].startsWith("-")) {
				tb.buildTarget(params[0]);
			} else {
				return parent(Parameterized.class).parameters(build, params);
			}
			return 0;
		}
		
		public void targetStarting(BuildTarget target) {
			as(Loggers.class).info().log("Starting target: " + target.name());
		}
		
		public void targetFinished(BuildTarget target) {}
	}
}
