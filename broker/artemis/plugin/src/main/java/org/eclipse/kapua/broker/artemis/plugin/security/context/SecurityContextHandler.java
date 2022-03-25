/*******************************************************************************
 * Copyright (c) 2021, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.broker.artemis.plugin.security.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.client.security.AuthErrorCodes;
import org.eclipse.kapua.client.security.KapuaIllegalDeviceStateException;
import org.eclipse.kapua.client.security.ServiceClient.SecurityAction;
import org.eclipse.kapua.client.security.bean.AuthAcl;
import org.eclipse.kapua.client.security.bean.AuthRequest;
import org.eclipse.kapua.client.security.context.SessionContext;
import org.eclipse.kapua.client.security.context.Utils;
import org.eclipse.kapua.commons.cache.LocalCache;
import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.localevent.ExecutorWrapper;
import org.eclipse.kapua.service.authentication.KapuaPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO move this under DI. Ask for a way to do so in Artemis (is still Artemis managed by Spring as ActiveMQ 5?)
 * So the singleton can be managed by the DI.
 *
 */
public final class SecurityContextHandler {

    protected static Logger logger = LoggerFactory.getLogger(SecurityContextHandler.class);

    private static final String REPORT_HEADER = "################################################################################################";
    private static final String REPORT_SEPARATOR = "------------------------------------------------------------------------------------------------";
    private enum ReportType {
        Full,
        Compact,
        DetailedServer
    }

    private static final SecurityContextHandler INSTANCE = new SecurityContextHandler();

    //it's a singleton, no needing to make this fields static
    private final int connectionTokenCacheSize = 1000;
    private final int connectionTokenCacheTTL = 60;
    private final ConnectionToken connectionTokenCacheDefValue = null;
    private final int sessionContextCacheSize = 1000;
    private final int sessionContextCacheTTL = 60;
    private final SessionContext sessionContextCacheDefValue = null;
    private final Acl aclCacheDefValue = null;

    //concurrency shouldn't be an issue since this set will contain the list of active connections
    private final Set<String> activeConnections = new HashSet<>();
    private final LocalCache<String, ConnectionToken> connectionTokenCache = new LocalCache<>(connectionTokenCacheSize, connectionTokenCacheTTL, connectionTokenCacheDefValue);
    private final LocalCache<String, SessionContext> sessionContextCache = new LocalCache<>(sessionContextCacheSize, sessionContextCacheTTL, sessionContextCacheDefValue);
    private final LocalCache<String, Acl> aclCache = new LocalCache<>(sessionContextCacheSize, sessionContextCacheTTL, aclCacheDefValue);

    //use string as key since some method returns DefaultChannelId as connection id, some other a string
    //the string returned by some method as connection id is the asShortText of DefaultChannelId
    private final Map<String, SessionContext> sessionContextMapByClient = new ConcurrentHashMap<>();

    //by connection id context
    private final Map<String, SessionContext> sessionContextMap = new ConcurrentHashMap<>();
    private final Map<String, Acl> aclMap = new ConcurrentHashMap<>();

    private ExecutorWrapper executorWrapper;

    private SecurityContextHandler() {
    }

    public static SecurityContextHandler getInstance() {
        return INSTANCE;
    }

    public void printReport(ActiveMQServer server, String caller, String connectionId) {
        printReport(ReportType.Full, server, caller, connectionId);
    }

    public void printCompactReport(ActiveMQServer server, String caller, String connectionId) {
        printReport(ReportType.Compact, server, caller, connectionId);
    }

    public void printDetailedServerReport(ActiveMQServer server, String caller, String connectionId) {
        printReport(ReportType.DetailedServer, server, caller, connectionId);
    }

