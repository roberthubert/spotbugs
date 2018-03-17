/*
 * Contributions to SpotBugs
 * Copyright (C) 2018, Misa
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
package edu.umd.cs.findbugs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PluginFileHandleLeakTest {

    public static void main(String[] args) throws Exception {
        Path sourcePluginPath = Paths.get("src/test/java/edu/umd/cs/findbugs/fb-contrib.jar");
        Path pluginPath = Files.createTempFile("fb-contrib", ".jar");
        Files.copy(sourcePluginPath, pluginPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Plugin copied to file: " + pluginPath.toString());

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Plugin plugin = Plugin.addCustomPlugin(pluginPath.toUri(), contextClassLoader);
        System.out.println("Custom plugin added: " + plugin);

        Plugin.removeCustomPlugin(plugin);
        System.out.println("Custom plugin removed: " + plugin);

        Files.delete(pluginPath);

        // java.net.JarURLConnection
    }

}
