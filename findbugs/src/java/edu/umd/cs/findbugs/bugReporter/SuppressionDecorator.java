/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.bugReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.PluginLoader;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;

/**
 * @author pugh
 */
public class SuppressionDecorator extends BugReporterDecorator {

	final String category;

	final HashSet<String> check = new HashSet<String>();

	final HashSet<String> dontCheck = new HashSet<String>();

	public SuppressionDecorator(BugReporterPlugin plugin, BugReporter delegate) {
		super(plugin, delegate);
		category = plugin.getProperties().getProperty("category");
		if (I18N.instance().getBugCategory(category) == null)
			throw new IllegalArgumentException("Unable to find category " + category);
		final String adjustmentSource = plugin.getProperties().getProperty("packageSource");
		String packageList = plugin.getProperties().getProperty("packageList");

		try {
			if (packageList != null) {
				processPackageList(new StringReader(packageList));
			}
			if (adjustmentSource != null) {
				URL u;

				if (adjustmentSource.startsWith("file:") || adjustmentSource.startsWith("http:")
				        || adjustmentSource.startsWith("https:"))
					u = new URL(adjustmentSource);
				else {
					u = plugin.getPlugin().getPluginLoader().getResource(adjustmentSource);
					if (u == null)
						u = DetectorFactoryCollection.getCoreResource(adjustmentSource);

				}
				if (u != null) {
					InputStreamReader rawIn = new InputStreamReader(u.openStream(), "UTF-8");
					processPackageList(rawIn);
				}

			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load " + category + " filters from " + adjustmentSource, e);
		}
	}

	/**
	 * @param rawIn
	 * @throws IOException
	 */
	private void processPackageList(Reader rawIn) throws IOException {
		BufferedReader in = new BufferedReader(rawIn);
		while (true) {
			String s = in.readLine();
			if (s == null)
				break;
			s = s.trim();
			if (s.length() == 0)
				continue;
			String packageName = s.substring(1).trim();
			if (s.charAt(0) == '+') {
				check.add(packageName);
				dontCheck.remove(packageName);
			} else if (s.charAt(0) == '-') {
				dontCheck.add(packageName);
				check.remove(packageName);
			} else
				throw new IllegalArgumentException("Can't parse " + category + " filter line: " + s);
		}
	}

	@Override
	public void reportBug(BugInstance bugInstance) {

		if (!category.equals(bugInstance.getBugPattern().getCategory())) {
			getDelegate().reportBug(bugInstance);
			return;
		}
		if (check.isEmpty())
			return;

		ClassAnnotation c = bugInstance.getPrimaryClass();
		@DottedClassName
		String packageName = c.getPackageName();

		while (true) {
			if (check.contains(packageName)) {
				getDelegate().reportBug(bugInstance);
				return;
			} else if (dontCheck.contains(packageName)) {
				return;
			}
			int i = packageName.lastIndexOf('.');
			if (i < 0)
				return;
			packageName = packageName.substring(0, i);
		}

	}

}