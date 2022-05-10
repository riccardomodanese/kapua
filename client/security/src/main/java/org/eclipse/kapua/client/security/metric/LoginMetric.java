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
    private static final String KAPUASYS = "kapuasys";
    private static final String CONNECT = "connect";
    private static final String CONNECTED = "connected";
    private static final String DISCONNECT = "disconnect";
    private static final String DISCONNECTED = "disconnected";
    private static final String INTERNAL_CONNECTOR = "internal_connector";
    private static final String SUCCESS_FROM_CACHE = "success_from_cache";
    private static final String FAILURE_PASSWORD = MetricsLabel.FAILURE + "_password";
    private static final String FAILURE_CLIENT_ID = MetricsLabel.FAILURE + "_client_id";
    private static final String NORMAL = "normal";
    private static final String STEALING_LINK = "stealing_link";
    private static final String ADMIN_STEALING_LINK = "admin_" + STEALING_LINK;
    private static final String DISCONNECT_BY_EVENT = DISCONNECT + "_by_event";
    private static final String ILLEGAL_STATE = "illegal_state";
    private static final String ADD_CONNECTION = "add_connection";
    private static final String USER = "user";
    private static final String SHIRO = "shiro";
    private static final String CHECK_ACCESS = "check_access";
    private static final String FIND_DEVICE_CONNECTION = "find_device_connection";
    private static final String UPDATE_DEVICE_CONNECTION = "update_device_connection";
    private static final String LOGOUT = "logout";
    private static final String SEND_LOGIN_UPDATE = "send_login_update";
    private static final String REMOVE_CONNECTION = "remove_connection";

    private Counter attempt;
    private Counter success;
    private Counter successFromCache;
    private Counter failure;
    private Counter connected;
    private Counter disconnected;
    private Counter invalidUserPassword;
    private Counter invalidClientId;
    private Counter adminAttempt;
    private Counter adminConnected;
    private Counter adminDisconnected;
    private Counter internalConnectorAttempt;
    private Counter internalConnectorConnected;
    private Counter internalConnectorDisconnected;
    private Counter stealingLinkConnect;
    private Counter stealingLinkDisconnect;
    private Counter adminStealingLinkConnect;
    private Counter adminStealingLinkDisconnect;
    private Counter disconnectByEvent;
    private Counter illegalStateDisconnect;

    private Timer addConnectionTime;
    private Timer normalUserTime;
    private Timer shiroLoginTime;
    private Timer checkAccessTime;
    private Timer findDeviceConnectionTime;
    private Timer updateDeviceConnectionTime;
    private Timer shiroLogoutTime;
    private Timer sendLoginUpdateMsgTime;
    private Timer removeConnectionTime;

    public static LoginMetric getInstance() {
        return LOGIN_METRIC;
    }

    private LoginMetric() {
        MetricsService metricsService = MetricServiceFactory.getInstance();
        // login
        attempt = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, NORMAL, MetricsLabel.COUNT);
        success = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.SUCCESS, MetricsLabel.COUNT);
        successFromCache = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SUCCESS_FROM_CACHE, MetricsLabel.COUNT);
        failure = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.FAILURE, MetricsLabel.COUNT);
        connected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CLIENTS, CONNECTED, MetricsLabel.COUNT);
        disconnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CLIENTS, DISCONNECTED, MetricsLabel.COUNT);
        invalidUserPassword = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, FAILURE_PASSWORD, MetricsLabel.COUNT);
        invalidClientId = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, FAILURE_CLIENT_ID, MetricsLabel.COUNT);
        adminAttempt = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, KAPUASYS, MetricsLabel.COUNT);
        adminConnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, KAPUASYS, CONNECTED, MetricsLabel.COUNT);
        adminDisconnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, KAPUASYS, DISCONNECTED, MetricsLabel.COUNT);
        internalConnectorAttempt = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, INTERNAL_CONNECTOR, MetricsLabel.COUNT);
        internalConnectorConnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, INTERNAL_CONNECTOR, CONNECT, MetricsLabel.COUNT);
        internalConnectorDisconnected = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, INTERNAL_CONNECTOR, DISCONNECT, MetricsLabel.COUNT);
        stealingLinkConnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, STEALING_LINK, CONNECT, MetricsLabel.COUNT);
        stealingLinkDisconnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, STEALING_LINK, DISCONNECT, MetricsLabel.COUNT);
        adminStealingLinkConnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADMIN_STEALING_LINK, CONNECT, MetricsLabel.COUNT);
        adminStealingLinkDisconnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, ADMIN_STEALING_LINK, DISCONNECT, MetricsLabel.COUNT);
        disconnectByEvent = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, DISCONNECT_BY_EVENT, DISCONNECT, MetricsLabel.COUNT);
        illegalStateDisconnect = metricsService.getCounter(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ILLEGAL_STATE, DISCONNECT, MetricsLabel.COUNT);
        // login time
        addConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, ADD_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        normalUserTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, USER, MetricsLabel.TIME, MetricsLabel.SECONDS);
        shiroLoginTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SHIRO, MetricsLabel.COMPONENT_LOGIN, MetricsLabel.TIME, MetricsLabel.SECONDS);
        checkAccessTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, CHECK_ACCESS, MetricsLabel.TIME, MetricsLabel.SECONDS);
        findDeviceConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, FIND_DEVICE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        updateDeviceConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, UPDATE_DEVICE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        shiroLogoutTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SHIRO, LOGOUT, MetricsLabel.TIME, MetricsLabel.SECONDS);
        sendLoginUpdateMsgTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, SEND_LOGIN_UPDATE, MetricsLabel.TIME, MetricsLabel.SECONDS);
        removeConnectionTime = metricsService.getTimer(MetricsLabel.MODULE_SECURITY, MetricsLabel.COMPONENT_LOGIN, REMOVE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
    }

    public Counter getSuccess() {
        return success;
    }

    public Counter getSuccessFromCache() {
        return successFromCache;
    }

    public Counter getFailure() {
        return failure;
    }

    public Counter getConnected() {
        return connected;
    }

    public Counter getDisconnected() {
        return disconnected;
    }

    public Counter getInvalidUserPassword() {
        return invalidUserPassword;
    }

    public Counter getInvalidClientId() {
        return invalidClientId;
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

    public Counter getInternalConnectorAttempt() {
        return internalConnectorAttempt;
    }

    public Counter getAttempt() {
        return attempt;
    }

    public Counter getStealingLinkConnect() {
        return stealingLinkConnect;
    }

    public Counter getStealingLinkDisconnect() {
        return stealingLinkDisconnect;
    }

    public Counter getDisconnectByEvent() {
        return disconnectByEvent;
    }

    public Counter getAdminStealingLinkConnect() {
        return adminStealingLinkConnect;
    }

    public Counter getAdminStealingLinkDisconnect() {
        return adminStealingLinkDisconnect;
    }

    public Counter getIllegalStateDisconnect() {
        return illegalStateDisconnect;
    }

    public Counter getInternalConnectorConnected() {
        return internalConnectorConnected;
    }

    public Counter getInternalConnectorDisconnected() {
        return internalConnectorDisconnected;
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

    public Timer getSendLoginUpdateMsgTime() {
        return sendLoginUpdateMsgTime;
    }

    public Timer getRemoveConnectionTime() {
        return removeConnectionTime;
    }

}
