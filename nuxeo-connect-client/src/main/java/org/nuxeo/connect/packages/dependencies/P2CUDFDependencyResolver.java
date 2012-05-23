/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;
import org.eclipse.equinox.p2.cudf.solver.SimplePlanner;
import org.eclipse.equinox.p2.cudf.solver.SolverConfiguration;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.InternalPackageManager;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.VersionRange;

/**
 * This implementation uses the p2cudf resolver to solve complex dependencies
 * 
 * @since 1.4
 */
public class P2CUDFDependencyResolver implements DependencyResolver {

    protected static Log log = LogFactory.getLog(P2CUDFDependencyResolver.class);

    protected InternalPackageManager pm;

    protected CUDFHelper cudfHelper;

    protected P2CUDFDependencyResolver() {
    }

    public P2CUDFDependencyResolver(InternalPackageManager pm) {
        this.pm = pm;
    }

    public DependencyResolution resolve(List<String> pkgInstall,
            List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform) throws DependencyException {
        cudfHelper = new CUDFHelper(pm);
        cudfHelper.setTargetPlatform(targetPlatform);
        // generate CUDF package universe and request stanza
        String cudf = cudfHelper.getCUDFFile(str2PkgDep(pkgInstall),
                str2PkgDep(pkgRemove), str2PkgDep(pkgUpgrade));
        log.debug("CUDF request:\n" + cudf);

        // pass to p2cudf for solving
        ProfileChangeRequest req = new Parser().parse(IOUtils.toInputStream(cudf));
        SolverConfiguration configuration = new SolverConfiguration(
                SolverConfiguration.OBJ_ALL_CRITERIA);
        if (log.isTraceEnabled()) {
            configuration.verbose = true;
            configuration.explain = true;
        }
        SimplePlanner planner = new SimplePlanner();
        planner.getSolutionFor(req, configuration);
        planner.stopSolver();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Hazardous wait for the solver stop
        }
        @SuppressWarnings("unchecked")
        Collection<InstallableUnit> solution = planner.getBestSolutionFoundSoFar();
        if (log.isTraceEnabled()) {
            log.trace(planner.getExplanation());
        }
        if (!planner.isSolutionOptimal()) {
            log.warn("The solution found might not be optimal");
        }
        DependencyResolution resolution = cudfHelper.buildResolution(solution,
                planner.getSolutionDetails());
        return resolution;
    }

    private PackageDependency[] str2PkgDep(List<String> pkgList) {
        List<PackageDependency> list = new ArrayList<PackageDependency>();
        if (pkgList == null || pkgList.size() == 0) {
            return list.toArray(new PackageDependency[0]);
        }
        List<DownloadablePackage> allPackages = pm.listAllPackages();
        for (String pkgStr : pkgList) {
            boolean isId = false;
            if (allPackages != null) {
                for (DownloadablePackage checkPkg : allPackages) {
                    if (checkPkg.getId().equals(pkgStr)) {
                        isId = true;
                        list.add(new PackageDependency(checkPkg.getName(),
                                checkPkg.getVersion(), checkPkg.getVersion()));
                        break;
                    }
                }
            }
            if (!isId) {
                list.add(new PackageDependency(pkgStr));
            }
        }
        return list.toArray(new PackageDependency[list.size()]);
    }

    public DependencyResolution resolve(String pkgId, String targetPlatform)
            throws DependencyException {
        List<String> pkgInstall = new ArrayList<String>();
        if (pkgId.contains(":")) { // new syntax (PackageDependency style)
            pkgInstall.add(pkgId);
        } else { // old syntax (DownloadablePackage style)
            Package pkg = pm.getPackage(pkgId);
            if (pkg == null) {
                throw new DependencyException("Couldn't find " + pkgId);
            }
            pkgInstall.add(new PackageDependency(pkg.getName(),
                    new VersionRange(pkg.getVersion())).toString());
        }
        return resolve(pkgInstall, null, null, targetPlatform);
    }

}