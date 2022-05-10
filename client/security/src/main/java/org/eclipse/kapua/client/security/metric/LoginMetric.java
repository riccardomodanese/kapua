/*******************************************************************************
 * Copyright (c) 2017, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.client.security.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;

import org.eclipse.kapua.commons.metric.MetricServiceFactory;
import org.eclipse.kapua.commons.metric.MetricsLabel;
import org.eclipse.kapua.commons.metric.MetricsService;

public class LoginMetric {

    private static final LoginMetric LOGIN_METRIC = new LoginMetric();

    private static final String CLIENTS = "clients";
    private static final String INTERNAL_CONNECTOR = "internal_connector";
    private static final String SUCCESS_FROM_CACHE = "success_from_cache";
    private static final String FAILURE_PASSWORD = MetricsLabel.FAILURE + "_password";
    private static final String FAILURE_CLIENT_ID = MetricsLabel.FAILURE + "_client_id";
    private static final String ATTEMPT = "attempt";
    private static final String CONNECTION_CLEANUP = "connection_cleanup";
    private static final String CRITICAL = "critical";
    private static final String LOGIN_CLOSED_CONNECTION = "lonig_closed_connection";
    private static final String DUPLICATE_SESSION_METADATA = "duplicate_session_metadata";
    private static final String STEALING_LINK = "stealing_link";
    private static final String DISCONNECT_BY_EVENT = MetricsLabel.DISCONNECT + "_by_event";
    private static final String ILLEGAL_STATE = "illegal_state";
    private static final String ADD_CONNECTION = "add_connection";
    private static final String USER = "user";
    private static final String SHIRO = "shiro";
    private static final String CHECK_ACCESS = "check_access";
    private static final String FIND_DEVICE_CONNECTION = "find_device_connection";
    private static final String UPDATE_DEVICE_CONNECTION = "update_device_connection";
    private static final String LOGOUT = "logout";
    private static final String RAISE_LIFECYCLE_EVENT = "raise_lifecycle_event";
    private static final String REMOVE_CONNECTION = "remove_connection";

    private Counter externalAttempt;
    private Counter externalSuccess;
    private Counter externalFailure;
    private Counter successFromCache;
    private Counter internalConnectorAttempt;
    private Counter internalConnectorSuccess;
    private Counter internalConnectorFailure;
    private Counter cleanupConnectionFailure;
    //other failures
    private Counter cleanupConnectionNullSession;
    private Counter criticalFailure;
    private Counter loginClosedConnectionFailure;
    private Counter duplicateSessionMetadataFailure;

    private Counter userAttempt;
    private Counter userConnected;
    private Counter userDisconnected;
    private Counter userStealingLinkConnect;
    private Counter userStealingLinkDisconnect;
    private Counter userIllegalStateDisconnect;

    private Counter invalidUserPassword;
    private Counter invalidClientId;

    private Counter adminAttempt;
    private Counter adminConnected;
    private Counter adminDisconnected;
    private Counter adminStealingLinkConnect;
    private Counter adminStealingLinkDisconnect;

    private Counter disconnectByEvent;

    private Timer addConnectionTime;
    private Timer normalUserTime;
    private Timer shiroLoginTime;
    private Timer checkAccessTime;
    private Timer findDeviceConnectionTime;
    private Timer updateDeviceConnectionTime;
    private Timer shiroLogoutTime;
    private Timer raiseLifecycleEventTime;
    private Timer removeConnectionTime;

    public static LoginMetric getInstance() {
        return LOGIN_METRIC;
    }

    private LoginMetric() {
        MetricsService metricsService = MetricServiceFactory.getInstance();
        // login by connectors
        externalAttempt = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.ATTEMPT, MetricsLabel.COUNT);
        externalSuccess = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.SUCCESS, MetricsLabel.COUNT);
        externalFailure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        successFromCache = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SUCCESS_FROM_CACHE, MetricsLabel.COUNT);
        internalConnectorAttempt = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, INTERNAL_CONNECTOR, MetricsLabel.ATTEMPT, MetricsLabel.COUNT);
        internalConnectorSuccess = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, INTERNAL_CONNECTOR, MetricsLabel.SUCCESS, MetricsLabel.COUNT);
        internalConnectorFailure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, INTERNAL_CONNECTOR, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        cleanupConnectionFailure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CONNECTION_CLEANUP, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        cleanupConnectionNullSession = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CONNECTION_CLEANUP, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        criticalFailure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CRITICAL, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        loginClosedConnectionFailure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, LOGIN_CLOSED_CONNECTION, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        duplicateSessionMetadataFailure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, DUPLICATE_SESSION_METADATA, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        //logins by user type
        userConnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CLIENTS, MetricsLabel.CONNECT, MetricsLabel.COUNT);
        userDisconnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CLIENTS, MetricsLabel.DISCONNECT, MetricsLabel.COUNT);
        userStealingLinkConnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, STEALING_LINK, MetricsLabel.CONNECT, MetricsLabel.COUNT);
        userStealingLinkDisconnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, STEALING_LINK, MetricsLabel.DISCONNECT, MetricsLabel.COUNT);
        userIllegalStateDisconnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ILLEGAL_STATE, MetricsLabel.DISCONNECT, MetricsLabel.COUNT);
        adminAttempt = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADMIN, MetricsLabel.COUNT);
        adminConnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADMIN, MetricsLabel.CONNECT, MetricsLabel.COUNT);
        adminDisconnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADMIN, MetricsLabel.DISCONNECT, MetricsLabel.COUNT);
        adminStealingLinkConnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADMIN, STEALING_LINK, MetricsLabel.CONNECT, MetricsLabel.COUNT);
        adminStealingLinkDisconnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, ADMIN, STEALING_LINK, MetricsLabel.DISCONNECT, MetricsLabel.COUNT);

        invalidUserPassword = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, PASSWORD, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        invalidClientId = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CLIENT_ID, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        disconnectByEvent = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, DISCONNECT_BY_EVENT, MetricsLabel.DISCONNECT, MetricsLabel.COUNT);

        // login time
        addConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADD_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        normalUserTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, USER, MetricsLabel.TIME, MetricsLabel.SECONDS);
        shiroLoginTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SHIRO, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.TIME, MetricsLabel.SECONDS);
        checkAccessTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CHECK_ACCESS, MetricsLabel.TIME, MetricsLabel.SECONDS);
        findDeviceConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, FIND_DEVICE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        updateDeviceConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, UPDATE_DEVICE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        shiroLogoutTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SHIRO, LOGOUT, MetricsLabel.TIME, MetricsLabel.SECONDS);
        raiseLifecycleEventTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, RAISE_LIFECYCLE_EVENT, MetricsLabel.TIME, MetricsLabel.SECONDS);
        removeConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, REMOVE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
    }

    public Counter getExternalAttempt() {
        return externalAttempt;
    }

    public Counter getExternalSuccess() {
        return externalSuccess;
    }

    public Counter getExternalFailure() {
        return externalFailure;
    }

    public Counter getSuccessFromCache() {
        return successFromCache;
    }

    public Counter getInternalConnectorAttempt() {
        return internalConnectorAttempt;
    }

    public Counter getInternalConnectorSuccess() {
        return internalConnectorSuccess;
    }

    public Counter getInternalConnectorFailure() {
        return internalConnectorFailure;
    }

    public Counter getCleanupConnectionFailure() {
        return cleanupConnectionFailure;
    }

    public Counter getCleanupConnectionNullSession() {
        return cleanupConnectionNullSession;
    }

    public Counter getCriticalFailure() {
        return criticalFailure;
    }

    public Counter getLoginClosedConnectionFailure() {
        return loginClosedConnectionFailure;
    }

    public Counter getDuplicateSessionMetadataFailure() {
        return duplicateSessionMetadataFailure;
    }

    public Counter getAdminAttempt() {
        return adminAttempt;
    }

    public Counter getAdminConnected() {
        return adminConnected;
    }

    public Counter getAdminDisconnected() {
        return adminDisconnected;
    }

    public Counter getAdminStealingLinkConnect() {
        return adminStealingLinkConnect;
    }

    public Counter getAdminStealingLinkDisconnect() {
        return adminStealingLinkDisconnect;
    }

    public Counter getUserAttempt() {
        return userAttempt;
    }

    public Counter getUserConnected() {
        return userConnected;
    }

    public Counter getUserDisconnected() {
        return userDisconnected;
    }

    public Counter getUserStealingLinkConnect() {
        return userStealingLinkConnect;
    }

    public Counter getUserStealingLinkDisconnect() {
        return userStealingLinkDisconnect;
    }

    public Counter getUserIllegalStateDisconnect() {
        return userIllegalStateDisconnect;
    }

    public Counter getInvalidUserPassword() {
        return invalidUserPassword;
    }

    //TODO link this metric to the clientId validation on login
    public Counter getInvalidClientId() {
        return invalidClientId;
    }

    public Counter getDisconnectByEvent() {
        return disconnectByEvent;
    }

    public Timer getAddConnectionTime() {
        return addConnectionTime;
    }

    public Timer getNormalUserTime() {
        return normalUserTime;
    }

    public Timer getShiroLoginTime() {
        return shiroLoginTime;
    }

    public Timer getCheckAccessTime() {
        return checkAccessTime;
    }

    public Timer getFindDeviceConnectionTime() {
        return findDeviceConnectionTime;
    }

    public Timer getUpdateDeviceConnectionTime() {
        return updateDeviceConnectionTime;
    }

    public Timer getShiroLogoutTime() {
        return shiroLogoutTime;
    }

    public Timer getRaiseLifecycleEventTime() {
        return raiseLifecycleEventTime;
    }

    public Timer getRemoveConnectionTime() {
        return removeConnectionTime;
    }

}
