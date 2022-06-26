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

import mkr.build.Behavior;
import mkr.build.controller.BuildEvents;
import mkr.build.controller.BuildTarget;
import mkr.build.controller.Loggers;
import mkr.build.controller.Runner;
import mkr.build.controller.TargetEvents;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.copalis.antsy.AntProject;
import org.copalis.antsy.AntTarget;
import org.copalis.antsy.AntTask;
import org.copalis.antsy.Tasks;

/**
 * A parent class for builds that use Ant task facades
 *
 * @author gilesjb
 */
public abstract class AntBuild extends MethodTargetBuild implements Tasks {
	
	@Override public Behavior createController() {
		return new Controller().overrides(super.createController());
	}

	private static final AntProject ant = new AntProject().setStreams();
	private static final Dir baseDir = Dir.valueOf(ant.project().getBaseDir());
	
	/**
	 * Gets the Ant Project
	 * @return the project
	 */
	public static Project project() {
		return ant.project();
	}
	
	/**
	 * Return the base directory of the project
	 * @return the project base directory,
	 * or null if the base directory has not been successfully set to a valid value. 
	 */
	public static Dir baseDir() {
		return baseDir;
	}
	
	/**
	 * Returns a Dir object
	 * @param name
	 * @return
	 */
	public static Dir dir(String name) {
		return baseDir().subDir(name);
	}
	
	/**
	 * Creates a new instance of the specified Ant task facade,
	 * with location inference enabled.
	 * 
	 * @param <X> the facade type
	 * @param <Y> the inner Ant task type
	 * @param type the class object for X
	 * @return a new instance of X
	 */
	public <X extends AntTask<Y>, Y extends Task> X task(Class<X> type) {
		X task = ant.task(type);
		task.inferLocation(true);
		return task;
	}

	/**
	 * Controller for Ant build.
	 * Sets up Ant <code>Project</code> and <code>BuildLogger</code>,
	 * and fires appropriate events
	 *
	 */
	private static class Controller extends Behavior implements Runner, BuildEvents, TargetEvents {
		
		AntTarget current;
		
		public void run(Object build, final Runner.Callback callback, String... args) {
			parent(Runner.class).run(build, new Runner.Callback() {
				public void invoke(Object build, String[] args) throws Exception {
					try {
						callback.invoke(build, args);
					} catch (BuildException e) {
						ant.buildFinished(e);
					} catch (MissingTargetException e) {
						ant.buildFinished(e.getMessage(), e);
					} catch (Throwable t) {
						if (as(Loggers.class).verbose().enabled()) {
							ant.buildFinished(t);
						} else {
							ant.buildFinished(t.getMessage(), t);
						}
					}
				}
			}, args);
		}

		public void targetStarting(BuildTarget bt) {
			current = ant.startTarget(bt.name());
		}

		public void targetFinished(BuildTarget bt) {
			current.finished();
		}
		
		public void buildStarting(Object build) {
			ant.startBuild();
		}

		public void buildFinished(Object build) {
			ant.buildFinished();
		}
	}
}
