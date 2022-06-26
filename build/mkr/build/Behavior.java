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
 * A base class for constructing behavior objects that can be composed
 * at run-time into a stack of method handlers where handlers at the top
 * of the stack inherit methods from lower handlers
 * <p>
 * Any methods that a derived class needs to make available
 * must be declared in an interface implemented by the class
 * 
 * @author gilesjb
 */
public abstract class Behavior {

	private Behavior first = this, next = null;
	
	/**
	 * Overrides an existing Behavior
	 * @param parent
	 * @return
	 */
	public final Behavior overrides(Behavior parent) {
		this.next = parent;
		while (parent != null) {
			parent.first = this;
			parent = parent.next;
		}
		return this;
	}
	
	/**
	 * Finds the first Behavior in the stack that implements the requested interface
	 * @param <T>
	 * @param type The requested interface
	 * @return An instance of type
	 */
	public final <T> T as(Class<T> type) {
		return find(type, first);
	}
	
	/**
	 * Finds the first Behavior beneath the current one that implements the requested interface
	 * @param <T>
	 * @param type The requested interface
	 * @return An instance of type
	 */
	public final <T> T parent(Class<T> type) {
		return find(type, next);
	}
	
	private static final <T> T find(Class<T> type, Behavior handler) {
		while (handler != null) {
			if (type.isInstance(handler)) {
				return type.cast(handler);
			}
			handler = handler.next;
		}
		throw new ClassCastException("Behavior not implemented: " + type.getCanonicalName());
	}
}
