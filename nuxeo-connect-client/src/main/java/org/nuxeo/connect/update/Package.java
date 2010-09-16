/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.connect.update;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface Package {

    /**
     * The package unique id. This is composed by the package name and version:
     * <code>name-version</code>.
     */
    String getId();

    /**
     * The package name.
     */
    String getName();

    /**
     * The package title.
     */
    String getTitle();

    /**
     * The package short description.
     */
    String getDescription();

    /**
     * The package type: addon, hotfix, etc.
     */
    PackageType getType(); // TODO use enum

    /**
     * Get the package vendor ID.
     */
    String getVendor();

    /**
     * The package version.
     */
    Version getVersion();

    /**
     * The list of platforms that supports this package.
     */
    String[] getTargetPlatforms();

    /**
     * Gets the list of dependencies of this package. If not dependencies exists,
     * either null or an empty array is returned.
     */
    PackageDependency[] getDependencies();

    /**
     * Gets the package life cycle status.
     */
    int getState();

    /**
     * Get the URL where more information can be found about this package. Can
     * be null.
     */
    String getHomePage();

    /**
     * Gets the license type. Examples: GPL, BSD, LGPL, etc.
     */
    String getLicenseType();

    /**
     * Gets an URL for the license. Return null if no URL exists. In that case
     * the license content must be included in the package as the content of the
     * license.txt file.
     */
    String getLicenseUrl();

    /**
     * Gets the package classifier: open source etc ?
     */
    String getClassifier(); // TODO use enum

    /**
     * Tests whether this package is local or a remote one. A local package has
     * a package data attached.
     */
    boolean isLocal();

}