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

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A derived File class that adds methods for creating child files and directories.
 * It also supports the {@code Iterable<File>} interface.
 * The iterator returns a depth-first search of all files and directories,
 * including itself,
 * with directories represented as {@link mkr.Dir} objects.
 *
 * @author gilesjb
 */
public class Dir extends File {
	private static final long serialVersionUID = 1L;
	
	private Dir(File parent, String child) {
		super(parent, child);
	}

	private Dir(String pathname) {
		super(pathname);
	}
	
	/**
	 * Returns a <code>Dir</code> instance
	 * with the specified path
	 * 
	 * @param pathname
	 * @return a Dir instance
	 */
	public static Dir valueOf(String pathname) {
		return new Dir(pathname);
	}
	
	/**
	 * Returns a <code>Dir</code> instance
	 * with the same pathname as the specified <code>File</code>
	 * 
	 * @param file
	 * @return a Dir instance
	 */
	public static Dir valueOf(File file) {
		return new Dir(file.getPath());
	}

	/**
	 * Returns a child <code>Dir</code> instance
	 * that has this <code>Dir</code> as its parent directory
	 * 
	 * @param name The subdirectory name
	 * @return a Dir instance
	 */
	public Dir subDir(String name) {
		return new Dir(this, name);
	}
	
	/**
	 * Returns a child <code>File</code> instance
	 * that has this <code>Dir</code> as its parent directory
	 * 
	 * @param name The child file name
	 * @return A File instance
	 */
	public File file(String name) {
		return new File(this, name);
	}
	
	/**
	 * Accepts a set of paths,
	 * and adds this Dir's path to each one
	 * 
	 * @param txt One or more paths delimited with commas
	 * @return The input paths prepended with this Dir's path
	 */
	public String include(String txt) {
		return getPath() + separator + txt.replaceAll(",", "," + getPath() + separator);
	}
	
	public interface TraversalOrder {
		int expandAt(int end);
	}

	/**
	 * A traversal order that visits a parent directory after depth-first traversal of the children
	 */
	public static final TraversalOrder DEPTH_FIRST = new TraversalOrder() {
		public int expandAt(int end) {return 0;}
	};
	
	/**
	 * A traversal order that visits a parent directory before depth-first traversal of the children
	 */
	public static final TraversalOrder PARENT_FIRST = new TraversalOrder() {
		public int expandAt(int end) {return 1;}
	};
		
	/**
	 * A breadth-first traversal order that does not visit files and directories at depth n
	 * until all those at depths x&lt;n have been visited
	 */
	public static final TraversalOrder BREADTH_FIRST = new TraversalOrder() {
		public int expandAt(int end) {return end;}
	};
	
	/**
	 * An Iterable that traverses this Dir and all children in the specified order
	 * @param order the traversal order
	 * @return a new File Iterator
	 */
	public Iterable<File> children(final TraversalOrder order) {
		return new Iterable<File>() {
			
			public Iterator<File> iterator() {
				
				return new Iterator<File>() {
					final LinkedList<File> agenda = new LinkedList<File>();

					{schedule(Dir.this);}
					
					public boolean hasNext() {
						while (!agenda.isEmpty()) {
							File head = agenda.peekFirst();
							if (head instanceof Dir || !head.isDirectory()) return true;
							
							schedule(valueOf(next()));
						}
						return false;
					}
					
					public File next() {
						return agenda.removeFirst();
					}
					
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					private void schedule(Dir dir) {
						agenda.addFirst(dir);
						agenda.addAll(order.expandAt(agenda.size()), Arrays.asList(dir.listFiles()));
					}
				};
			}
		};
	}
	
	/**
	 * Recursively deletes this Dir and all children
	 */
	public void deleteAll() {
		for (File file : children(DEPTH_FIRST)) file.delete();
	}
	
//	public static void main(String... args) {
//		for (File file : Dir.valueOf("target").children(PARENT_FIRST)) {
//			System.out.format("%2d %-4s %s\n",
//					file.getAbsolutePath().split("/").length,
//					file.getClass().getSimpleName(),
//					file.getAbsolutePath());
//		}
//	}
}

