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

/**
 * An abstract base class for loggers
 *
 * @author gilesjb
 */
public abstract class Logger {
	private int written = 0, used = 0;
	private boolean enabled = true;
	
	protected abstract void write(String msg);
	
	public final Logger enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
	
	public final boolean enabled() {
		return enabled;
	}
	
	public final int written() {
		return written;
	}
	
	public final Logger log(String msg) {
		used++;
		if (enabled) {
			write(msg);
			written++;
		}
		return this;
	}
	
	public boolean used() {
		return used > 0;
	}
}