    private void printReport(ReportType reportType, ActiveMQServer server, String caller, String connectionId) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n").append(REPORT_HEADER).append("\n");
        switch (reportType) {
        case Full:
            appendServerContextReport(builder, server);
            builder.append(REPORT_SEPARATOR).append("\n");
            appendSessionInfoReport(builder, server);
            builder.append(REPORT_SEPARATOR).append("\n");
            appendDetailedServerContextReport(builder, caller, connectionId);
            break;
        case Compact:
            appendServerContextReport(builder, server);
            break;
        case DetailedServer:
            appendDetailedServerContextReport(builder, caller, connectionId);
            break;
        default:
            break;
        }
        builder.append(REPORT_HEADER);
        logger.info("{}", builder);
    }

    public void init(ActiveMQServer server) {
       executorWrapper = new ExecutorWrapper("ServerReport", () -> printReport(server, "ServerReportTask", "N/A"), 60, 30, TimeUnit.SECONDS);
       executorWrapper.start();
    }

    public void shutdown(ActiveMQServer server) {
        if (executorWrapper!=null) {
            executorWrapper.stop();
        }
    }

    public ConnectionToken updateConnectionTokenOnConnection(String connectionId) {
        ConnectionToken connectionToken = connectionTokenCache.getAndRemove(connectionId);
        if (connectionToken==null) {
            connectionTokenCache.put(connectionId,
                new ConnectionToken(SecurityAction.brokerConnect, KapuaDateUtils.getKapuaSysDate()));
        }
        else {
            //the disconnect callback is called after the connect so nothing to add to the context
            //TODO add metric?
            logger.warn("Connect callback called after the disconnection callback ({} - {} - {})", connectionId, connectionToken.getAction(), connectionToken.getActionDate());
        }
        return connectionToken;
    }

    public boolean setSessionContext(SessionContext sessionContext, List<AuthAcl> authAcls) throws KapuaIllegalArgumentException {
        logger.info("Updating session context for connection id: {}", sessionContext.getConnectionId());
        synchronized (sessionContext.getConnectionId().intern()) {
            String connectionId = sessionContext.getConnectionId();
            if (updateConnectionTokenOnConnection(connectionId)==null) {
                logger.info("Setting session context for connection id: {}", connectionId);
                activeConnections.add(connectionId);
                //fill by connection id context
                sessionContextMap.put(connectionId, sessionContext);
                aclMap.put(connectionId, new Acl(sessionContext.getPrincipal(), authAcls));
                //fill by full client id context
                sessionContextMapByClient.put(Utils.getFullClientId(sessionContext), sessionContext);
                return true;
            }
            else {
                return false;
            }
        }
    }

    public void updateConnectionTokenOnDisconnection(String connectionId) {
        if (connectionTokenCache.getAndRemove(connectionId)==null) {
            //put the connection token
            connectionTokenCache.put(connectionId,
                new ConnectionToken(SecurityAction.brokerDisconnect, KapuaDateUtils.getKapuaSysDate()));
            logger.warn("Disconnect callback called before the connection callback for connection id: {}", connectionId);
        }
    }

    public SessionContext cleanSessionContext(SessionContext sessionContext) {
        logger.info("Updating session context for connection id: {}", sessionContext.getConnectionId());
        synchronized (sessionContext.getConnectionId().intern()) {
            String connectionId = sessionContext.getConnectionId();
            logger.info("Cleaning session context for connection id: {}", connectionId);
            //cleaning context and filling cache
            SessionContext sessionContextOld = sessionContextMap.remove(connectionId);
            if (sessionContextOld!=null) {
                sessionContextCache.put(connectionId, sessionContextOld);
            }
            Acl aclOld = aclMap.remove(connectionId);
            if (aclOld!=null) {
                aclCache.put(connectionId, aclOld);
            }
            activeConnections.remove(connectionId);

            String fullClientId = Utils.getFullClientId(sessionContext);
            SessionContext currentSessionContext = sessionContextMapByClient.get(fullClientId);
            //if no stealing link remove the context by client id
            //on a stealing link currentSessionContext could be null if the disconnect of the latest connected client happens before the others
            if (currentSessionContext==null) {
                logger.warn("Cannot find session context by full client id: {}", fullClientId);
                //TODO add metric?
            }
            else {
                if (connectionId.equals(currentSessionContext.getConnectionId())) {
                    //redundant assignment
                    currentSessionContext = sessionContextMapByClient.remove(fullClientId);
                    logger.info("Disconnect: NO stealing - remove session context by clientId: {} - connection id: {}", currentSessionContext.getClientId(), currentSessionContext.getConnectionId());
                }
                else {
                    logger.info("Disconnect: stealing - leave session context by clientId: {} - connection id: {}", currentSessionContext.getClientId(), currentSessionContext.getConnectionId());
                }
            }
            return currentSessionContext;
        }
    }

    public SessionContext getSessionContextByClientId(String fullClientId) {
        return sessionContextMapByClient.get(fullClientId);
    }

    public SessionContext getSessionContextWithCacheFallback(String connectionId) {
        SessionContext sessionContext = sessionContextMap.get(connectionId);
        if (sessionContext == null) {
            //try from cache
            //TODO add metric?
            sessionContext = sessionContextCache.get(connectionId);
            if (sessionContext!=null) {
                logger.warn("Got sessioncontext for connectionId {} from cache!", connectionId);
            }
        }
        return sessionContext;
    }

    public SessionContext getSessionContext(String connectionId) {
        return sessionContextMap.get(connectionId);
    }

    public boolean checkPublisherAllowed(SessionContext sessionContext, String address) {
//        KapuaPrincipal principal = principalMap.get(sessionContext.getConnectionId());
//            throw new SecurityException("User " + principal.getName() + " not allowed to publish to " + address);
        Acl acl = getAcl(sessionContext.getConnectionId());
        return acl!=null && acl.canWrite(sessionContext.getPrincipal(), address);
    }

    public boolean checkConsumerAllowed(SessionContext sessionContext, String address) {
        Acl acl = getAcl(sessionContext.getConnectionId());
        return acl!=null && acl.canRead(sessionContext.getPrincipal(), address);
    }

    public boolean checkAdminAllowed(SessionContext sessionContext, String address) {
        Acl acl = getAcl(sessionContext.getConnectionId());
        return acl!=null && acl.canManage(sessionContext.getPrincipal(), address);
    }

     private Acl getAcl(String connectionId) {
        Acl acl = aclMap.get(connectionId);
        if (acl==null) {
            //try from cache
            //TODO add metric?
            acl = aclCache.get(connectionId);
            if (acl!=null) {
                logger.warn("Got acl for connectionId {} from cache!", connectionId);
            }
        }
        return acl;
    }

    public Subject buildFromPrincipal(KapuaPrincipal kapuaPrincipal) {
        Subject subject = new Subject();
        subject.getPrincipals().add(kapuaPrincipal);
        return subject;
    }

    public void updateStealingLinkAndIllegalState(AuthRequest authRequest, String connectionId, String oldConnectionId) {
        authRequest.setStealingLink(isStealingLink(connectionId, oldConnectionId));
        authRequest.setIllegalState(isIllegalState(authRequest));
    }

    private boolean isStealingLink(String connectionId, String oldConnectionId) {
        return oldConnectionId!=null && connectionId!=null ?
            !connectionId.equals(oldConnectionId) : oldConnectionId!=null;
    }

    private boolean isIllegalState(AuthRequest authRequest) {
        //TODO make this check based on instanceof
        //something like Class.forName(exceptionClass).. just are we sure we have the exceptionClass implementation available at runtime?
        return KapuaIllegalDeviceStateException.class.getName().equals(authRequest.getExceptionClass()) && AuthErrorCodes.DUPLICATE_CLIENT_ID.equals(authRequest.getAuthErrorCode());
    }


    private void appendServerContextReport(StringBuilder builder, ActiveMQServer server) {
        builder.append("## Session count: ").append(server.getSessions().size()).
            append(" - Connection count: ").append(server.getConnectionCount()).
            append(" - Broker connections: ").append(server.getBrokerConnections().size()).append("\n");
        builder.append("## session context: ").append(sessionContextMap.size()).append("\n");
        builder.append("## session context by client: ").append(sessionContextMapByClient.size()).append("\n");
        builder.append("## acl: ").append(aclMap.size()).append("\n");
        builder.append("## connection: ").append(activeConnections.size()).append("\n");
    }

    private void appendSessionInfoReport(StringBuilder builder, ActiveMQServer server) {
        builder.append("## Sessions:").append("\n");
        Map<Object, Integer> sessionById = new HashMap<>();
        server.getSessions().forEach(session -> {
            Integer tmp = sessionById.get(session.getConnectionID());
            if (tmp == null) {
                sessionById.put(session.getConnectionID(), new Integer(1));
            }
            else {
                sessionById.put(session.getConnectionID(), new Integer(tmp.intValue() + 1));
            }
        });
        sessionById.forEach((id, count) -> builder.append("##\tid:count ").append(id).append(":").append(count.intValue()).append("\n"));
    }

    private void appendDetailedServerContextReport(StringBuilder builder, String caller, String connectionId) {
        builder.append("## Security context: (caller: ").append(caller).append(" - connectionId: ").append(connectionId).append(")\n");
        builder.append("## connection info by client id\n");
        sessionContextMapByClient.forEach((key, sessionContext) -> builder.append("##\tclientId: ").append(key).append(" - ip: ").append(sessionContext.getClientIp()).append(" - conId: ").append(sessionContext.getConnectionId()).append("\tinternal: ").append(sessionContext.isInternal()).append("\n"));
        builder.append("## connection info by connection id\n");
        sessionContextMap.forEach((key, sessionContext) -> builder.append("##\tconId: ").append(key).append(" - clientId: ").append(sessionContext.getClientId()).append(" - ip: ").append(sessionContext.getClientIp()).append("\tinternal: ").append(sessionContext.isInternal()).append("\n"));
        builder.append("## acl by connection id\n");
        aclMap.forEach((key, acl) -> builder.append("##\tconnId: ").append(key).append("\n"));
    }
}
