/*******************************************************************************
 * Copyright (c) 2010, Hyper9
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * - Neither the name of the Hyper9 nor the names of its contributors may be 
 *   used to endorse or promote products derived from this software without 
 *   specific prior written permission. 
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.hyper9.simdk.ws;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.log4j.Logger;
import com.hyper9.simdk.ws.util.PropertyCollectorUtil;
import com.hyper9.simdk.ws.util.ServiceInstanceUtil;
import com.hyper9.simdk.ws.util.SessionManagerUtil;
import com.hyper9.simdk.ws.util.ViewManagerUtil;
import com.hyper9.simdk.stubs.VimServiceEx;
import com.hyper9.simdk.stubs.dao.*;
import com.hyper9.simdk.stubs.enums.ManagedEntityStatus;
import com.hyper9.simdk.stubs.enums.TaskInfoState;
import com.hyper9.simdk.stubs.enums.VirtualMachineMovePriority;
import com.hyper9.simdk.stubs.enums.VirtualMachinePowerState;
import com.hyper9.simdk.stubs.faults.AgentInstallFailed;
import com.hyper9.simdk.stubs.faults.AlreadyExists;
import com.hyper9.simdk.stubs.faults.AlreadyUpgraded;
import com.hyper9.simdk.stubs.faults.AuthMinimumAdminPermission;
import com.hyper9.simdk.stubs.faults.CannotAccessFile;
import com.hyper9.simdk.stubs.faults.CannotAccessLocalSource;
import com.hyper9.simdk.stubs.faults.ConcurrentAccess;
import com.hyper9.simdk.stubs.faults.CustomizationFault;
import com.hyper9.simdk.stubs.faults.DasConfigFault;
import com.hyper9.simdk.stubs.faults.DatastoreNotWritableOnHost;
import com.hyper9.simdk.stubs.faults.DuplicateName;
import com.hyper9.simdk.stubs.faults.DvsFault;
import com.hyper9.simdk.stubs.faults.DvsNotAuthorized;
import com.hyper9.simdk.stubs.faults.EVCConfigFault;
import com.hyper9.simdk.stubs.faults.FileAlreadyExists;
import com.hyper9.simdk.stubs.faults.FileFault;
import com.hyper9.simdk.stubs.faults.FileLocked;
import com.hyper9.simdk.stubs.faults.FileNotFound;
import com.hyper9.simdk.stubs.faults.HostConfigFailed;
import com.hyper9.simdk.stubs.faults.HostConfigFault;
import com.hyper9.simdk.stubs.faults.HostConnectFault;
import com.hyper9.simdk.stubs.faults.HostIncompatibleForRecordReplay;
import com.hyper9.simdk.stubs.faults.HostPowerOpFailed;
import com.hyper9.simdk.stubs.faults.HttpFault;
import com.hyper9.simdk.stubs.faults.InaccessibleDatastore;
import com.hyper9.simdk.stubs.faults.InsufficientResourcesFault;
import com.hyper9.simdk.stubs.faults.InvalidArgument;
import com.hyper9.simdk.stubs.faults.InvalidBundle;
import com.hyper9.simdk.stubs.faults.InvalidCollectorVersion;
import com.hyper9.simdk.stubs.faults.InvalidDatastore;
import com.hyper9.simdk.stubs.faults.InvalidDiskFormat;
import com.hyper9.simdk.stubs.faults.InvalidEvent;
import com.hyper9.simdk.stubs.faults.InvalidFolder;
import com.hyper9.simdk.stubs.faults.InvalidHostState;
import com.hyper9.simdk.stubs.faults.InvalidIpmiLoginInfo;
import com.hyper9.simdk.stubs.faults.InvalidIpmiMacAddress;
import com.hyper9.simdk.stubs.faults.InvalidLicense;
import com.hyper9.simdk.stubs.faults.InvalidLocale;
import com.hyper9.simdk.stubs.faults.InvalidLogin;
import com.hyper9.simdk.stubs.faults.InvalidName;
import com.hyper9.simdk.stubs.faults.InvalidPowerState;
import com.hyper9.simdk.stubs.faults.InvalidPrivilege;
import com.hyper9.simdk.stubs.faults.InvalidProperty;
import com.hyper9.simdk.stubs.faults.InvalidState;
import com.hyper9.simdk.stubs.faults.InvalidTicket;
import com.hyper9.simdk.stubs.faults.InvalidType;
import com.hyper9.simdk.stubs.faults.LeaseFault;
import com.hyper9.simdk.stubs.faults.LibraryFault;
import com.hyper9.simdk.stubs.faults.LicenseEntityAlreadyExists;
import com.hyper9.simdk.stubs.faults.LicenseEntityNotFound;
import com.hyper9.simdk.stubs.faults.LicenseServerUnavailable;
import com.hyper9.simdk.stubs.faults.LimitExceeded;
import com.hyper9.simdk.stubs.faults.LogBundlingFailed;
import com.hyper9.simdk.stubs.faults.MigrationFault;
import com.hyper9.simdk.stubs.faults.MismatchedBundle;
import com.hyper9.simdk.stubs.faults.NoActiveHostInCluster;
import com.hyper9.simdk.stubs.faults.NoClientCertificate;
import com.hyper9.simdk.stubs.faults.NoDiskFound;
import com.hyper9.simdk.stubs.faults.NoDiskSpace;
import com.hyper9.simdk.stubs.faults.NoHost;
import com.hyper9.simdk.stubs.faults.NoPermission;
import com.hyper9.simdk.stubs.faults.NoSubjectName;
import com.hyper9.simdk.stubs.faults.NotFound;
import com.hyper9.simdk.stubs.faults.NotSupported;
import com.hyper9.simdk.stubs.faults.NotSupportedHost;
import com.hyper9.simdk.stubs.faults.OutOfBounds;
import com.hyper9.simdk.stubs.faults.PatchBinariesNotFound;
import com.hyper9.simdk.stubs.faults.PatchInstallFailed;
import com.hyper9.simdk.stubs.faults.PatchMetadataInvalid;
import com.hyper9.simdk.stubs.faults.PatchNotApplicable;
import com.hyper9.simdk.stubs.faults.PlatformConfigFault;
import com.hyper9.simdk.stubs.faults.ProfileUpdateFailed;
import com.hyper9.simdk.stubs.faults.RebootRequired;
import com.hyper9.simdk.stubs.faults.RecordReplayDisabled;
import com.hyper9.simdk.stubs.faults.RemoveFailed;
import com.hyper9.simdk.stubs.faults.RequestCanceled;
import com.hyper9.simdk.stubs.faults.ResourceInUse;
import com.hyper9.simdk.stubs.faults.ResourceNotAvailable;
import com.hyper9.simdk.stubs.faults.RuntimeFault;
import com.hyper9.simdk.stubs.faults.SSLVerifyFault;
import com.hyper9.simdk.stubs.faults.SSPIChallenge;
import com.hyper9.simdk.stubs.faults.SnapshotFault;
import com.hyper9.simdk.stubs.faults.TaskInProgress;
import com.hyper9.simdk.stubs.faults.Timedout;
import com.hyper9.simdk.stubs.faults.TooManyHosts;
import com.hyper9.simdk.stubs.faults.TooManyTickets;
import com.hyper9.simdk.stubs.faults.TooManyWrites;
import com.hyper9.simdk.stubs.faults.ToolsUnavailable;
import com.hyper9.simdk.stubs.faults.UserNotFound;
import com.hyper9.simdk.stubs.faults.VAppConfigFault;
import com.hyper9.simdk.stubs.faults.VimFault;
import com.hyper9.simdk.stubs.faults.VmConfigFault;
import com.hyper9.simdk.stubs.faults.VmFaultToleranceIssue;
import com.hyper9.simdk.stubs.faults.VmToolsUpgradeFault;
import com.hyper9.simdk.stubs.faults.VmWwnConflict;
import com.hyper9.simdk.stubs.faults.VmfsAmbiguousMount;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;

public class VimServiceExImpl implements VimServiceEx
{
    private boolean isUnitTesting = false;

    /**
     * Gets a flag indicating whether the current caller is running a unit test.
     * 
     * @return A flag indicating whether the current caller is running a unit
     *         test.
     */
    boolean isUnitTesting()
    {
        return this.isUnitTesting;
    }

    /**
     * Sets a flag indicating whether the current caller is running a unit test.
     * 
     * @param toSet A flag indicating whether the current caller is running a
     *        unit test.
     */
    void setUnitTesting(boolean toSet)
    {
        this.isUnitTesting = toSet;
    }

    private String getSessionKey()
    {
        logger.debug("getting existing session key");
        return getSessionKey(false);
    }

    private String getSessionKey(boolean generateNewOne)
    {
        if (isUnitTesting)
        {
            return Constants.ANON_SESSION_KEY;
        }

        if (generateNewOne)
        {
            logger.debug("generating new session key");
            return UUID.randomUUID().toString();
        }

        HttpServletRequestEx req =
            (HttpServletRequestEx) this.webServiceContent
                .getMessageContext()
                .get(MessageContext.SERVLET_REQUEST);

        Cookie[] cookies = req.getCookies();

        if (cookies == null)
        {
            logger.debug("session key not found");
            return Constants.ANON_SESSION_KEY;
        }

        for (Cookie c : cookies)
        {
            if (c.getName().equals("vmware_soap_session"))
            {
                logger.debug("session key found=" + c.getValue());
                return c.getValue();
            }
        }

        logger.debug("session key not found");
        return Constants.ANON_SESSION_KEY;
    }

    private void setSessionKey(String toSet)
    {
        if (isUnitTesting)
        {
            return;
        }

        // Add the session key to the HTTP response's cookie collection.
        HttpServletResponse resp =
            (HttpServletResponse) this.webServiceContent
                .getMessageContext()
                .get(MessageContext.SERVLET_RESPONSE);
        Cookie c = new Cookie("vmware_soap_session", toSet);
        c.setPath("/");
        resp.addCookie(c);

        logger.debug("set session key=" + toSet);
    }

    /**
     * The logger.
     */
    private Logger logger = Logger.getLogger(VimServiceExImpl.class);

    private WebServiceContext webServiceContent;

    @Override
    public WebServiceContext getWebServiceContext()
    {
        return this.webServiceContent;
    }

    @Override
    public void postMethod()
    {
    }

    @Override
    public void preMethod()
    {
    }

    @Override
    public void setWebServiceContext(WebServiceContext toSet)
    {
        this.webServiceContent = toSet;
    }

    @Override
    public void FTManagerDisableSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference FTManagerDisableSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void FTManagerEnableSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference FTManagerEnableSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void FTManagerMakePrimaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference FTManagerMakePrimaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void FTManagerRegisterSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid,
        String cfgPath,
        String host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference FTManagerRegisterSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid,
        String cfgPath,
        String host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void FTManagerStartSecondaryOnRemoteHost(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String secondaryCfgPath,
        String host,
        Integer port)
        throws RemoteException,
        FileFault,
        Timedout,
        HostConnectFault,
        VmFaultToleranceIssue,
        MigrationFault,
        InsufficientResourcesFault,
        NotFound,
        RuntimeFault,
        VmConfigFault,
        TaskInProgress,
        InvalidState,
        InvalidPowerState,
        AlreadyExists
    {
    }

    @Override
    public ManagedObjectReference FTManagerStartSecondaryOnRemoteHost_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String secondaryCfgPath,
        String host,
        Integer port)
        throws RemoteException,
        FileFault,
        Timedout,
        HostConnectFault,
        VmFaultToleranceIssue,
        MigrationFault,
        InsufficientResourcesFault,
        NotFound,
        RuntimeFault,
        VmConfigFault,
        TaskInProgress,
        InvalidState,
        InvalidPowerState,
        AlreadyExists
    {
        return null;
    }

    @Override
    public void FTManagerTerminateFaultTolerantVM(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference FTManagerTerminateFaultTolerantVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void FTManagerUnregisterSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference FTManagerUnregisterSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference primary,
        String instanceUuid)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void FTManagerUnregisterVM(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        InvalidPowerState,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void LLPMReconfigVM(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec configSpec)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference LLPMReconfigVM_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec configSpec)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void abortBackup(ManagedObjectReference _this, String msg)
        throws RemoteException,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public void acknowledgeAlarm(
        ManagedObjectReference _this,
        ManagedObjectReference alarm,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public HostServiceTicket acquireCimServicesTicket(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public String acquireCloneTicket(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostDiskManagerLeaseInfo acquireLease(
        ManagedObjectReference _this,
        ManagedObjectReference snapshot,
        String diskPath) throws RemoteException, LeaseFault, RuntimeFault
    {
        return null;
    }

    @Override
    public HostDiskManagerLeaseInfo acquireLeaseExt(
        ManagedObjectReference _this,
        ManagedObjectReference snapshot,
        String diskPath,
        Boolean readOnly)
        throws RemoteException,
        LeaseFault,
        InvalidDiskFormat,
        RuntimeFault
    {
        return null;
    }

    @Override
    public SessionManagerLocalTicket acquireLocalTicket(
        ManagedObjectReference _this,
        String userName) throws RemoteException, InvalidLogin, RuntimeFault
    {
        return null;
    }

    @Override
    public VirtualMachineMksTicket acquireMksTicket(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String acquireSessionTicket(
        ManagedObjectReference _this,
        String serviceKey)
        throws RemoteException,
        InvalidLogin,
        NotSupportedHost,
        TooManyTickets,
        RuntimeFault,
        NoHost,
        NotFound
    {
        return null;
    }

    @Override
    public Integer addAuthorizationRole(
        ManagedObjectReference _this,
        String name,
        List<String> privIds)
        throws RemoteException,
        InvalidName,
        AlreadyExists,
        RuntimeFault
    {
        return null;
    }

    @Override
    public CustomFieldDef addCustomFieldDef(
        ManagedObjectReference _this,
        String name,
        String moType,
        PrivilegePolicyDef fieldDefPolicy,
        PrivilegePolicyDef fieldPolicy)
        throws RemoteException,
        DuplicateName,
        InvalidPrivilege,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void addDVPortgroup(
        ManagedObjectReference _this,
        List<DVPortgroupConfigSpec> spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        DvsFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference addDVPortgroup_Task(
        ManagedObjectReference _this,
        List<DVPortgroupConfigSpec> spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        DvsFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void addEndpoint(
        ManagedObjectReference _this,
        ProxyServiceEndpointSpec endpoint)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference addHost(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        Boolean asConnected,
        ManagedObjectReference resourcePool,
        String license)
        throws RemoteException,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addHostWithAdminDisabled(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        Boolean asConnected,
        ManagedObjectReference resourcePool,
        String license)
        throws RemoteException,
        HostConfigFault,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addHostWithAdminDisabled_Task(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        Boolean asConnected,
        ManagedObjectReference resourcePool,
        String license)
        throws RemoteException,
        HostConfigFault,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addHost_Task(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        Boolean asConnected,
        ManagedObjectReference resourcePool,
        String license)
        throws RemoteException,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void addInternetScsiSendTargets(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        List<HostInternetScsiHbaSendTarget> targets)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void addInternetScsiStaticTargets(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        List<HostInternetScsiHbaStaticTarget> targets)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public String addLibrary(
        ManagedObjectReference _this,
        String serviceUrl,
        String libName,
        String sslThumbprint)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault,
        NotFound,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public LicenseManagerLicenseInfo addLicense(
        ManagedObjectReference _this,
        String licenseKey,
        List<KeyValue> labels) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void addPortGroup(
        ManagedObjectReference _this,
        HostPortGroupSpec portgrp)
        throws RemoteException,
        HostConfigFault,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public String addServiceConsoleVirtualNic(
        ManagedObjectReference _this,
        String portgroup,
        HostVirtualNicSpec nic)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addStandaloneHost(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        ComputeResourceConfigSpec compResSpec,
        Boolean addConnected,
        String license)
        throws RemoteException,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addStandaloneHostWithAdminDisabled(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        ComputeResourceConfigSpec compResSpec,
        Boolean addConnected,
        String license)
        throws RemoteException,
        HostConfigFault,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addStandaloneHostWithAdminDisabled_Task(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        ComputeResourceConfigSpec compResSpec,
        Boolean addConnected,
        String license)
        throws RemoteException,
        HostConfigFault,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference addStandaloneHost_Task(
        ManagedObjectReference _this,
        HostConnectSpec spec,
        ComputeResourceConfigSpec compResSpec,
        Boolean addConnected,
        String license)
        throws RemoteException,
        InvalidLogin,
        DuplicateName,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void addTag(ManagedObjectReference _this, List<Tag> tag)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public String addVirtualNic(
        ManagedObjectReference _this,
        String portgroup,
        HostVirtualNicSpec nic)
        throws RemoteException,
        HostConfigFault,
        AlreadyExists,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void addVirtualSwitch(
        ManagedObjectReference _this,
        String vswitchName,
        HostVirtualSwitchSpec spec)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        AlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public void answerVM(
        ManagedObjectReference _this,
        String questionId,
        String answerChoice)
        throws RemoteException,
        ConcurrentAccess,
        RuntimeFault
    {
    }

    @Override
    public void applyHostConfig(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        HostConfigSpec configSpec)
        throws RemoteException,
        InvalidState,
        RuntimeFault,
        HostConfigFailed
    {
    }

    @Override
    public ManagedObjectReference applyHostConfig_Task(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        HostConfigSpec configSpec)
        throws RemoteException,
        InvalidState,
        RuntimeFault,
        HostConfigFailed
    {
        return null;
    }

    @Override
    public void applyRecommendation(ManagedObjectReference _this, String key)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public Boolean areAlarmActionsEnabled(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void assignUserToGroup(
        ManagedObjectReference _this,
        String user,
        String group)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault,
        UserNotFound
    {
    }

    @Override
    public void associateProfile(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void attachVmfsExtent(
        ManagedObjectReference _this,
        String vmfsPath,
        HostScsiDiskPartition extent)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void autoStartPowerOff(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void autoStartPowerOn(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public String backupFirmwareConfiguration(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public VirtualMachineBackupEventInfo backupProtocolEvent(
        ManagedObjectReference _this,
        Integer timeout)
        throws RemoteException,
        Timedout,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
        return null;
    }

    @Override
    public DiagnosticManagerLogHeader browseDiagnosticLog(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        String key,
        Integer start,
        Integer lines) throws RemoteException, CannotAccessFile, RuntimeFault
    {
        return null;
    }

    @Override
    public void cancelTask(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void cancelWaitForUpdates(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void changeOwner(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        String owner)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        UserNotFound
    {
    }

    @Override
    public List<CheckResult> checkClone(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference folder,
        String name,
        VirtualMachineCloneSpec spec,
        List<String> testType)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkClone_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference folder,
        String name,
        VirtualMachineCloneSpec spec,
        List<String> testType)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<CheckResult> checkCompatibility(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference host,
        ManagedObjectReference pool,
        List<String> testType)
        throws RemoteException,
        NoActiveHostInCluster,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkCompatibility_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference host,
        ManagedObjectReference pool,
        List<String> testType)
        throws RemoteException,
        NoActiveHostInCluster,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ComplianceResult> checkCompliance(
        ManagedObjectReference _this,
        List<ManagedObjectReference> profile,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkCompliance_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> profile,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void checkCustomizationResources(
        ManagedObjectReference _this,
        String guestOs)
        throws RemoteException,
        CustomizationFault,
        RuntimeFault
    {
    }

    @Override
    public void checkCustomizationSpec(
        ManagedObjectReference _this,
        CustomizationSpec spec)
        throws RemoteException,
        CustomizationFault,
        RuntimeFault
    {
    }

    @Override
    public ClusterDasAdmissionResult checkDasAdmission(
        ManagedObjectReference _this,
        List<ManagedObjectReference> vm) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public UpdateSet checkForUpdates(
        ManagedObjectReference _this,
        String version)
        throws RemoteException,
        InvalidCollectorVersion,
        RuntimeFault
    {
        return PropertyCollectorUtil.checkForUpdates(
            getSessionKey(),
            _this,
            version);
    }

    @Override
    public List<Boolean> checkGroupMembership(
        ManagedObjectReference _this,
        String userName,
        List<String> group) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostPatchManagerResult checkHostPatch(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkHostPatch_Task(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public Boolean checkLicenseFeature(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        String featureKey) throws RemoteException, InvalidState, RuntimeFault
    {
        return null;
    }

    @Override
    public List<CheckResult> checkMigrate(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference host,
        ManagedObjectReference pool,
        VirtualMachinePowerState state,
        List<String> testType)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkMigrate_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference host,
        ManagedObjectReference pool,
        VirtualMachinePowerState state,
        List<String> testType)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ComplianceResult> checkProfileCompliance(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkProfileCompliance_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<CheckResult> checkRelocate(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        VirtualMachineRelocateSpec spec,
        List<String> testType)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkRelocate_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        VirtualMachineRelocateSpec spec,
        List<String> testType)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<CheckResult> checkVMCompatibility(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec vmConfigSpec,
        List<ManagedObjectReference> hosts)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference checkVMCompatibility_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec vmConfigSpec,
        List<ManagedObjectReference> hosts)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void clearComplianceStatus(
        ManagedObjectReference _this,
        List<ManagedObjectReference> profile,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public UserSession cloneSession(
        ManagedObjectReference _this,
        String cloneTicket) throws RemoteException, InvalidLogin, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference cloneVApp(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference target,
        VAppCloneSpec spec)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference cloneVApp_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference target,
        VAppCloneSpec spec)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference cloneVM(
        ManagedObjectReference _this,
        ManagedObjectReference folder,
        String name,
        VirtualMachineCloneSpec spec)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        CustomizationFault,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference cloneVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference folder,
        String name,
        VirtualMachineCloneSpec spec)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        CustomizationFault,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> closeInventoryViewFolder(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void completeDestination(
        ManagedObjectReference _this,
        Long migrationId) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void completeSource(ManagedObjectReference _this, Long migrationId)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public HostDiskPartitionInfo computeDiskPartitionInfo(
        ManagedObjectReference _this,
        String devicePath,
        HostDiskPartitionLayout layout)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public HostDiskPartitionInfo computeDiskPartitionInfoForResize(
        ManagedObjectReference _this,
        HostScsiDiskPartition partition,
        HostDiskPartitionBlockRange blockRange)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void configureDatastorePrincipal(
        ManagedObjectReference _this,
        String userName,
        String password)
        throws RemoteException,
        HostConfigFault,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void configureEVC(ManagedObjectReference _this, String evcModeKey)
        throws RemoteException,
        EVCConfigFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference configureEVC_Task(
        ManagedObjectReference _this,
        String evcModeKey) throws RemoteException, EVCConfigFault, RuntimeFault
    {
        return null;
    }

    @Override
    public void configureLicenseSource(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        LicenseSource licenseSource)
        throws RemoteException,
        CannotAccessLocalSource,
        LicenseServerUnavailable,
        InvalidLicense,
        RuntimeFault
    {
    }

    @Override
    public void copy(
        ManagedObjectReference _this,
        ManagedObjectReference sourceDatacenter,
        String sourcePath,
        ManagedObjectReference destinationDatacenter,
        String destinationPath,
        Boolean force,
        String fileType)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public void copyDatastoreFile(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destinationName,
        ManagedObjectReference destinationDatacenter,
        Boolean force)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference copyDatastoreFile_Task(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destinationName,
        ManagedObjectReference destinationDatacenter,
        Boolean force)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String copyVirtualDisk(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destName,
        ManagedObjectReference destDatacenter,
        VirtualDiskSpec destSpec,
        Boolean force) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference copyVirtualDisk_Task(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destName,
        ManagedObjectReference destDatacenter,
        VirtualDiskSpec destSpec,
        Boolean force) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference copy_Task(
        ManagedObjectReference _this,
        ManagedObjectReference sourceDatacenter,
        String sourcePath,
        ManagedObjectReference destinationDatacenter,
        String destinationPath,
        Boolean force,
        String fileType)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createAlarm(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        AlarmSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createChildVM(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec config,
        ManagedObjectReference host)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createChildVM_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec config,
        ManagedObjectReference host)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createCluster(
        ManagedObjectReference _this,
        String name,
        ClusterConfigSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createClusterEx(
        ManagedObjectReference _this,
        String name,
        ClusterConfigSpecEx spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createCollectorForEvents(
        ManagedObjectReference _this,
        EventFilterSpec filter)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createCollectorForTasks(
        ManagedObjectReference _this,
        TaskFilterSpec filter)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createContainerView(
        ManagedObjectReference _this,
        ManagedObjectReference container,
        List<String> type,
        Boolean recursive) throws RemoteException, RuntimeFault
    {
        return ViewManagerUtil.createContainerView(
            getSessionKey(),
            _this,
            container,
            type,
            recursive);
    }

    @Override
    public void createCustomizationSpec(
        ManagedObjectReference _this,
        CustomizationSpecItem item)
        throws RemoteException,
        CustomizationFault,
        AlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference createDVS(
        ManagedObjectReference _this,
        DVSCreateSpec spec)
        throws RemoteException,
        InvalidName,
        DvsNotAuthorized,
        DuplicateName,
        DvsFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public ManagedObjectReference createDVS_Task(
        ManagedObjectReference _this,
        DVSCreateSpec spec)
        throws RemoteException,
        InvalidName,
        DvsNotAuthorized,
        DuplicateName,
        DvsFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public ManagedObjectReference createDatacenter(
        ManagedObjectReference _this,
        String name)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ApplyProfile createDefaultProfile(
        ManagedObjectReference _this,
        String profileType) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public OvfCreateDescriptorResult createDescriptor(
        ManagedObjectReference _this,
        ManagedObjectReference obj,
        OvfCreateDescriptorParams cdp)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void createDiagnosticPartition(
        ManagedObjectReference _this,
        HostDiagnosticPartitionCreateSpec spec)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference createFilter(
        ManagedObjectReference _this,
        PropertyFilterSpec spec,
        Boolean partialUpdates)
        throws RemoteException,
        InvalidProperty,
        RuntimeFault
    {
        return PropertyCollectorUtil.createFilter(
            getSessionKey(),
            _this,
            spec,
            partialUpdates);
    }

    @Override
    public ManagedObjectReference createFolder(
        ManagedObjectReference _this,
        String name)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void createGroup(ManagedObjectReference _this, HostAccountSpec group)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public OvfCreateImportSpecResult createImportSpec(
        ManagedObjectReference _this,
        String ovfDescriptor,
        ManagedObjectReference resourcePool,
        ManagedObjectReference datastore,
        OvfCreateImportSpecParams cisp)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createInventoryView(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public Integer createIpPool(
        ManagedObjectReference _this,
        ManagedObjectReference dc,
        IpPool pool) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public String createLinkedClone(
        ManagedObjectReference _this,
        String vmName,
        String dsPath,
        Boolean overwrite)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createLinkedClone_Task(
        ManagedObjectReference _this,
        String vmName,
        String dsPath,
        Boolean overwrite)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createListView(
        ManagedObjectReference _this,
        List<ManagedObjectReference> obj) throws RemoteException, RuntimeFault
    {
        return ViewManagerUtil.createListView(getSessionKey(), _this, obj);
    }

    @Override
    public ManagedObjectReference createListViewFromView(
        ManagedObjectReference _this,
        ManagedObjectReference view) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createLocalDatastore(
        ManagedObjectReference _this,
        String name,
        String path)
        throws RemoteException,
        HostConfigFault,
        InvalidName,
        DuplicateName,
        FileNotFound,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createNasDatastore(
        ManagedObjectReference _this,
        HostNasVolumeSpec spec)
        throws RemoteException,
        HostConfigFault,
        DuplicateName,
        AlreadyExists,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createObjectScheduledTask(
        ManagedObjectReference _this,
        ManagedObjectReference obj,
        ScheduledTaskSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void createPerfInterval(
        ManagedObjectReference _this,
        PerfInterval intervalId) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference createProfile(
        ManagedObjectReference _this,
        ProfileCreateSpec createSpec)
        throws RemoteException,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createResourcePool(
        ManagedObjectReference _this,
        String name,
        ResourceConfigSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createScheduledTask(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        ScheduledTaskSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String createScreenshot(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createScreenshot_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public FaultToleranceSecondaryOpResult createSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        VmFaultToleranceIssue,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createSnapshot(
        ManagedObjectReference _this,
        String name,
        String description,
        Boolean memory,
        Boolean quiesce)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createSnapshot_Task(
        ManagedObjectReference _this,
        String name,
        String description,
        Boolean memory,
        Boolean quiesce)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public TaskInfo createTask(
        ManagedObjectReference _this,
        ManagedObjectReference obj,
        String taskTypeId,
        String initiatedBy,
        Boolean cancelable,
        String parentTaskKey) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void createUser(ManagedObjectReference _this, HostAccountSpec user)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference createVApp(
        ManagedObjectReference _this,
        String name,
        ResourceConfigSpec resSpec,
        VAppConfigSpec configSpec,
        ManagedObjectReference vmFolder)
        throws RemoteException,
        VmConfigFault,
        InvalidName,
        DuplicateName,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createVM(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec config,
        ManagedObjectReference pool,
        ManagedObjectReference host)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createVM_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec config,
        ManagedObjectReference pool,
        ManagedObjectReference host)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String createVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        VirtualDiskSpec spec) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        VirtualDiskSpec spec) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference createVmfsDatastore(
        ManagedObjectReference _this,
        VmfsDatastoreCreateSpec spec)
        throws RemoteException,
        HostConfigFault,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public Calendar currentTime(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return Calendar.getInstance();
    }

    @Override
    public String customizationSpecItemToXml(
        ManagedObjectReference _this,
        CustomizationSpecItem item) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void customizeVM(ManagedObjectReference _this, CustomizationSpec spec)
        throws RemoteException,
        CustomizationFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference customizeVM_Task(
        ManagedObjectReference _this,
        CustomizationSpec spec)
        throws RemoteException,
        CustomizationFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public LicenseManagerLicenseInfo decodeLicense(
        ManagedObjectReference _this,
        String licenseKey) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void defragmentAllDisks(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public void defragmentVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference defragmentVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void delete(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String datastorePath,
        String fileType)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public void deleteCustomizationSpec(
        ManagedObjectReference _this,
        String name) throws RemoteException, RuntimeFault, NotFound
    {
    }

    @Override
    public void deleteDatastoreFile(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference deleteDatastoreFile_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void deleteFile(ManagedObjectReference _this, String datastorePath)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public void deleteVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference deleteVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void deleteVm(
        ManagedObjectReference _this,
        VirtualMachineConfigInfo configInfo)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference deleteVm_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigInfo configInfo)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference delete_Task(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String datastorePath,
        String fileType)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void deselectVnic(ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void deselectVnicForNicType(
        ManagedObjectReference _this,
        String nicType,
        String device)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        InvalidArgument
    {
    }

    @Override
    public void destroy(ManagedObjectReference _this)
        throws RemoteException,
        VimFault,
        RuntimeFault
    {
    }

    @Override
    public void destroyChildren(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void destroyCollector(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void destroyDatastore(ManagedObjectReference _this)
        throws RemoteException,
        ResourceInUse,
        RuntimeFault
    {
    }

    @Override
    public void destroyIpPool(
        ManagedObjectReference _this,
        ManagedObjectReference dc,
        Integer id,
        Boolean force) throws RemoteException, InvalidState, RuntimeFault
    {
    }

    @Override
    public void destroyNetwork(ManagedObjectReference _this)
        throws RemoteException,
        ResourceInUse,
        RuntimeFault
    {
    }

    @Override
    public void destroyProfile(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void destroyPropertyFilter(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void destroyView(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference destroy_Task(ManagedObjectReference _this)
        throws RemoteException,
        VimFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void disableAdmin(ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void disableEVC(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference disableEVC_Task(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public Boolean disableFeature(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        String featureKey)
        throws RemoteException,
        LicenseServerUnavailable,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void disableHyperThreading(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void disableMethods(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity,
        List<DisabledMethodRequest> method,
        String sourceId) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void disableMultipathPath(
        ManagedObjectReference _this,
        String pathName)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void disableRuleset(ManagedObjectReference _this, String id)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void disableSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference disableSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void disconnectHost(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference disconnectHost_Task(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void dissociateProfile(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public Boolean doesCustomizationSpecExist(
        ManagedObjectReference _this,
        String name) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void duplicateCustomizationSpec(
        ManagedObjectReference _this,
        String name,
        String newName)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void eagerZeroVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference eagerZeroVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void enableAdmin(ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void enableAlarmActions(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        Boolean enabled) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public Boolean enableFeature(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        String featureKey)
        throws RemoteException,
        LicenseServerUnavailable,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void enableHyperThreading(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public List<EntityDisabledMethodInfo> enableMethods(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity,
        List<String> method,
        String sourceId) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void enableMultipathPath(
        ManagedObjectReference _this,
        String pathName)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void enableRuleset(ManagedObjectReference _this, String id)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public FaultToleranceSecondaryOpResult enableSecondaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference enableSecondaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void enterMaintenanceMode(
        ManagedObjectReference _this,
        Integer timeout,
        Boolean evacuatePoweredOffVms)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference enterMaintenanceMode_Task(
        ManagedObjectReference _this,
        Integer timeout,
        Boolean evacuatePoweredOffVms)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public DatabaseSizeEstimate estimateDatabaseSize(
        ManagedObjectReference _this,
        DatabaseSizeParam dbSizeParam) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ProfileExecuteResult executeHostProfile(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        List<ProfileDeferredPolicyOptionParameter> deferredParam)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String executeSimpleCommand(
        ManagedObjectReference _this,
        List<String> arguments) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void exitMaintenanceMode(
        ManagedObjectReference _this,
        Integer timeout)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference exitMaintenanceMode_Task(
        ManagedObjectReference _this,
        Integer timeout)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference expandVmfsDatastore(
        ManagedObjectReference _this,
        ManagedObjectReference datastore,
        VmfsDatastoreExpandSpec spec)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void expandVmfsExtent(
        ManagedObjectReference _this,
        String vmfsPath,
        HostScsiDiskPartition extent)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public String exportMediaToLibrary(
        ManagedObjectReference _this,
        ManagedObjectReference srcDc,
        String srcName,
        String libKey,
        ImageLibraryManagerMediaInfo metadata,
        List<KeyValue> headers)
        throws RemoteException,
        FileLocked,
        FileNotFound,
        LibraryFault,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault,
        InvalidArgument
    {
        return null;
    }

    @Override
    public ManagedObjectReference exportMediaToLibrary_Task(
        ManagedObjectReference _this,
        ManagedObjectReference srcDc,
        String srcName,
        String libKey,
        ImageLibraryManagerMediaInfo metadata,
        List<KeyValue> headers)
        throws RemoteException,
        FileLocked,
        FileNotFound,
        LibraryFault,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault,
        InvalidArgument
    {
        return null;
    }

    @Override
    public List<String> exportOvfToUrl(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        String dstBaseUrl,
        List<KeyValue> httpHeaders,
        String sslThumbprint)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidType,
        ConcurrentAccess,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference exportOvfToUrl_Task(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        String dstBaseUrl,
        List<KeyValue> httpHeaders,
        String sslThumbprint)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidType,
        ConcurrentAccess,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public String exportProfile(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference exportVApp(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference exportVm(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void extendVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        Long newCapacityKb,
        Boolean eagerZero) throws RemoteException, FileFault, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference extendVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        Long newCapacityKb,
        Boolean eagerZero) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference extendVmfsDatastore(
        ManagedObjectReference _this,
        ManagedObjectReference datastore,
        VmfsDatastoreExtendSpec spec)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public String extractOvfEnvironment(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<String> fetchDVPortKeys(
        ManagedObjectReference _this,
        DistributedVirtualSwitchPortCriteria criteria)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<DistributedVirtualPort> fetchDVPorts(
        ManagedObjectReference _this,
        DistributedVirtualSwitchPortCriteria criteria)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> findAllByDnsName(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String dnsName,
        Boolean vmSearch) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> findAllByIp(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String ip,
        Boolean vmSearch) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> findAllByUuid(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String uuid,
        Boolean vmSearch,
        Boolean instanceUuid) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> findAssociatedProfile(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference findByDatastorePath(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String path) throws RemoteException, InvalidDatastore, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference findByDnsName(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String dnsName,
        Boolean vmSearch) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference findByInventoryPath(
        ManagedObjectReference _this,
        String inventoryPath) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference findByIp(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String ip,
        Boolean vmSearch) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference findByUuid(
        ManagedObjectReference _this,
        ManagedObjectReference datacenter,
        String uuid,
        Boolean vmSearch,
        Boolean instanceUuid) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference findChild(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        String name) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public Extension findExtension(
        ManagedObjectReference _this,
        String extensionKey) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostVmfsVolume formatVmfs(
        ManagedObjectReference _this,
        HostVmfsSpec createSpec)
        throws RemoteException,
        HostConfigFault,
        AlreadyExists,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostProfileManagerConfigTaskList generateConfigTaskList(
        ManagedObjectReference _this,
        HostConfigSpec configSpec,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<DiagnosticManagerBundleInfo> generateLogBundles(
        ManagedObjectReference _this,
        Boolean includeDefault,
        List<ManagedObjectReference> host)
        throws RemoteException,
        RuntimeFault,
        LogBundlingFailed
    {
        return null;
    }

    @Override
    public ManagedObjectReference generateLogBundles_Task(
        ManagedObjectReference _this,
        Boolean includeDefault,
        List<ManagedObjectReference> host)
        throws RemoteException,
        RuntimeFault,
        LogBundlingFailed
    {
        return null;
    }

    @Override
    public HostIntegrityReport generateReport(
        ManagedObjectReference _this,
        List<Integer> pcrSelection,
        List<Byte> nonce) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> getAlarm(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<AlarmState> getAlarmState(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public CustomizationSpecItem getCustomizationSpec(
        ManagedObjectReference _this,
        String name) throws RemoteException, RuntimeFault, NotFound
    {
        return null;
    }

    @Override
    public String getPublicKey(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void hostDVSClonePort(
        ManagedObjectReference _this,
        String switchUuid,
        List<HostDVSPortCloneSpec> ports)
        throws RemoteException,
        PlatformConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void hostDVSCreateDVS(
        ManagedObjectReference _this,
        HostDVSCreateSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        AlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public void hostDVSDeletePorts(
        ManagedObjectReference _this,
        String switchUuid,
        List<HostDVSPortDeleteSpec> portSpec)
        throws RemoteException,
        PlatformConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public List<HostDVSPortData> hostDVSGetPortState(
        ManagedObjectReference _this,
        String switchUuid,
        List<String> portKeys)
        throws RemoteException,
        PlatformConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void hostDVSReconfigure(
        ManagedObjectReference _this,
        HostDVSConfigSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void hostDVSRemoveDVS(ManagedObjectReference _this, String switchUuid)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void hostDVSUpdateDVPortgroups(
        ManagedObjectReference _this,
        String switchUuid,
        List<HostDVPortgroupConfigSpec> configSpec)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void hostDVSUpdatePorts(
        ManagedObjectReference _this,
        String switchUuid,
        List<HostDVSPortData> portData)
        throws RemoteException,
        PlatformConfigFault,
        RuntimeFault,
        NotFound,
        LimitExceeded
    {
    }

    @Override
    public void httpNfcLeaseAbort(
        ManagedObjectReference _this,
        LocalizedMethodFault fault)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void httpNfcLeaseComplete(ManagedObjectReference _this)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void httpNfcLeaseProgress(
        ManagedObjectReference _this,
        Integer percent) throws RemoteException, Timedout, RuntimeFault
    {
    }

    @Override
    public UserSession impersonateUser(
        ManagedObjectReference _this,
        String userName,
        String locale)
        throws RemoteException,
        InvalidLogin,
        InvalidLocale,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void importLibraryMedia(
        ManagedObjectReference _this,
        String url,
        String sslThumbprint,
        List<KeyValue> headers,
        ManagedObjectReference dstDc,
        String dstName,
        Boolean force)
        throws RemoteException,
        NoDiskSpace,
        LibraryFault,
        FileAlreadyExists,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
    }

    @Override
    public ManagedObjectReference importLibraryMedia_Task(
        ManagedObjectReference _this,
        String url,
        String sslThumbprint,
        List<KeyValue> headers,
        ManagedObjectReference dstDc,
        String dstName,
        Boolean force)
        throws RemoteException,
        NoDiskSpace,
        LibraryFault,
        FileAlreadyExists,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference importOvfAtUrl(
        ManagedObjectReference _this,
        String ovfDescriptorUrl,
        OvfCreateImportSpecParams importSpecParams,
        ManagedObjectReference fldr,
        ManagedObjectReference pool,
        ManagedObjectReference datastore,
        List<KeyValue> httpHeaders,
        String sslThumbprint)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        VmWwnConflict,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference importOvfAtUrl_Task(
        ManagedObjectReference _this,
        String ovfDescriptorUrl,
        OvfCreateImportSpecParams importSpecParams,
        ManagedObjectReference fldr,
        ManagedObjectReference pool,
        ManagedObjectReference datastore,
        List<KeyValue> httpHeaders,
        String sslThumbprint)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        VmWwnConflict,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference importVApp(
        ManagedObjectReference _this,
        ImportSpec spec,
        ManagedObjectReference folder,
        ManagedObjectReference host)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void inflateVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference inflateVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostVMotionManagerDestinationState initiateDestination(
        ManagedObjectReference _this,
        Long migrationId,
        String dstConfigPath)
        throws RemoteException,
        TaskInProgress,
        Timedout,
        InvalidState,
        AlreadyExists,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void initiateSource(
        ManagedObjectReference _this,
        Long migrationId,
        Integer dstId)
        throws RemoteException,
        Timedout,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference initiateSource_Task(
        ManagedObjectReference _this,
        Long migrationId,
        Integer dstId)
        throws RemoteException,
        Timedout,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void installHostPatch(
        ManagedObjectReference _this,
        HostPatchManagerLocator repository,
        String updateID,
        Boolean force)
        throws RemoteException,
        NoDiskSpace,
        TaskInProgress,
        RebootRequired,
        PatchBinariesNotFound,
        InvalidState,
        PatchNotApplicable,
        RuntimeFault,
        PatchInstallFailed,
        PatchMetadataInvalid
    {
    }

    @Override
    public HostPatchManagerResult installHostPatchV2(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        List<String> vibUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference installHostPatchV2_Task(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        List<String> vibUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference installHostPatch_Task(
        ManagedObjectReference _this,
        HostPatchManagerLocator repository,
        String updateID,
        Boolean force)
        throws RemoteException,
        NoDiskSpace,
        TaskInProgress,
        RebootRequired,
        PatchBinariesNotFound,
        InvalidState,
        PatchNotApplicable,
        RuntimeFault,
        PatchInstallFailed,
        PatchMetadataInvalid
    {
        return null;
    }

    @Override
    public List<InternalStatsValue> internalQueryLatestVmStats(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<String> statsSet) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<LicenseAssignmentManagerFeatureLicenseAvailability> isFeatureAvailable(
        ManagedObjectReference _this,
        List<LicenseAssignmentManagerEntityFeaturePair> entityFeaturePair)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostDiskBlockInfo leaseAllocateBlocks(
        ManagedObjectReference _this,
        Long startBlock,
        Long numBlocks,
        Boolean assureValid)
        throws RemoteException,
        NoDiskSpace,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void leaseClearLazyZero(
        ManagedObjectReference _this,
        Long startBlock,
        Long numBlocks,
        Boolean fillZero) throws RemoteException, NoPermission, RuntimeFault
    {
    }

    @Override
    public HostDiskBlockInfo leaseMapDiskRegion(
        ManagedObjectReference _this,
        Long mapStart,
        Long mapLength) throws RemoteException, OutOfBounds, RuntimeFault
    {
        return null;
    }

    @Override
    public void loadDVPort(
        ManagedObjectReference _this,
        String switchUuid,
        String portKey,
        String filePath)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void logUserEvent(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        String msg) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public UserSession login(
        ManagedObjectReference _this,
        String userName,
        String password,
        String locale)
        throws RemoteException,
        InvalidLogin,
        InvalidLocale,
        RuntimeFault
    {
        return SessionManagerUtil.login(
            getSessionKey(true),
            _this,
            userName,
            password,
            locale);
    }

    @Override
    public UserSession loginBySSLThumbprint(
        ManagedObjectReference _this,
        String locale)
        throws RemoteException,
        InvalidLogin,
        NoClientCertificate,
        NotSupported,
        InvalidLocale,
        RuntimeFault,
        UserNotFound
    {
        return null;
    }

    @Override
    public UserSession loginBySSPI(
        ManagedObjectReference _this,
        String base64Token,
        String locale)
        throws RemoteException,
        InvalidLogin,
        InvalidLocale,
        SSPIChallenge,
        RuntimeFault
    {
        return null;
    }

    @Override
    public UserSession loginBySessionTicket(
        ManagedObjectReference _this,
        String ticketData)
        throws RemoteException,
        NotSupported,
        InvalidTicket,
        RuntimeFault,
        UserNotFound
    {
        return null;
    }

    @Override
    public UserSession loginExtensionByCertificate(
        ManagedObjectReference _this,
        String extensionKey,
        String locale)
        throws RemoteException,
        InvalidLogin,
        NoClientCertificate,
        InvalidLocale,
        RuntimeFault
    {
        return null;
    }

    @Override
    public UserSession loginExtensionBySubjectName(
        ManagedObjectReference _this,
        String extensionKey,
        String locale)
        throws RemoteException,
        InvalidLogin,
        NoSubjectName,
        NoClientCertificate,
        InvalidLocale,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void logout(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        SessionManagerUtil.logout(getSessionKey());
    }

    @Override
    public void lowLevelConsolidateDisks(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<VirtualDisk> disks)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference lowLevelConsolidateDisks_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<VirtualDisk> disks)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference lowLevelCreateVm(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec configSpec)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference lowLevelCreateVm_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec configSpec)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void makeDirectory(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        Boolean createParentDirectories)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public void makePrimaryVM(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference makePrimaryVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void markAsTemplate(ManagedObjectReference _this)
        throws RemoteException,
        VmConfigFault,
        FileFault,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void markAsVirtualMachine(
        ManagedObjectReference _this,
        ManagedObjectReference pool,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        FileFault,
        InvalidState,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public void mergeDvs(
        ManagedObjectReference _this,
        ManagedObjectReference dvs)
        throws RemoteException,
        DvsFault,
        InvalidHostState,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference mergeDvs_Task(
        ManagedObjectReference _this,
        ManagedObjectReference dvs)
        throws RemoteException,
        DvsFault,
        InvalidHostState,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void mergePermissions(
        ManagedObjectReference _this,
        Integer srcRoleId,
        Integer dstRoleId)
        throws RemoteException,
        AuthMinimumAdminPermission,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void migrateVM(
        ManagedObjectReference _this,
        ManagedObjectReference pool,
        ManagedObjectReference host,
        VirtualMachineMovePriority priority,
        VirtualMachinePowerState state)
        throws RemoteException,
        VmConfigFault,
        Timedout,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference migrateVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference pool,
        ManagedObjectReference host,
        VirtualMachineMovePriority priority,
        VirtualMachinePowerState state)
        throws RemoteException,
        VmConfigFault,
        Timedout,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> modifyListView(
        ManagedObjectReference _this,
        List<ManagedObjectReference> add,
        List<ManagedObjectReference> remove)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void mountToolsInstaller(ManagedObjectReference _this)
        throws RemoteException,
        VmConfigFault,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void move(
        ManagedObjectReference _this,
        ManagedObjectReference sourceDatacenter,
        String sourcePath,
        ManagedObjectReference destinationDatacenter,
        String destinationPath,
        Boolean force,
        String fileType)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public void moveDVPort(
        ManagedObjectReference _this,
        List<String> portKey,
        String destinationPortgroupKey)
        throws RemoteException,
        DvsFault,
        ConcurrentAccess,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference moveDVPort_Task(
        ManagedObjectReference _this,
        List<String> portKey,
        String destinationPortgroupKey)
        throws RemoteException,
        DvsFault,
        ConcurrentAccess,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void moveDatastoreFile(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destinationName,
        ManagedObjectReference destinationDatacenter,
        Boolean force)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference moveDatastoreFile_Task(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destinationName,
        ManagedObjectReference destinationDatacenter,
        Boolean force)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void moveHostInto(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        ManagedObjectReference resourcePool)
        throws RemoteException,
        InvalidState,
        TooManyHosts,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference moveHostInto_Task(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        ManagedObjectReference resourcePool)
        throws RemoteException,
        InvalidState,
        TooManyHosts,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void moveInto(
        ManagedObjectReference _this,
        List<ManagedObjectReference> host)
        throws RemoteException,
        DuplicateName,
        InvalidState,
        TooManyHosts,
        RuntimeFault
    {
    }

    @Override
    public void moveIntoFolder(
        ManagedObjectReference _this,
        List<ManagedObjectReference> list)
        throws RemoteException,
        DuplicateName,
        InvalidState,
        RuntimeFault,
        InvalidFolder
    {
    }

    @Override
    public ManagedObjectReference moveIntoFolder_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> list)
        throws RemoteException,
        DuplicateName,
        InvalidState,
        RuntimeFault,
        InvalidFolder
    {
        return null;
    }

    @Override
    public void moveIntoResourcePool(
        ManagedObjectReference _this,
        List<ManagedObjectReference> list)
        throws RemoteException,
        DuplicateName,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference moveInto_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> host)
        throws RemoteException,
        DuplicateName,
        InvalidState,
        TooManyHosts,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String moveVirtualDisk(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destName,
        ManagedObjectReference destDatacenter,
        Boolean force) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference moveVirtualDisk_Task(
        ManagedObjectReference _this,
        String sourceName,
        ManagedObjectReference sourceDatacenter,
        String destName,
        ManagedObjectReference destDatacenter,
        Boolean force) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference move_Task(
        ManagedObjectReference _this,
        ManagedObjectReference sourceDatacenter,
        String sourcePath,
        ManagedObjectReference destinationDatacenter,
        String destinationPath,
        Boolean force,
        String fileType)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostServiceTicket nfcFileManagement(
        ManagedObjectReference _this,
        ManagedObjectReference ds,
        ManagedObjectReference hostForAccess)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostServiceTicket nfcGetVmFiles(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference hostForAccess)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostServiceTicket nfcPutVmFiles(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference hostForAccess)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostServiceTicket nfcRandomAccessOpenDisk(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        Integer diskDeviceKey,
        ManagedObjectReference hostForAccess)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostServiceTicket nfcRandomAccessOpenReadonly(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        Integer diskDeviceKey,
        ManagedObjectReference hostForAccess)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostServiceTicket nfcSystemManagement(
        ManagedObjectReference _this,
        ManagedObjectReference hostForAccess)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void notifySnapshotCompletion(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public List<ManagedObjectReference> openInventoryViewFolder(
        ManagedObjectReference _this,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void overwriteCustomizationSpec(
        ManagedObjectReference _this,
        CustomizationSpecItem item)
        throws RemoteException,
        CustomizationFault,
        ConcurrentAccess,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public OvfParseDescriptorResult parseDescriptor(
        ManagedObjectReference _this,
        String ovfDescriptor,
        OvfParseDescriptorParams pdp)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public OvfParseDescriptorResult parseDescriptorAtUrl(
        ManagedObjectReference _this,
        String ovfDescriptorUrl,
        OvfParseDescriptorParams pdp,
        List<KeyValue> httpHeaders,
        String sslThumbprint)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference parseDescriptorAtUrl_Task(
        ManagedObjectReference _this,
        String ovfDescriptorUrl,
        OvfParseDescriptorParams pdp,
        List<KeyValue> httpHeaders,
        String sslThumbprint)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault
    {
        return null;
    }

    @Override
    public void performDvsProductSpecOperation(
        ManagedObjectReference _this,
        String operation,
        DistributedVirtualSwitchProductSpec productSpec)
        throws RemoteException,
        DvsFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference performDvsProductSpecOperation_Task(
        ManagedObjectReference _this,
        String operation,
        DistributedVirtualSwitchProductSpec productSpec)
        throws RemoteException,
        DvsFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void postEvent(
        ManagedObjectReference _this,
        Event eventToPost,
        TaskInfo taskInfo) throws RemoteException, RuntimeFault, InvalidEvent
    {
    }

    @Override
    public void powerDownHostToStandBy(
        ManagedObjectReference _this,
        Integer timeoutSec,
        Boolean evacuatePoweredOffVms)
        throws RemoteException,
        RequestCanceled,
        HostPowerOpFailed,
        NotSupported,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference powerDownHostToStandBy_Task(
        ManagedObjectReference _this,
        Integer timeoutSec,
        Boolean evacuatePoweredOffVms)
        throws RemoteException,
        RequestCanceled,
        HostPowerOpFailed,
        NotSupported,
        Timedout,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void powerOffVApp(ManagedObjectReference _this, Boolean force)
        throws RemoteException,
        VAppConfigFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference powerOffVApp_Task(
        ManagedObjectReference _this,
        Boolean force)
        throws RemoteException,
        VAppConfigFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void powerOffVM(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference powerOffVM_Task(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ClusterPowerOnVmResult powerOnMultiVM(
        ManagedObjectReference _this,
        List<ManagedObjectReference> vm) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference powerOnMultiVM_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> vm) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void powerOnVApp(ManagedObjectReference _this)
        throws RemoteException,
        VAppConfigFault,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference powerOnVApp_Task(ManagedObjectReference _this)
        throws RemoteException,
        VAppConfigFault,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void powerOnVM(
        ManagedObjectReference _this,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference powerOnVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void powerUpHostFromStandBy(
        ManagedObjectReference _this,
        Integer timeoutSec)
        throws RemoteException,
        HostPowerOpFailed,
        NotSupported,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference powerUpHostFromStandBy_Task(
        ManagedObjectReference _this,
        Integer timeoutSec)
        throws RemoteException,
        HostPowerOpFailed,
        NotSupported,
        Timedout,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void prepareDestination(
        ManagedObjectReference _this,
        HostVMotionManagerSpec spec,
        ManagedObjectReference pool) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void prepareDestinationEx(
        ManagedObjectReference _this,
        HostVMotionManagerSpec spec,
        ManagedObjectReference pool) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference prepareDestinationEx_Task(
        ManagedObjectReference _this,
        HostVMotionManagerSpec spec,
        ManagedObjectReference pool) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void prepareSource(
        ManagedObjectReference _this,
        HostVMotionManagerSpec spec,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void prepareSourceEx(
        ManagedObjectReference _this,
        HostVMotionManagerSpec spec,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference prepareSourceEx_Task(
        ManagedObjectReference _this,
        HostVMotionManagerSpec spec,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        Timedout,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String prepareToUpgrade(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void promoteDisks(
        ManagedObjectReference _this,
        Boolean unlink,
        List<VirtualDisk> disks)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference promoteDisks_Task(
        ManagedObjectReference _this,
        Boolean unlink,
        List<VirtualDisk> disks)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String publishMediaToLibrary(
        ManagedObjectReference _this,
        ManagedObjectReference srcDc,
        String srcName,
        String libKey,
        ImageLibraryManagerMediaInfo metadata,
        List<KeyValue> headers)
        throws RemoteException,
        FileLocked,
        FileNotFound,
        LibraryFault,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault,
        InvalidArgument
    {
        return null;
    }

    @Override
    public ManagedObjectReference publishMediaToLibrary_Task(
        ManagedObjectReference _this,
        ManagedObjectReference srcDc,
        String srcName,
        String libKey,
        ImageLibraryManagerMediaInfo metadata,
        List<KeyValue> headers)
        throws RemoteException,
        FileLocked,
        FileNotFound,
        LibraryFault,
        FileFault,
        InvalidDatastore,
        RuntimeFault,
        HttpFault,
        SSLVerifyFault,
        InvalidArgument
    {
        return null;
    }

    @Override
    public List<LicenseAssignmentManagerLicenseAssignment> queryAssignedLicenses(
        ManagedObjectReference _this,
        String entityId) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostScsiDisk> queryAvailableDisksForVmfs(
        ManagedObjectReference _this,
        ManagedObjectReference datastore)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<DistributedVirtualSwitchProductSpec> queryAvailableDvsSpec(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostDiagnosticPartition> queryAvailablePartition(
        ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<PerfMetricId> queryAvailablePerfMetric(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        Calendar beginTime,
        Calendar endTime,
        Integer intervalId) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostDateTimeSystemTimeZone> queryAvailableTimeZones(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostBootDeviceInfo queryBootDevices(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public DiskChangeInfo queryChangedDiskAreas(
        ManagedObjectReference _this,
        ManagedObjectReference snapshot,
        Integer deviceKey,
        Long startOffset,
        String changeId)
        throws RemoteException,
        FileFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> queryCompatibleHostForExistingDvs(
        ManagedObjectReference _this,
        ManagedObjectReference container,
        Boolean recursive,
        ManagedObjectReference dvs) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> queryCompatibleHostForNewDvs(
        ManagedObjectReference _this,
        ManagedObjectReference container,
        Boolean recursive,
        DistributedVirtualSwitchProductSpec switchProductSpec)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ComplianceResult> queryComplianceStatus(
        ManagedObjectReference _this,
        List<ManagedObjectReference> profile,
        List<ManagedObjectReference> entity)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public VirtualMachineConfigOption queryConfigOption(
        ManagedObjectReference _this,
        String key,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<VirtualMachineConfigOptionDescriptor> queryConfigOptionDescriptor(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ConfigTarget queryConfigTarget(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public String queryConfiguredModuleOptionString(
        ManagedObjectReference _this,
        String name) throws RemoteException, RuntimeFault, NotFound
    {
        return null;
    }

    @Override
    public HostConnectInfo queryConnectionInfo(
        ManagedObjectReference _this,
        String hostname,
        Integer port,
        String username,
        String password,
        String sslThumbprint)
        throws RemoteException,
        InvalidLogin,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public Calendar queryDateTime(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<DiagnosticManagerLogDescriptor> queryDescriptions(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<DisabledMethodInfo> queryDisabledMethods(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference queryDvsByUuid(
        ManagedObjectReference _this,
        String uuid) throws RemoteException, RuntimeFault, NotFound
    {
        return null;
    }

    @Override
    public List<DistributedVirtualSwitchHostProductSpec> queryDvsCompatibleHostSpec(
        ManagedObjectReference _this,
        DistributedVirtualSwitchProductSpec switchProductSpec)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public DVSManagerDvsConfigTarget queryDvsConfigTarget(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        ManagedObjectReference dvs) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<Event> queryEvents(
        ManagedObjectReference _this,
        EventFilterSpec filter) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ProfileExpressionMetadata> queryExpressionMetadata(
        ManagedObjectReference _this,
        List<String> expressionName) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public String queryFirmwareConfigUploadURL(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostConnectInfo queryHostConnectionInfo(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostPatchManagerResult queryHostPatch(
        ManagedObjectReference _this,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference queryHostPatch_Task(
        ManagedObjectReference _this,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<ProfileMetadata> queryHostProfileMetadata(
        ManagedObjectReference _this,
        List<String> profileName) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<IpPool> queryIpPools(
        ManagedObjectReference _this,
        ManagedObjectReference dc) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<Event> queryLastEvent(
        ManagedObjectReference _this,
        LastEventFilterSpec filter) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<LicenseAvailabilityInfo> queryLicenseSourceAvailability(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public LicenseUsageInfo queryLicenseUsage(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public Long queryMemoryOverhead(
        ManagedObjectReference _this,
        Long memorySize,
        Integer videoRamSize,
        Integer numVcpus) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public Long queryMemoryOverheadEx(
        ManagedObjectReference _this,
        VirtualMachineConfigInfo vmConfigInfo)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<KernelModuleInfo> queryModules(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public VirtualNicManagerNetConfig queryNetConfig(
        ManagedObjectReference _this,
        String nicType)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        InvalidArgument
    {
        return null;
    }

    @Override
    public List<PhysicalNicHintInfo> queryNetworkHint(
        ManagedObjectReference _this,
        List<String> device)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<OptionValue> queryOptions(
        ManagedObjectReference _this,
        String name) throws RemoteException, InvalidName, RuntimeFault
    {
        return null;
    }

    @Override
    public HostDiagnosticPartitionCreateDescription queryPartitionCreateDesc(
        ManagedObjectReference _this,
        String diskUuid,
        String diagnosticType)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<HostDiagnosticPartitionCreateOption> queryPartitionCreateOptions(
        ManagedObjectReference _this,
        String storageType,
        String diagnosticType)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostPathSelectionPolicyOption> queryPathSelectionPolicyOptions(
        ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<PerfEntityMetricBase> queryPerf(
        ManagedObjectReference _this,
        List<PerfQuerySpec> querySpec) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public PerfCompositeMetric queryPerfComposite(
        ManagedObjectReference _this,
        PerfQuerySpec querySpec) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<PerfCounterInfo> queryPerfCounter(
        ManagedObjectReference _this,
        List<Integer> counterId) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<PerfCounterInfo> queryPerfCounterByLevel(
        ManagedObjectReference _this,
        Integer level) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public PerfProviderSummary queryPerfProviderSummary(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ProfilePolicyMetadata> queryPolicyMetadata(
        ManagedObjectReference _this,
        List<String> policyName) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostSystemDebugManagerProcessInfo> queryProcessInfo(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ServiceEndpoint> queryServiceEndpointList(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return ServiceInstanceUtil
            .getInternalServiceContent()
            .getServiceDirectory()
            .getService();
    }

    @Override
    public List<ServiceManagerServiceInfo> queryServiceList(
        ManagedObjectReference _this,
        String serviceName,
        List<String> location) throws RemoteException, RuntimeFault
    {
        return ServiceInstanceUtil
            .getInternalServiceContent()
            .getServiceManager()
            .getService();
    }

    @Override
    public List<HostStorageArrayTypePolicyOption> queryStorageArrayTypePolicyOptions(
        ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<LicenseFeatureInfo> querySupportedFeatures(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostCapability queryTargetCapabilities(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<String> queryUnownedFiles(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostUnresolvedVmfsVolume> queryUnresolvedVmfsVolume(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostUnresolvedVmfsVolume> queryUnresolvedVmfsVolumes(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<Integer> queryUsedVlanIdInDvs(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostVMotionCompatibility> queryVMotionCompatibility(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<ManagedObjectReference> host,
        List<String> compatibility) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<CheckResult> queryVMotionCompatibilityEx(
        ManagedObjectReference _this,
        List<ManagedObjectReference> vm,
        List<ManagedObjectReference> host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference queryVMotionCompatibilityEx_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> vm,
        List<ManagedObjectReference> host) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public Integer queryVirtualDiskFragmentation(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostDiskDimensionsChs queryVirtualDiskGeometry(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String queryVirtualDiskUuid(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<VmfsDatastoreOption> queryVmfsDatastoreCreateOptions(
        ManagedObjectReference _this,
        String devicePath)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<VmfsDatastoreOption> queryVmfsDatastoreExpandOptions(
        ManagedObjectReference _this,
        ManagedObjectReference datastore)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<VmfsDatastoreOption> queryVmfsDatastoreExtendOptions(
        ManagedObjectReference _this,
        ManagedObjectReference datastore,
        String devicePath,
        Boolean suppressExpandCandidates)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<Event> readNextEvents(
        ManagedObjectReference _this,
        Integer maxCount) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<TaskInfo> readNextTasks(
        ManagedObjectReference _this,
        Integer maxCount) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<Event> readPreviousEvents(
        ManagedObjectReference _this,
        Integer maxCount) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<TaskInfo> readPreviousTasks(
        ManagedObjectReference _this,
        Integer maxCount) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void rebootGuest(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public void rebootHost(ManagedObjectReference _this, Boolean force)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference rebootHost_Task(
        ManagedObjectReference _this,
        Boolean force) throws RemoteException, InvalidState, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ClusterHostRecommendation> recommendHostsForVm(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        ManagedObjectReference pool) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void reconfigVM(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec spec)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        InvalidDatastore,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reconfigVM_Task(
        ManagedObjectReference _this,
        VirtualMachineConfigSpec spec)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        InvalidDatastore,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void reconfigureAlarm(ManagedObjectReference _this, AlarmSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
    }

    @Override
    public void reconfigureAutostart(
        ManagedObjectReference _this,
        HostAutoStartManagerConfig spec) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void reconfigureCluster(
        ManagedObjectReference _this,
        ClusterConfigSpec spec,
        Boolean modify) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reconfigureCluster_Task(
        ManagedObjectReference _this,
        ClusterConfigSpec spec,
        Boolean modify) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void reconfigureComputeResource(
        ManagedObjectReference _this,
        ComputeResourceConfigSpec spec,
        Boolean modify) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reconfigureComputeResource_Task(
        ManagedObjectReference _this,
        ComputeResourceConfigSpec spec,
        Boolean modify) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void reconfigureDVPort(
        ManagedObjectReference _this,
        List<DVPortConfigSpec> port)
        throws RemoteException,
        DvsFault,
        ConcurrentAccess,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference reconfigureDVPort_Task(
        ManagedObjectReference _this,
        List<DVPortConfigSpec> port)
        throws RemoteException,
        DvsFault,
        ConcurrentAccess,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void reconfigureDVPortgroup(
        ManagedObjectReference _this,
        DVPortgroupConfigSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        DvsFault,
        ConcurrentAccess,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reconfigureDVPortgroup_Task(
        ManagedObjectReference _this,
        DVPortgroupConfigSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        DvsFault,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void reconfigureDvs(ManagedObjectReference _this, DVSConfigSpec spec)
        throws RemoteException,
        InvalidName,
        DvsNotAuthorized,
        DuplicateName,
        DvsFault,
        ResourceNotAvailable,
        InvalidState,
        ResourceInUse,
        ConcurrentAccess,
        AlreadyExists,
        RuntimeFault,
        NotFound,
        LimitExceeded
    {
    }

    @Override
    public ManagedObjectReference reconfigureDvs_Task(
        ManagedObjectReference _this,
        DVSConfigSpec spec)
        throws RemoteException,
        InvalidName,
        DvsNotAuthorized,
        DuplicateName,
        DvsFault,
        ResourceNotAvailable,
        InvalidState,
        ResourceInUse,
        ConcurrentAccess,
        AlreadyExists,
        RuntimeFault,
        NotFound,
        LimitExceeded
    {
        return null;
    }

    @Override
    public void reconfigureHostForDAS(ManagedObjectReference _this)
        throws RemoteException,
        DasConfigFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reconfigureHostForDAS_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        DasConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void reconfigureScheduledTask(
        ManagedObjectReference _this,
        ScheduledTaskSpec spec)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void reconfigureServiceConsoleReservation(
        ManagedObjectReference _this,
        Long cfgBytes) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void reconfigureSnmpAgent(
        ManagedObjectReference _this,
        HostSnmpConfigSpec spec)
        throws RemoteException,
        InsufficientResourcesFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void reconfigureVirtualMachineReservation(
        ManagedObjectReference _this,
        VirtualMachineMemoryReservationSpec spec)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void reconnectHost(
        ManagedObjectReference _this,
        HostConnectSpec cnxSpec)
        throws RemoteException,
        InvalidName,
        InvalidLogin,
        InvalidState,
        HostConnectFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reconnectHost_Task(
        ManagedObjectReference _this,
        HostConnectSpec cnxSpec)
        throws RemoteException,
        InvalidName,
        InvalidLogin,
        InvalidState,
        HostConnectFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void rectifyDvsHost(
        ManagedObjectReference _this,
        List<ManagedObjectReference> hosts)
        throws RemoteException,
        DvsFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference rectifyDvsHost_Task(
        ManagedObjectReference _this,
        List<ManagedObjectReference> hosts)
        throws RemoteException,
        DvsFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void refresh(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshDVPortState(
        ManagedObjectReference _this,
        List<String> portKeys)
        throws RemoteException,
        DvsFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void refreshDatastore(ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void refreshDatastoreStorageInfo(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshDateTimeSystem(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshFirewall(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshHealthStatusSystem(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshNetworkSystem(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshRecommendation(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshServices(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshStorageInfo(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void refreshStorageSystem(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference registerChildVM(
        ManagedObjectReference _this,
        String path,
        String name,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        InvalidName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public ManagedObjectReference registerChildVM_Task(
        ManagedObjectReference _this,
        String path,
        String name,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        InvalidName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void registerEntity(
        ManagedObjectReference _this,
        String entityId,
        String productName,
        String productVersion,
        String displayName,
        Integer cost,
        Calendar evaluationExpiry,
        String evaluationKey)
        throws RemoteException,
        LicenseEntityAlreadyExists,
        RuntimeFault
    {
    }

    @Override
    public void registerExtension(
        ManagedObjectReference _this,
        Extension extension) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference registerVM(
        ManagedObjectReference _this,
        String path,
        String name,
        Boolean asTemplate,
        ManagedObjectReference pool,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        InvalidName,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public ManagedObjectReference registerVM_Task(
        ManagedObjectReference _this,
        String path,
        String name,
        Boolean asTemplate,
        ManagedObjectReference pool,
        ManagedObjectReference host)
        throws RemoteException,
        VmConfigFault,
        InvalidName,
        DuplicateName,
        FileFault,
        OutOfBounds,
        InsufficientResourcesFault,
        InvalidDatastore,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void releaseLease(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void reload(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void reloadDisks(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<String> target) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reloadDisks_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<String> target) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void relocateVM(
        ManagedObjectReference _this,
        VirtualMachineRelocateSpec spec,
        VirtualMachineMovePriority priority)
        throws RemoteException,
        VmConfigFault,
        Timedout,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        InvalidDatastore,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference relocateVM_Task(
        ManagedObjectReference _this,
        VirtualMachineRelocateSpec spec,
        VirtualMachineMovePriority priority)
        throws RemoteException,
        VmConfigFault,
        Timedout,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        MigrationFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void removeAlarm(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void removeAllSnapshots(ManagedObjectReference _this)
        throws RemoteException,
        SnapshotFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference removeAllSnapshots_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        SnapshotFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void removeAssignedLicense(
        ManagedObjectReference _this,
        String entityId)
        throws RemoteException,
        LicenseEntityNotFound,
        RuntimeFault
    {
    }

    @Override
    public void removeAuthorizationRole(
        ManagedObjectReference _this,
        Integer roleId,
        Boolean failIfUsed)
        throws RemoteException,
        RemoveFailed,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeCustomFieldDef(ManagedObjectReference _this, Integer key)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void removeDatastore(
        ManagedObjectReference _this,
        ManagedObjectReference datastore)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeEndpoint(
        ManagedObjectReference _this,
        ProxyServiceEndpointSpec endpoint)
        throws RemoteException,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeEntityPermission(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        String user,
        Boolean isGroup)
        throws RemoteException,
        AuthMinimumAdminPermission,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeGroup(ManagedObjectReference _this, String groupName)
        throws RemoteException,
        RuntimeFault,
        UserNotFound
    {
    }

    @Override
    public void removeInternetScsiSendTargets(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        List<HostInternetScsiHbaSendTarget> targets)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeInternetScsiStaticTargets(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        List<HostInternetScsiHbaStaticTarget> targets)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeLibrary(ManagedObjectReference _this, String libKey)
        throws RemoteException,
        RuntimeFault,
        InvalidArgument
    {
    }

    @Override
    public void removeLicense(ManagedObjectReference _this, String licenseKey)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void removeLicenseLabel(
        ManagedObjectReference _this,
        String licenseKey,
        String labelKey) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void removePerfInterval(
        ManagedObjectReference _this,
        Integer samplePeriod) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void removePortGroup(ManagedObjectReference _this, String pgName)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeScheduledTask(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void removeServiceConsoleVirtualNic(
        ManagedObjectReference _this,
        String device)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeSnapshot(
        ManagedObjectReference _this,
        Boolean removeChildren)
        throws RemoteException,
        TaskInProgress,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference removeSnapshot_Task(
        ManagedObjectReference _this,
        Boolean removeChildren)
        throws RemoteException,
        TaskInProgress,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void removeTag(ManagedObjectReference _this, List<Tag> tag)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void removeUser(ManagedObjectReference _this, String userName)
        throws RemoteException,
        RuntimeFault,
        UserNotFound
    {
    }

    @Override
    public void removeVirtualNic(ManagedObjectReference _this, String device)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void removeVirtualSwitch(
        ManagedObjectReference _this,
        String vswitchName)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void rename(ManagedObjectReference _this, String newName)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
    }

    @Override
    public void renameCustomFieldDef(
        ManagedObjectReference _this,
        Integer key,
        String name) throws RemoteException, DuplicateName, RuntimeFault
    {
    }

    @Override
    public void renameCustomizationSpec(
        ManagedObjectReference _this,
        String name,
        String newName)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void renameDatastore(ManagedObjectReference _this, String newName)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
    }

    @Override
    public void renameSnapshot(
        ManagedObjectReference _this,
        String name,
        String description)
        throws RemoteException,
        InvalidName,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference rename_Task(
        ManagedObjectReference _this,
        String newName)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void renewAllLeases(ManagedObjectReference _this)
        throws RemoteException,
        Timedout,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void renewLease(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void reparentDisks(
        ManagedObjectReference _this,
        List<VirtualDiskManagerReparentSpec> reparentSpec)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference reparentDisks_Task(
        ManagedObjectReference _this,
        List<VirtualDiskManagerReparentSpec> reparentSpec)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostTpmManagerEncryptedBlob requestIdentity(
        ManagedObjectReference _this,
        String publicKey,
        String label) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void rescanAllHba(ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void rescanHba(ManagedObjectReference _this, String hbaDevice)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void rescanVmfs(ManagedObjectReference _this)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void resetCollector(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void resetEntityPermissions(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        List<Permission> permission)
        throws RemoteException,
        AuthMinimumAdminPermission,
        RuntimeFault,
        NotFound,
        UserNotFound
    {
    }

    @Override
    public void resetFirmwareToFactoryDefaults(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void resetGuestInformation(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public List<ManagedObjectReference> resetListView(
        ManagedObjectReference _this,
        List<ManagedObjectReference> obj) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void resetListViewFromView(
        ManagedObjectReference _this,
        ManagedObjectReference view) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void resetSystemHealthInfo(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void resetVM(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference resetVM_Task(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public HostResignatureRescanResult resignatureUnresolvedVmfsVolume(
        ManagedObjectReference _this,
        HostUnresolvedVmfsResignatureSpec resolutionSpec)
        throws RemoteException,
        HostConfigFault,
        VmfsAmbiguousMount,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference resignatureUnresolvedVmfsVolume_Task(
        ManagedObjectReference _this,
        HostUnresolvedVmfsResignatureSpec resolutionSpec)
        throws RemoteException,
        HostConfigFault,
        VmfsAmbiguousMount,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostUnresolvedVmfsResolutionResult> resolveMultipleUnresolvedVmfsVolumes(
        ManagedObjectReference _this,
        List<HostUnresolvedVmfsResolutionSpec> resolutionSpec)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void restartService(ManagedObjectReference _this, String id)
        throws RemoteException,
        HostConfigFault,
        InvalidState,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void restartServiceConsoleVirtualNic(
        ManagedObjectReference _this,
        String device)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void restoreFirmwareConfiguration(
        ManagedObjectReference _this,
        Boolean force)
        throws RemoteException,
        MismatchedBundle,
        FileFault,
        InvalidState,
        InvalidBundle,
        RuntimeFault
    {
    }

    @Override
    public List<Permission> retrieveAllPermissions(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<EventArgDesc> retrieveArgumentDescription(
        ManagedObjectReference _this,
        String eventTypeId) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference retrieveBackupAgent(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<String> retrieveDVPortgroup(
        ManagedObjectReference _this,
        String switchUuid)
        throws RemoteException,
        PlatformConfigFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<HostDVPortgroupConfigSpec> retrieveDVPortgroupConfigSpec(
        ManagedObjectReference _this,
        String switchUuid,
        List<String> portgroupKey)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public ClusterDasAdvancedRuntimeInfo retrieveDasAdvancedRuntimeInfo(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostDiskPartitionInfo> retrieveDiskPartitionInfo(
        ManagedObjectReference _this,
        List<String> devicePath) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostDVSConfigSpec retrieveDvsConfigSpec(
        ManagedObjectReference _this,
        String switchUuid)
        throws RemoteException,
        PlatformConfigFault,
        DvsFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public List<Permission> retrieveEntityPermissions(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        Boolean inherited) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ManagedObjectReference> retrieveEntityScheduledTask(
        ManagedObjectReference _this,
        ManagedObjectReference entity) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostInternalCapability retrieveInternalCapability(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public HostInternalConfigManager retrieveInternalConfigManager(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public InternalServiceInstanceContent retrieveInternalContent(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return ServiceInstanceUtil.getInternalServiceContent();
    }

    @Override
    public List<ManagedObjectReference> retrieveObjectScheduledTask(
        ManagedObjectReference _this,
        ManagedObjectReference obj) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference retrievePatchManager(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ProductComponentInfo> retrieveProductComponents(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public List<ObjectContent> retrieveProperties(
        ManagedObjectReference _this,
        List<PropertyFilterSpec> specSet)
        throws RemoteException,
        InvalidProperty,
        RuntimeFault
    {
        return PropertyCollectorUtil.retrieveProperties(_this, specSet);
    }

    @Override
    public List<Permission> retrieveRolePermissions(
        ManagedObjectReference _this,
        Integer roleId) throws RemoteException, RuntimeFault, NotFound
    {
        return null;
    }

    @Override
    public ServiceContent retrieveServiceContent(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
        if (!isUnitTesting
            && getSessionKey().equals(Constants.ANON_SESSION_KEY))
        {
            setSessionKey(Constants.ANON_SESSION_KEY);
        }

        return ServiceInstanceUtil.getServiceInstance().getContent();
    }

    @Override
    public List<UserSearchResult> retrieveUserGroups(
        ManagedObjectReference _this,
        String domain,
        String searchStr,
        String belongsToGroup,
        String belongsToUser,
        Boolean exactMatch,
        Boolean findUsers,
        Boolean findGroups) throws RemoteException, RuntimeFault, NotFound
    {
        return null;
    }

    @Override
    public void revertToCurrentSnapshot(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        Boolean suppressPowerOn)
        throws RemoteException,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference revertToCurrentSnapshot_Task(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        Boolean suppressPowerOn)
        throws RemoteException,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void revertToSnapshot(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        Boolean suppressPowerOn)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference revertToSnapshot_Task(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        Boolean suppressPowerOn)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void rewindCollector(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void runScheduledTask(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public List<HostPatchManagerStatus> scanHostPatch(
        ManagedObjectReference _this,
        HostPatchManagerLocator repository,
        List<String> updateID)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        RuntimeFault,
        PatchMetadataInvalid
    {
        return null;
    }

    @Override
    public HostPatchManagerResult scanHostPatchV2(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference scanHostPatchV2_Task(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference scanHostPatch_Task(
        ManagedObjectReference _this,
        HostPatchManagerLocator repository,
        List<String> updateID)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        RuntimeFault,
        PatchMetadataInvalid
    {
        return null;
    }

    @Override
    public HostDatastoreBrowserSearchResults searchDatastore(
        ManagedObjectReference _this,
        String datastorePath,
        HostDatastoreBrowserSearchSpec searchSpec)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<HostDatastoreBrowserSearchResults> searchDatastoreSubFolders(
        ManagedObjectReference _this,
        String datastorePath,
        HostDatastoreBrowserSearchSpec searchSpec)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference searchDatastoreSubFolders_Task(
        ManagedObjectReference _this,
        String datastorePath,
        HostDatastoreBrowserSearchSpec searchSpec)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference searchDatastore_Task(
        ManagedObjectReference _this,
        String datastorePath,
        HostDatastoreBrowserSearchSpec searchSpec)
        throws RemoteException,
        FileFault,
        InvalidDatastore,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void selectActivePartition(
        ManagedObjectReference _this,
        HostScsiDiskPartition partition)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void selectVnic(ManagedObjectReference _this, String device)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void selectVnicForNicType(
        ManagedObjectReference _this,
        String nicType,
        String device)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        InvalidArgument
    {
    }

    @Override
    public void sendTestNotification(ManagedObjectReference _this)
        throws RemoteException,
        InsufficientResourcesFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void sendWakeOnLanPacket(
        ManagedObjectReference _this,
        List<HostWakeOnLanConfig> wakeOnLanConfig)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public Boolean sessionIsActive(
        ManagedObjectReference _this,
        String sessionID,
        String userName) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void setAlarmStatus(
        ManagedObjectReference _this,
        ManagedObjectReference alarm,
        ManagedObjectReference entity,
        ManagedEntityStatus status) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void setCollectorPageSize(
        ManagedObjectReference _this,
        Integer maxCount) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void setCustomValue(
        ManagedObjectReference _this,
        String key,
        String value) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void setDisplayTopology(
        ManagedObjectReference _this,
        List<VirtualMachineDisplayTopology> displays)
        throws RemoteException,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public void setEntityPermissions(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        List<Permission> permission)
        throws RemoteException,
        AuthMinimumAdminPermission,
        RuntimeFault,
        NotFound,
        UserNotFound
    {
    }

    @Override
    public void setExtensionCertificate(
        ManagedObjectReference _this,
        String extensionKey,
        String certificatePem)
        throws RemoteException,
        NoClientCertificate,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void setField(
        ManagedObjectReference _this,
        ManagedObjectReference entity,
        Integer key,
        String value) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void setLicenseEdition(
        ManagedObjectReference _this,
        ManagedObjectReference host,
        String featureKey)
        throws RemoteException,
        LicenseServerUnavailable,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void setLocale(ManagedObjectReference _this, String locale)
        throws RemoteException,
        InvalidLocale,
        RuntimeFault
    {
    }

    @Override
    public void setMultipathLunPolicy(
        ManagedObjectReference _this,
        String lunId,
        HostMultipathInfoLogicalUnitPolicy policy)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void setPublicKey(
        ManagedObjectReference _this,
        String extensionKey,
        String publicKey) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void setScreenResolution(
        ManagedObjectReference _this,
        Integer width,
        Integer height)
        throws RemoteException,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public void setTaskDescription(
        ManagedObjectReference _this,
        LocalizableMessage description) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void setTaskState(
        ManagedObjectReference _this,
        TaskInfoState state,
        Object result,
        LocalizedMethodFault fault)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void setVirtualDiskUuid(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        String uuid) throws RemoteException, FileFault, RuntimeFault
    {
    }

    @Override
    public void shrinkVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        Boolean copy) throws RemoteException, FileFault, RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference shrinkVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter,
        Boolean copy) throws RemoteException, FileFault, RuntimeFault
    {
        return null;
    }

    @Override
    public void shutdownGuest(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public void shutdownHost(ManagedObjectReference _this, Boolean force)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference shutdownHost_Task(
        ManagedObjectReference _this,
        Boolean force) throws RemoteException, InvalidState, RuntimeFault
    {
        return null;
    }

    @Override
    public HostPatchManagerResult stageHostPatch(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        List<String> vibUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference stageHostPatch_Task(
        ManagedObjectReference _this,
        List<String> metaUrls,
        List<String> bundleUrls,
        List<String> vibUrls,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        RequestCanceled,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void standbyGuest(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public void startBackup(
        ManagedObjectReference _this,
        Boolean generateManifests,
        List<String> volumes)
        throws RemoteException,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference startRecording(
        ManagedObjectReference _this,
        String name,
        String description)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        HostIncompatibleForRecordReplay,
        RecordReplayDisabled,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference startRecording_Task(
        ManagedObjectReference _this,
        String name,
        String description)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        HostIncompatibleForRecordReplay,
        RecordReplayDisabled,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void startReplaying(
        ManagedObjectReference _this,
        ManagedObjectReference replaySnapshot)
        throws RemoteException,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        HostIncompatibleForRecordReplay,
        RecordReplayDisabled,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public ManagedObjectReference startReplaying_Task(
        ManagedObjectReference _this,
        ManagedObjectReference replaySnapshot)
        throws RemoteException,
        VmConfigFault,
        SnapshotFault,
        TaskInProgress,
        HostIncompatibleForRecordReplay,
        RecordReplayDisabled,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void startService(ManagedObjectReference _this, String id)
        throws RemoteException,
        HostConfigFault,
        InvalidState,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void stopRecording(ManagedObjectReference _this)
        throws RemoteException,
        SnapshotFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference stopRecording_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        SnapshotFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void stopReplaying(ManagedObjectReference _this)
        throws RemoteException,
        SnapshotFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference stopReplaying_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        SnapshotFault,
        TaskInProgress,
        FileFault,
        InvalidState,
        InvalidPowerState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void stopService(ManagedObjectReference _this, String id)
        throws RemoteException,
        HostConfigFault,
        InvalidState,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public List<PerfEntityMetricCSV> summarizeStats(
        ManagedObjectReference _this,
        List<PerfQuerySpec> querySpec) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void suspendVM(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference suspendVM_Task(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void syncFirmwareConfiguration(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault,
        TooManyWrites
    {
    }

    @Override
    public void terminateFaultTolerantVM(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference terminateFaultTolerantVM_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void terminateSession(
        ManagedObjectReference _this,
        List<String> sessionId) throws RemoteException, RuntimeFault, NotFound
    {
    }

    @Override
    public ManagedObjectReference transitionalEVCManager(
        ManagedObjectReference _this) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void turnOffFaultToleranceForVM(ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference turnOffFaultToleranceForVM_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        VmFaultToleranceIssue,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void unassignUserFromGroup(
        ManagedObjectReference _this,
        String user,
        String group) throws RemoteException, RuntimeFault, UserNotFound
    {
    }

    @Override
    public HostPatchManagerResult uninstallHostPatch(
        ManagedObjectReference _this,
        List<String> bulletinIds,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public ManagedObjectReference uninstallHostPatch_Task(
        ManagedObjectReference _this,
        List<String> bulletinIds,
        HostPatchManagerPatchManagerOperationSpec spec)
        throws RemoteException,
        PlatformConfigFault,
        TaskInProgress,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void uninstallService(ManagedObjectReference _this, String id)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void unmountForceMountedVmfsVolume(
        ManagedObjectReference _this,
        String vmfsUuid)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void unmountToolsInstaller(ManagedObjectReference _this)
        throws RemoteException,
        VmConfigFault,
        InvalidState,
        RuntimeFault
    {
    }

    @Override
    public void unregisterAndDestroy(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference unregisterAndDestroy_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void unregisterEntity(ManagedObjectReference _this, String entityId)
        throws RemoteException,
        LicenseEntityNotFound,
        RuntimeFault
    {
    }

    @Override
    public void unregisterExtension(
        ManagedObjectReference _this,
        String extensionKey) throws RemoteException, RuntimeFault, NotFound
    {
    }

    @Override
    public void unregisterVApp(ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference unregisterVApp_Task(
        ManagedObjectReference _this)
        throws RemoteException,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void unregisterVM(ManagedObjectReference _this)
        throws RemoteException,
        InvalidPowerState,
        RuntimeFault
    {
    }

    @Override
    public LicenseManagerLicenseInfo updateAssignedLicense(
        ManagedObjectReference _this,
        String entity,
        String licenseKey,
        String entityDisplayName)
        throws RemoteException,
        LicenseEntityNotFound,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void updateAuthorizationRole(
        ManagedObjectReference _this,
        Integer roleId,
        String newName,
        List<String> privIds)
        throws RemoteException,
        InvalidName,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateBootDevice(ManagedObjectReference _this, String key)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void updateChildResourceConfiguration(
        ManagedObjectReference _this,
        List<ResourceConfigSpec> spec)
        throws RemoteException,
        InvalidState,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public void updateClusterProfile(
        ManagedObjectReference _this,
        ClusterProfileConfigSpec config)
        throws RemoteException,
        DuplicateName,
        RuntimeFault
    {
    }

    @Override
    public void updateConfig(
        ManagedObjectReference _this,
        String name,
        ResourceConfigSpec config)
        throws RemoteException,
        InvalidName,
        DuplicateName,
        ConcurrentAccess,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public void updateConsoleIpRouteConfig(
        ManagedObjectReference _this,
        HostIpRouteConfig config)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void updateDateTime(ManagedObjectReference _this, Calendar dateTime)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void updateDateTimeConfig(
        ManagedObjectReference _this,
        HostDateTimeConfig config)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void updateDefaultPolicy(
        ManagedObjectReference _this,
        HostFirewallDefaultPolicy defaultPolicy)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void updateDiskPartitions(
        ManagedObjectReference _this,
        String devicePath,
        HostDiskPartitionSpec spec)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateDnsConfig(
        ManagedObjectReference _this,
        HostDnsConfig config)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateDvsCapability(
        ManagedObjectReference _this,
        DVSCapability capability) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updateExtension(
        ManagedObjectReference _this,
        Extension extension) throws RemoteException, RuntimeFault, NotFound
    {
    }

    @Override
    public void updateFeatureInUse(
        ManagedObjectReference _this,
        String entityId,
        String feature,
        Boolean inUse)
        throws RemoteException,
        LicenseEntityNotFound,
        RuntimeFault
    {
    }

    @Override
    public void updateFlags(ManagedObjectReference _this, HostFlagInfo flagInfo)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void updateHostProfile(
        ManagedObjectReference _this,
        HostProfileConfigSpec config)
        throws RemoteException,
        DuplicateName,
        ProfileUpdateFailed,
        RuntimeFault
    {
    }

    @Override
    public void updateInternetScsiAdvancedOptions(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        HostInternetScsiHbaTargetSet targetSet,
        List<HostInternetScsiHbaParamValue> options)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateInternetScsiAlias(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        String iScsiAlias)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateInternetScsiAuthenticationProperties(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        HostInternetScsiHbaAuthenticationProperties authenticationProperties,
        HostInternetScsiHbaTargetSet targetSet)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateInternetScsiDigestProperties(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        HostInternetScsiHbaTargetSet targetSet,
        HostInternetScsiHbaDigestProperties digestProperties)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateInternetScsiDiscoveryProperties(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        HostInternetScsiHbaDiscoveryProperties discoveryProperties)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateInternetScsiIPProperties(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        HostInternetScsiHbaIPProperties ipProperties)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateInternetScsiName(
        ManagedObjectReference _this,
        String iScsiHbaDevice,
        String iScsiName)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateIpConfig(
        ManagedObjectReference _this,
        HostIpConfig ipConfig)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateIpPool(
        ManagedObjectReference _this,
        ManagedObjectReference dc,
        IpPool pool) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updateIpRouteConfig(
        ManagedObjectReference _this,
        HostIpRouteConfig config)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void updateIpRouteTableConfig(
        ManagedObjectReference _this,
        HostIpRouteTableConfig config)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void updateIpmi(ManagedObjectReference _this, HostIpmiInfo ipmiInfo)
        throws RemoteException,
        InvalidIpmiMacAddress,
        InvalidIpmiLoginInfo,
        RuntimeFault
    {
    }

    @Override
    public void updateLibrary(
        ManagedObjectReference _this,
        String libKey,
        String libName,
        String sslThumbprint)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault,
        InvalidArgument
    {
    }

    @Override
    public LicenseManagerLicenseInfo updateLicense(
        ManagedObjectReference _this,
        String licenseKey,
        List<KeyValue> labels) throws RemoteException, RuntimeFault
    {
        return null;
    }

    @Override
    public void updateLicenseLabel(
        ManagedObjectReference _this,
        String licenseKey,
        String labelKey,
        String labelValue) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updateLocalSwapDatastore(
        ManagedObjectReference _this,
        ManagedObjectReference datastore)
        throws RemoteException,
        InaccessibleDatastore,
        RuntimeFault,
        DatastoreNotWritableOnHost
    {
    }

    @Override
    public void updateManagementServerIp(
        ManagedObjectReference _this,
        String serverIp) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updateMemMirrorFlag(ManagedObjectReference _this, Boolean enable)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void updateModuleOptionString(
        ManagedObjectReference _this,
        String name,
        String options) throws RemoteException, RuntimeFault, NotFound
    {
    }

    @Override
    public HostNetworkConfigResult updateNetworkConfig(
        ManagedObjectReference _this,
        HostNetworkConfig config,
        String changeMode)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
        return null;
    }

    @Override
    public void updateOptions(
        ManagedObjectReference _this,
        List<OptionValue> changedValue)
        throws RemoteException,
        InvalidName,
        RuntimeFault
    {
    }

    @Override
    public void updatePassthruConfig(
        ManagedObjectReference _this,
        List<HostPciPassthruConfig> config)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault
    {
    }

    @Override
    public void updatePerfInterval(
        ManagedObjectReference _this,
        PerfInterval interval) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updatePhysicalNicLinkSpeed(
        ManagedObjectReference _this,
        String device,
        PhysicalNicLinkInfo linkSpeed)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updatePortGroup(
        ManagedObjectReference _this,
        String pgName,
        HostPortGroupSpec portgrp)
        throws RemoteException,
        HostConfigFault,
        AlreadyExists,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateProgress(ManagedObjectReference _this, Integer percentDone)
        throws RemoteException,
        InvalidState,
        OutOfBounds,
        RuntimeFault
    {
    }

    @Override
    public void updateReferenceHost(
        ManagedObjectReference _this,
        ManagedObjectReference host) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updateScsiLunDisplayName(
        ManagedObjectReference _this,
        String lunUuid,
        String displayName)
        throws RemoteException,
        HostConfigFault,
        InvalidName,
        DuplicateName,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateServiceConsoleVirtualNic(
        ManagedObjectReference _this,
        String device,
        HostVirtualNicSpec nic)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateServiceMessage(
        ManagedObjectReference _this,
        String message) throws RemoteException, RuntimeFault
    {
    }

    @Override
    public void updateServicePolicy(
        ManagedObjectReference _this,
        String id,
        String policy)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateSoftwareInternetScsiEnabled(
        ManagedObjectReference _this,
        Boolean enabled) throws RemoteException, HostConfigFault, RuntimeFault
    {
    }

    @Override
    public void updateSslThumbprintInfo(
        ManagedObjectReference _this,
        HostSslThumbprintInfo sslThumbprintInfo)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void updateSystemResources(
        ManagedObjectReference _this,
        HostSystemResourceInfo resourceInfo)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void updateUsage(
        ManagedObjectReference _this,
        String entityId,
        Integer value)
        throws RemoteException,
        LicenseEntityNotFound,
        RuntimeFault
    {
    }

    @Override
    public void updateUser(ManagedObjectReference _this, HostAccountSpec user)
        throws RemoteException,
        AlreadyExists,
        RuntimeFault,
        UserNotFound
    {
    }

    @Override
    public void updateVAppConfig(
        ManagedObjectReference _this,
        VAppConfigSpec spec)
        throws RemoteException,
        InvalidName,
        VmConfigFault,
        DuplicateName,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        InvalidDatastore,
        InsufficientResourcesFault,
        RuntimeFault
    {
    }

    @Override
    public void updateVirtualNic(
        ManagedObjectReference _this,
        String device,
        HostVirtualNicSpec nic)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void updateVirtualSwitch(
        ManagedObjectReference _this,
        String vswitchName,
        HostVirtualSwitchSpec spec)
        throws RemoteException,
        HostConfigFault,
        ResourceInUse,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void upgrade(
        ManagedObjectReference _this,
        String executable,
        String signatureFile)
        throws RemoteException,
        AgentInstallFailed,
        RuntimeFault
    {
    }

    @Override
    public void upgradeDestination(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<HostVMotionManagerReparentSpec> reparentSpec)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference upgradeDestination_Task(
        ManagedObjectReference _this,
        ManagedObjectReference vm,
        List<HostVMotionManagerReparentSpec> reparentSpec)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void upgradeTools(
        ManagedObjectReference _this,
        String installerOptions)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        VmToolsUpgradeFault,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference upgradeTools_Task(
        ManagedObjectReference _this,
        String installerOptions)
        throws RemoteException,
        VmConfigFault,
        TaskInProgress,
        VmToolsUpgradeFault,
        InvalidState,
        ToolsUnavailable,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void upgradeVM(ManagedObjectReference _this, String version)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        AlreadyUpgraded,
        RuntimeFault,
        NoDiskFound
    {
    }

    @Override
    public ManagedObjectReference upgradeVM_Task(
        ManagedObjectReference _this,
        String version)
        throws RemoteException,
        TaskInProgress,
        InvalidState,
        AlreadyUpgraded,
        RuntimeFault,
        NoDiskFound
    {
        return null;
    }

    @Override
    public void upgradeVmLayout(ManagedObjectReference _this)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public void upgradeVmfs(ManagedObjectReference _this, String vmfsPath)
        throws RemoteException,
        HostConfigFault,
        RuntimeFault,
        NotFound
    {
    }

    @Override
    public void uploadLicenseFile(
        ManagedObjectReference _this,
        LicenseAssignmentManagerLicenseFileDescriptor licenseFile)
        throws RemoteException,
        RuntimeFault
    {
    }

    @Override
    public OvfValidateHostResult validateHost(
        ManagedObjectReference _this,
        String ovfDescriptor,
        ManagedObjectReference host,
        OvfValidateHostParams vhp)
        throws RemoteException,
        TaskInProgress,
        FileFault,
        InvalidState,
        ConcurrentAccess,
        RuntimeFault
    {
        return null;
    }

    @Override
    public List<Event> validateMigration(
        ManagedObjectReference _this,
        List<ManagedObjectReference> vm,
        VirtualMachinePowerState state,
        List<String> testType,
        ManagedObjectReference pool,
        ManagedObjectReference host)
        throws RemoteException,
        InvalidState,
        RuntimeFault
    {
        return null;
    }

    @Override
    public String verifyCredential(
        ManagedObjectReference _this,
        HostTpmManagerEncryptedBlob encryptedCredential)
        throws RemoteException,
        RuntimeFault
    {
        return null;
    }

    @Override
    public UpdateSet waitForUpdates(ManagedObjectReference _this, String version)
        throws RemoteException,
        InvalidCollectorVersion,
        RuntimeFault
    {
        return PropertyCollectorUtil.waitForUpdates(
            getSessionKey(),
            _this,
            version);
    }

    @Override
    public CustomizationSpecItem xmlToCustomizationSpecItem(
        ManagedObjectReference _this,
        String specItemXml)
        throws RemoteException,
        CustomizationFault,
        RuntimeFault
    {
        return null;
    }

    @Override
    public void zeroFillVirtualDisk(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
    }

    @Override
    public ManagedObjectReference zeroFillVirtualDisk_Task(
        ManagedObjectReference _this,
        String name,
        ManagedObjectReference datacenter)
        throws RemoteException,
        FileFault,
        RuntimeFault
    {
        return null;
    }
}
