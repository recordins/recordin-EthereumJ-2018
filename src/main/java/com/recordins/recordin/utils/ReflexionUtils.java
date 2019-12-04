/*
 * Record'in
 *
 * Copyright (C) 2019 Blockchain Record'in Solutions
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.recordins.recordin.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cheetah.webserver.CheetahClassLoader;

import static org.cheetah.webserver.RessourceFinder.getJarURLs;

import org.cheetah.webserver.CheetahWebserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflexionUtils {

    private static Logger logger = LoggerFactory.getLogger(ReflexionUtils.class);

    public static List<Class<?>> getClassesFromPackage(String pckgname, CheetahClassLoader cl) {
        logger.trace("START getClassesFromPackage(String, URLClassLoader)");

        ArrayList<Class<?>> result = new ArrayList();
        ArrayList<File> files = new ArrayList();
        HashMap<File, String> packageNames = null;

        try {

            for (URL jarURL : getJarURLs(cl)) {

//                logger.trace("JAR in classpath CLASS: " + jarURL);
                getClassesInSamePackageFromJar(result, pckgname, jarURL.getPath(), cl);
                String path = pckgname;
                Enumeration<URL> resources = cl.getResources(path);
                File file = null;
                while (resources.hasMoreElements()) {
                    String path2 = resources.nextElement().getPath();
                    file = new File(URLDecoder.decode(path2, "UTF-8"));
                    files.add(file);
                }
                if (packageNames == null) {
                    packageNames = new HashMap<File, String>();
                }
                packageNames.put(file, pckgname);
            }
        } catch (NullPointerException x) {
            // throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            // throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            // throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }

        for (File file : files) {
            if (file.exists()) {

                String[] filesPath = file.list();
                for (String filePath : filesPath) {
                    if (filePath.endsWith(".class")) {
                        try {
                            //result.add(Class.forName(packageNames.get(file) + '.' + filePath.substring(0, filePath.length() - 6)));

                            result.add(cl.loadClass(packageNames.get(file) + '.' + filePath.substring(0, filePath.length() - 6)));
                        } catch (Throwable e) {
                        }
                    }
                }
            }
        }

        logger.trace("END getClassesFromPackage()");
        return result;
    }

    /**
     * Returns the list of classes in the same directories as Classes in
     * <code>classes</code>.
     *
     * @param result
     * @param classes
     * @param jarPath
     */
    public static void getClassesInSamePackageFromJar(List<Class<?>> result, String packageName, String jarPath, CheetahClassLoader cl) {
        logger.trace("START getClassesInSamePackageFromJar(List, String, String, URLClassLoader)");

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);

            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements()) {
                try {
                    JarEntry entry = en.nextElement();
                    String entryName = entry.getName();

                    packageName = packageName.replace('.', '/');

//                    logger.debug("entryName: " + entryName + " JAR: " + jarPath);
                    if (entryName != null && entryName.endsWith(".class") && entryName.startsWith(packageName) && !entryName.substring(packageName.length() + 1).contains("/")) {

//                    logger.debug("entryName sub: " + entryName.substring(packageName.length() + 1));
                        try {
                            Class<?> entryClass = cl.loadClassPlugin(entryName.substring(0, entryName.length() - 6).replace('/', '.'));
                            if (entryClass != null) {
                                result.add(entryClass);
                            }
                        } catch (Throwable e) {
                            logger.debug("Error instanciating: " + entryName + " " + e.toString());
                        }
                    }
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {

        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (Exception e) {
            }
        }

        logger.trace("END getClassesInSamePackageFromJar()");
    }

}
