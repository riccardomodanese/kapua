/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.qa.common;

import javax.xml.bind.JAXBContext;

import org.eclipse.kapua.commons.configuration.metatype.TscalarImpl;
import org.eclipse.kapua.commons.util.xml.JAXBContextProvider;
import org.eclipse.kapua.job.engine.commons.model.JobTargetSublist;
import org.eclipse.kapua.model.config.metatype.KapuaTad;
import org.eclipse.kapua.model.config.metatype.KapuaTdesignate;
import org.eclipse.kapua.model.config.metatype.KapuaTicon;
import org.eclipse.kapua.model.config.metatype.KapuaTmetadata;
import org.eclipse.kapua.model.config.metatype.KapuaTobject;
import org.eclipse.kapua.model.config.metatype.KapuaTocd;
import org.eclipse.kapua.model.config.metatype.KapuaToption;
import org.eclipse.kapua.model.config.metatype.MetatypeXmlRegistry;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountCreator;
import org.eclipse.kapua.service.account.AccountListResult;
import org.eclipse.kapua.service.account.AccountXmlRegistry;
import org.eclipse.kapua.service.account.Organization;
import org.eclipse.kapua.service.authentication.token.AccessToken;
import org.eclipse.kapua.service.device.call.kura.model.bundle.KuraBundle;
import org.eclipse.kapua.service.device.call.kura.model.bundle.KuraBundles;
import org.eclipse.kapua.service.device.call.kura.model.configuration.KuraDeviceComponentConfiguration;
import org.eclipse.kapua.service.device.call.kura.model.configuration.KuraDeviceConfiguration;
import org.eclipse.kapua.service.device.call.kura.model.deploy.KuraBundleInfo;
import org.eclipse.kapua.service.device.call.kura.model.deploy.KuraDeploymentPackage;
import org.eclipse.kapua.service.device.call.kura.model.deploy.KuraDeploymentPackages;
import org.eclipse.kapua.service.device.call.kura.model.snapshot.KuraSnapshotIds;
import org.eclipse.kapua.service.device.management.asset.DeviceAsset;
import org.eclipse.kapua.service.device.management.asset.DeviceAssets;
import org.eclipse.kapua.service.device.management.bundle.DeviceBundle;
import org.eclipse.kapua.service.device.management.bundle.DeviceBundles;
import org.eclipse.kapua.service.device.management.command.DeviceCommandInput;
import org.eclipse.kapua.service.device.management.configuration.DeviceComponentConfiguration;
import org.eclipse.kapua.service.device.management.configuration.DeviceConfiguration;
import org.eclipse.kapua.service.device.management.packages.model.DevicePackages;
import org.eclipse.kapua.service.device.management.packages.model.download.DevicePackageDownloadRequest;
import org.eclipse.kapua.service.device.management.packages.model.install.DevicePackageInstallRequest;
import org.eclipse.kapua.service.device.management.packages.model.uninstall.DevicePackageUninstallRequest;
import org.eclipse.kapua.service.device.management.snapshot.DeviceSnapshots;
import org.eclipse.kapua.service.job.Job;
import org.eclipse.kapua.service.job.JobListResult;
import org.eclipse.kapua.service.job.JobXmlRegistry;
import org.eclipse.kapua.service.tag.Tag;
import org.eclipse.kapua.service.tag.TagListResult;
import org.eclipse.kapua.service.tag.TagXmlRegistry;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserListResult;
import org.eclipse.kapua.service.user.UserQuery;
import org.eclipse.kapua.service.user.UserXmlRegistry;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

/**
 * JAXB context provided for proper (un)marshalling of interface annotated classes.
 * This particular implementation is used only in unit and integration tests.
 * <p>
 * Application and interfaces have their own implementation of provider.
 */
public class TestJAXBContextProvider implements JAXBContextProvider {

    private JAXBContext context;

    @Override
    public JAXBContext getJAXBContext() {
        try {
            if (context == null) {
                context = JAXBContextFactory.createContext(new Class<?>[]{
                        // General
                        KapuaTmetadata.class,
                        KapuaTocd.class,
                        KapuaTad.class,
                        KapuaTicon.class,
                        TscalarImpl.class,
                        KapuaToption.class,
                        KapuaTdesignate.class,
                        KapuaTobject.class,
                        MetatypeXmlRegistry.class,
                        // Device
                        KuraDeviceComponentConfiguration.class,
                        KuraDeviceConfiguration.class,
                        KuraDeploymentPackage.class,
                        KuraDeploymentPackages.class,
                        KuraBundle.class,
                        KuraBundles.class,
                        KuraBundleInfo.class,
                        DeviceAsset.class,
                        DeviceAssets.class,
                        DevicePackages.class,
                        DeviceBundle.class,
                        DeviceBundles.class,
                        DeviceCommandInput.class,
                        DeviceConfiguration.class,
                        DeviceComponentConfiguration.class,
                        DevicePackageInstallRequest.class,
                        DevicePackageUninstallRequest.class,
                        DevicePackageDownloadRequest.class,
                        KuraSnapshotIds.class,
                        DeviceSnapshots.class,
                        // Authorization
                        AccessToken.class,
                        // User
                        User.class,
                        UserCreator.class,
                        UserListResult.class,
                        UserQuery.class,
                        UserXmlRegistry.class,
                        // Account
                        Account.class,
                        AccountCreator.class,
                        AccountListResult.class,
                        Organization.class,
                        AccountXmlRegistry.class,
                        // Tag
                        Tag.class,
                        TagListResult.class,
                        TagXmlRegistry.class,
                        // Jobs
                        Job.class,
                        JobListResult.class,
                        JobXmlRegistry.class,
                        JobTargetSublist.class
                }, null);
            }
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
