/*******************************************************************************
 * Copyright (c) 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.qa.integration.steps;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.ListNetworksFilterParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import com.spotify.docker.client.messages.PortBinding;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.activemq.command.BrokerInfo;
import org.eclipse.kapua.qa.common.StepData;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DockerSteps {

    private static final Logger logger = LoggerFactory.getLogger(DockerSteps.class);

    private static final String NETWORK_PREFIX = "kapua-net";
    private static final String KAPUA_VERSION = "1.3.0-EXT-CONN-SNAPSHOT";
    private static final String ES_IMAGE = "elasticsearch:5.4.0";
    private static final List<String> DEFAULT_DEPLOYMENT_CONTAINERS_NAME;
    private static final int WAIT_COUNT = 120;//total wait time = 240 secs (120 * 2000ms)
    private static final long WAIT_STEP = 2000;
    private static final long WAIT_FOR_DB = 10000;
    private static final long WAIT_FOR_ES = 10000;
    private static final long WAIT_FOR_EVENTS_BROKER = 10000;
    private static final long WAIT_FOR_BROKER = 60000;
    private static final int HTTP_COMMUNICATION_TIMEOUT = 3000;

    private static final int LIFECYCLE_HEALTH_CHECK_PORT = 8090;
    private static final int TElEMETRY_HEALTH_CHECK_PORT = 8091;

    private static final String LIFECYCLE_CHECK_WEB_APP = "lifecycle";
    private static final String TELEMETRY_CHECK_WEB_APP = "telemetry";

    private static final String LIFECYCLE_HEALTH_URL = String.format("http://localhost:%d/%s/health", LIFECYCLE_HEALTH_CHECK_PORT, LIFECYCLE_CHECK_WEB_APP);
    private static final String TELEMETRY_HEALTH_URL = String.format("http://localhost:%d/%s/health", TElEMETRY_HEALTH_CHECK_PORT, TELEMETRY_CHECK_WEB_APP);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME = new ArrayList<>();
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME.add("telemetry-consumer");
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME.add("lifecycle-consumer");
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME.add("message-broker");
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME.add("events-broker");
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME.add("es");
        DEFAULT_DEPLOYMENT_CONTAINERS_NAME.add("db");
    }

    private NetworkConfig networkConfig;
    private String networkId;
    private boolean debug;
    private List<String> envVar;
    private Map<String, String> containerMap;
    public Map<String, Integer> portMap;
    public Map<String, BrokerInfo> brokerMap;

    private StepData stepData;

    @Inject
    public DockerSteps(StepData stepData) {
        this.stepData = stepData;
        containerMap = new HashMap<>();
    }

    @Given("^Enable debug$")
    public void enableDebug() {
        this.debug = true;
    }

    @Given("^Disable debug$")
    public void disableDebug() {
        this.debug = false;
    }

    @Given("^Create mqtt \"(.*)\" client for broker \"(.*)\" on port (\\d+) with user \"(.*)\" and pass \"(.*)\"$")
    public void createMqttClient(String clientId, String broker, int port, String user, String pass) {
        try {
            BrokerClient client = new BrokerClient(broker, port, clientId, user, pass);
            stepData.put(clientId, client);
        } catch (MqttException e) {
            logger.error("Error creating mqtt client with id " + clientId, e);
        }
    }

    @Given("^Connect to mqtt client \"(.*)\"$")
    public void connectMqttClient(String clientId) {
        BrokerClient client = (BrokerClient) stepData.get(clientId);
        try {
            client.connect();
        } catch (MqttException e) {
            logger.error("Unable to connect to mqtt broker with client " + clientId, e);
            e.printStackTrace();
        }
    }

    @Given("^Disconnect mqtt client \"(.*)\"$")
    public void disconnectMqttClient(String clientId) {
        BrokerClient client = (BrokerClient) stepData.get(clientId);
        try {
            client.disconnect();
        } catch (MqttException e) {
            logger.error("Unable to disconnect from mqtt broker with client " + clientId, e);
        }
    }

    @Given("^Subscribe mqtt client \"(.*)\" to topic \"(.*)\"$")
    public void subscribeMqttClient(String clientId, String topic) {
        BrokerClient client = (BrokerClient) stepData.get(clientId);
        try {
            client.subscribe(topic, 1);
        } catch (MqttException e) {
            logger.error("Can not subscribe with client " + clientId);
        }
    }

    @Then("^Client \"(.*)\" has (\\d+) messages?.*$")
    public void clientCountMsg(String clientId, int numMsgs) {
        BrokerClient client = (BrokerClient) stepData.get(clientId);
        int receivedMsgs = client.getRecivedMsgCnt();
        Assert.assertEquals(numMsgs, receivedMsgs);
    }

    @Given("^Publish string \"(.*)\" to topic \"(.*)\" as client \"(.*)\"")
    public void publishMqttClient(String message, String topic, String clientId) {
        BrokerClient client = (BrokerClient) stepData.get(clientId);
        try {
            client.publish(topic, 1, message);
        } catch (MqttException e) {
            logger.error("Can not publish to topic " + topic);
        }
    }

    @Given("^Start full docker environment$")
    public void startFullDockerEnvironment() throws Exception {
        logger.info("Starting full docker environment...");
        try {
            pullImage(ES_IMAGE);
            stopFullDockerEnvironment();
            removeNetwork();
            createNetwork();

            startDBContainer("db");
            synchronized (this) {
                this.wait(WAIT_FOR_DB);
            }

            startESContainer("es");
            synchronized (this) {
                this.wait(WAIT_FOR_ES);
            }

            startEventBrokerContainer("events-broker");
            synchronized (this) {
                this.wait(WAIT_FOR_EVENTS_BROKER);
            }

            startMessageBrokerContainer("message-broker");
            synchronized (this) {
                this.wait(WAIT_FOR_BROKER);
            }

            startLifecycleConsumerContainer("lifecycle-consumer");
            startTelemetryConsumerContainer("telemetry-consumer");
            logger.info("Starting full docker environment... DONE (waiting for containers to be ready)");
            //wait until consumers are ready
            int loops = 0;
            while (!areConsumersReady()) {
                if (loops++ > WAIT_COUNT) {
                    throw new DockerException("Timeout waiting for cluster startup reached!");
                }
                synchronized (this) {
                    this.wait(WAIT_STEP);
                }
                logger.info("Consumers not ready after {}s... wait", (loops * WAIT_STEP / 1000));
            }
            logger.info("Consumers ready");
        }
        catch (Exception e) {
            logger.error("Error while starting full docker environment: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean areConsumersReady() throws JsonParseException, JsonMappingException, IOException {
        if (isConsumerReady(LIFECYCLE_CHECK_WEB_APP)) {
            return isConsumerReady(TELEMETRY_CHECK_WEB_APP);
        }
        return false;
    }

    private boolean isConsumerReady(String type) throws JsonParseException, JsonMappingException, IOException {
        URL consumerUrl = new URL(LIFECYCLE_HEALTH_URL);//lifecycle endpoint
        if (TELEMETRY_CHECK_WEB_APP.equals(type)) {
            consumerUrl = new URL(TELEMETRY_HEALTH_URL);
        }
        logger.debug("Querying {} consumer status for url: {}", type, consumerUrl);
        HttpURLConnection conn = null;
        DataOutputStream out = null;
        BufferedReader in = null;
        InputStreamReader isr = null;
        try {
            conn = (HttpURLConnection) consumerUrl.openConnection();
            conn.setConnectTimeout(HTTP_COMMUNICATION_TIMEOUT);
            conn.setReadTimeout(HTTP_COMMUNICATION_TIMEOUT);
            //works with spring boot actuator servlet mappings
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            int status = conn.getResponseCode();
            if (status == 200) {
                isr = new InputStreamReader(conn.getInputStream());
                in = new BufferedReader(isr);
                return isRunning(MAPPER.readValue(in, Map.class));
            } else {
                logger.info("Querying {} consumer status for url: {} - ERROR", type, consumerUrl);
                return false;
            }
        } catch (IOException e) {
            //nothing to do
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (Exception e) {
                    logger.warn("Cannot close InputStreamReader", e.getMessage(), e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.warn("Cannot close BufferedReader", e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.warn("Cannot close DataOutputStream", e.getMessage(), e);
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    logger.warn("Cannot close HttpURLConnection", e.getMessage(), e);
                }
            }
        }
        return false;
    }

    private boolean isRunning(Map<String, Object> map) {
        if (map.get("status") != null && "UP".equals(map.get("status"))) {
            return true;
        }
        return false;
    }

    @Given("^Stop full docker environment$")
    public void stopFullDockerEnvironment() throws DockerException, InterruptedException {
        removeContainer(DEFAULT_DEPLOYMENT_CONTAINERS_NAME);
    }

    @Given("^Create network$")
    public void createNetwork() throws DockerException, InterruptedException {
        networkConfig = NetworkConfig.builder().name(NETWORK_PREFIX).build();
        NetworkCreation networkCreation = DockerUtil.getDockerClient().createNetwork(networkConfig);
        networkId = networkCreation.id();
    }

    @Given("^Remove network$")
    public void removeNetwork() throws DockerException, InterruptedException {
        List<Network> networkList = DockerUtil.getDockerClient().listNetworks(ListNetworksFilterParam.byNetworkName(NETWORK_PREFIX));
        if (networkList != null) {
            for (Network network : networkList) {
                String networkId = network.id();
                String networkName = network.name();
                logger.info("Removing network id {} - name {}...", networkId, networkName);
                try {
                    DockerUtil.getDockerClient().removeNetwork(networkId);
                } catch (NetworkNotFoundException e) {
                    //no error!
                    logger.warn("Cannot remove network id {}... network not found!", networkId);
                }
                logger.info("Removing network id {} - name {}... DONE", networkId, networkName);
            }
        }
    }

    @Given("^Pull image \"(.*)\"$")
    public void pullImage(String image) throws DockerException, InterruptedException {
        DockerUtil.getDockerClient().pull(image);
    }

    @Given("^List images by name \"(.*)\"$")
    public void listImages(String imageName) throws Exception {
        List<Image> images = DockerUtil.getDockerClient().listImages(DockerClient.ListImagesParam.byName(imageName));
        if ((images != null) && (images.size() > 0)) {
            for (Image image : images) {
                logger.info("Image: " + image);
            }
        } else {
            logger.info("No docker images found.");
        }
    }

    @And("^Start DB container with name \"(.*)\"$")
    public void startDBContainer(String name) throws DockerException, InterruptedException {
        logger.info("Starting DB container...");
        ContainerConfig dbConfig = getDbContainerConfig();
        ContainerCreation dbContainerCreation = DockerUtil.getDockerClient().createContainer(dbConfig, name);
        String containerId = dbContainerCreation.id();

        DockerUtil.getDockerClient().startContainer(containerId);
        DockerUtil.getDockerClient().connectToNetwork(containerId, networkId);
        containerMap.put("db", containerId);
        logger.info("DB container started: {}", containerId);
    }

    @And("^Start ES container with name \"(.*)\"$")
    public void startESContainer(String name) throws DockerException, InterruptedException {
        logger.info("Starting ES container...");
        ContainerConfig esConfig = getEsContainerConfig();
        ContainerCreation esContainerCreation = DockerUtil.getDockerClient().createContainer(esConfig, name);
        String containerId = esContainerCreation.id();

        DockerUtil.getDockerClient().startContainer(containerId);
        DockerUtil.getDockerClient().connectToNetwork(containerId, networkId);
        containerMap.put("es", containerId);
        logger.info("ES container started: {}", containerId);
    }

    @And("^Start EventBroker container with name \"(.*)\"$")
    public void startEventBrokerContainer(String name) throws DockerException, InterruptedException {
        logger.info("Starting EventBroker container...");
        ContainerConfig ebConfig = getEventBrokerContainerConfig();
        ContainerCreation ebContainerCreation = DockerUtil.getDockerClient().createContainer(ebConfig, name);
        String containerId = ebContainerCreation.id();

        DockerUtil.getDockerClient().startContainer(containerId);
        DockerUtil.getDockerClient().connectToNetwork(containerId, networkId);
        containerMap.put(name, containerId);
        logger.info("EventBroker container started: {}", containerId);
    }

    @And("^Start MessageBroker container with name \"(.*)\"$")
    public void startMessageBrokerContainer(String name) throws DockerException, InterruptedException {
//        BrokerConfigData bcData = brokerConfigDataList.get(0);
        logger.info("Starting Message Broker container {}...", name);
        ContainerConfig mbConfig = getBrokerContainerConfig("message-broker", 1883, 1883, 1893, 1893, 8883, 8883, 8161, 8161, 5005, 5005, "kapua/kapua-broker:" + KAPUA_VERSION);
        ContainerCreation mbContainerCreation = DockerUtil.getDockerClient().createContainer(mbConfig, name);
        String containerId = mbContainerCreation.id();

        DockerUtil.getDockerClient().startContainer(containerId);
        DockerUtil.getDockerClient().connectToNetwork(containerId, networkId);
        containerMap.put(name, containerId);
        logger.info("Message Broker {} container started: {}", name, containerId);
    }

    @And("^Start TelemetryConsumer container with name \"(.*)\"$")
    public void startTelemetryConsumerContainer(String name) throws DockerException, InterruptedException {
        logger.info("Starting Telemetry Consumer container {}...", name);
        ContainerCreation mbContainerCreation = DockerUtil.getDockerClient().createContainer(getTelemetryConsumerConfig(8080, 8091, 8001, 8002), name);
        String containerId = mbContainerCreation.id();

        DockerUtil.getDockerClient().startContainer(containerId);
        DockerUtil.getDockerClient().connectToNetwork(containerId, networkId);
        containerMap.put(name, containerId);
        logger.info("Telemetry Consumer {} container started: {}", name, containerId);
    }

    @And("^Start LifecycleConsumer container with name \"(.*)\"$")
    public void startLifecycleConsumerContainer(String name) throws DockerException, InterruptedException {
        logger.info("Starting Lifecycle Consumer container {}...", name);
        ContainerCreation mbContainerCreation = DockerUtil.getDockerClient().createContainer(getLifecycleConsumerConfig(8080, 8090, 8001, 8001), name);
        String containerId = mbContainerCreation.id();

        DockerUtil.getDockerClient().startContainer(containerId);
        DockerUtil.getDockerClient().connectToNetwork(containerId, networkId);
        containerMap.put(name, containerId);
        logger.info("Lifecycle Consumer {} container started: {}", name, containerId);
    }

    @Then("^Stop container with name \"(.*)\"$")
    public void stopContainer(List<String> names) throws DockerException, InterruptedException {
        for (String name : names) {
            logger.info("Stopping container {}...", name);
            String containerId = containerMap.get(name);
            DockerUtil.getDockerClient().stopContainer(containerId, 3);
            logger.info("Container {} stopped.", name);
        }
    }

    @Then("^Remove container with name \"(.*)\"$")
    public void removeContainer(List<String> names) throws DockerException, InterruptedException {
        for (String name : names) {
            logger.info("Removing container {}...", name);
            List<Container> containers = DockerUtil.getDockerClient().listContainers(ListContainersParam.filter("name", name));
            if (containers.isEmpty()) {
                logger.info("No docker images found. Cannot remove container {}. (Container not found!)", name);
            } else {
                containers.forEach(container -> {
                    try {
                        DockerUtil.getDockerClient().removeContainer(container.id(), new RemoveContainerParam("force", "true"));
                    } catch (DockerException | InterruptedException e) {
                        //test fails since the environment is no cleaned up
                        Assert.fail("Cannot remove container!");
                    }
                    containerMap.remove(name);
                    logger.info("Container {} removed. (Container id: {})", name, container.id());
                });
            }
        }
    }

    /**
     * Creation of docker container configuration for broker.
     *
     * @param brokerAddr
     * @param brokerIp
     * @param clusterName
     * @param controlMessageForwarding
     * @param mqttPort                 mqtt port on docker
     * @param mqttHostPort             mqtt port on docker host
     * @param mqttsPort                mqtts port on docker
     * @param mqttsHostPort            mqtts port on docker host
     * @param webPort                  web port on docker
     * @param webHostPort              web port on docker host
     * @param debugPort                debug port on docker
     * @param debugHostPort            debug port on docker host
     * @param brokerInternalDebugPort
     * @param dockerImage              full name of image (e.g. "kapua/kapua-broker:" + version)
     * @return Container configuration for specific boroker instance
     */
    private ContainerConfig getBrokerContainerConfig(String brokerIp,
                                                     int mqttPort, int mqttHostPort,
                                                     int mqttInternalPort, int mqttInternalHostPort,
                                                     int mqttsPort, int mqttsHostPort,
                                                     int webPort, int webHostPort,
                                                     int debugPort, int debugHostPort,
                                                     String dockerImage) {

        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        addHostPort("0.0.0.0", portBindings, mqttPort, mqttHostPort);
        addHostPort("0.0.0.0", portBindings, mqttInternalPort, mqttInternalHostPort);
        addHostPort("0.0.0.0", portBindings, mqttsPort, mqttsHostPort);
        addHostPort("0.0.0.0", portBindings, webPort, webHostPort);
        addHostPort("0.0.0.0", portBindings, debugPort, debugHostPort);

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        List<String> envVars = Lists.newArrayList("commons.db.schema.update=true",
                "commons.db.connection.host=db",
                "commons.db.connection.port=3306",
                "datastore.elasticsearch.nodes=es",
                "datastore.elasticsearch.port=9200",
                "datastore.client.class=org.eclipse.kapua.service.datastore.client.rest.RestDatastoreClient",
                "commons.eventbus.url=failover:(amqp://events-broker:5672)?jms.sendTimeout=1000",
                "certificate.jwt.private.key=file:///var/opt/activemq/key.pk8",
                "certificate.jwt.certificate=file:///var/opt/activemq/cert.pem",
                String.format("broker.ip=%s", brokerIp));
        if (envVar != null) {
            envVars.addAll(envVar);
        }

        if (debug) {
            envVars.add(String.format("ACTIVEMQ_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%s", debugPort));
        }

        String[] ports = {
                String.valueOf(mqttPort),
                String.valueOf(mqttInternalPort),
                String.valueOf(mqttsPort),
                String.valueOf(webPort),
                String.valueOf(debugPort)
        };

        return ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(ports)
                .env(envVars)
                .image(dockerImage)
                .build();
    }

    /**
     * Creation of docker container configuration for H2 database.
     *
     * @return Container configuration for database instance.
     */
    private ContainerConfig getDbContainerConfig() {
        final int dbPort = 3306;
        final int dbPortConsole = 8181;
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        addHostPort("0.0.0.0", portBindings, dbPort, dbPort);
        addHostPort("0.0.0.0", portBindings, dbPortConsole, dbPortConsole);
        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        String[] ports = {
                String.valueOf(dbPort),
                String.valueOf(dbPortConsole)
        };

        return ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(ports)
                .env(
                        "DATABASE=kapuadb",
                        "DB_USER=kapua",
                        "DB_PASSWORD=kapua",
                        "DB_PORT_3306_TCP_PORT=3306"
                )
                .image("kapua/kapua-sql:" + KAPUA_VERSION)
                .build();
    }

    /**
     * Creation of docker container configuration for telemetry consumer.
     *
     * @return Container configuration for telemetry consumer.
     */
    private ContainerConfig getTelemetryConsumerConfig(int healthPort, int healthHostPort, int debugPort, int debugHostPort) {
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        addHostPort("0.0.0.0", portBindings, healthPort, healthHostPort);
        addHostPort("0.0.0.0", portBindings, debugPort, debugHostPort);
        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        String[] ports = {
                String.valueOf(healthPort),
                String.valueOf(debugPort)
        };

        return ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(ports)
                .env(
                        "commons.db.schema.update=true",
                        "BROKER_HOST=message-broker"
                )
                .image("kapua/kapua-consumer-telemetry:" + KAPUA_VERSION)
                .build();
    }

    /**
     * Creation of docker container configuration for lifecycle consumer.
     *
     * @return Container configuration for lifecycle consumer.
     */
    private ContainerConfig getLifecycleConsumerConfig(int healthPort, int healthHostPort, int debugPort, int debugHostPort) {
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        addHostPort("0.0.0.0", portBindings, healthPort, healthHostPort);
        addHostPort("0.0.0.0", portBindings, debugPort, debugHostPort);
        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        String[] ports = {
                String.valueOf(healthPort),
                String.valueOf(debugPort)
        };

        return ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(ports)
                .env(
                        "commons.db.schema.update=true",
                        "BROKER_HOST=message-broker"
                )
                .image("kapua/kapua-consumer-lifecycle:" + KAPUA_VERSION)
                .build();
    }

    /**
     * Creation of docker container configuration for Elasticsearch.
     *
     * @return Container configuration for Elasticsearch instance.
     */
    private ContainerConfig getEsContainerConfig() {
        final int esPortRest = 9200;
        final int esPortNodes = 9300;
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        addHostPort("0.0.0.0", portBindings, esPortRest, esPortRest);
        addHostPort("0.0.0.0", portBindings, esPortNodes, esPortNodes);
        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        return ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(String.valueOf(esPortRest), String.valueOf(esPortNodes))
                .image("elasticsearch:5.4.0")
                .cmd(
                        "-Ecluster.name=kapua-datastore",
                        "-Ediscovery.type=single-node",
                        "-Etransport.host=0.0.0.0 ",
                        "-Etransport.ping_schedule=-1 ",
                        "-Etransport.tcp.connect_timeout=30s"
                )
                .build();
    }

    /**
     * Creation of docker container configuration for event broker.
     *
     * @return Container configuration for event broker instance.
     */
    private ContainerConfig getEventBrokerContainerConfig() {
        final int brokerPort = 5672;
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        addHostPort("0.0.0.0", portBindings, brokerPort, brokerPort);
        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        return ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(String.valueOf(brokerPort))
                .image("kapua/kapua-events-broker:" + KAPUA_VERSION)
                .build();
    }

    /**
     * Add docker port to host port mapping.
     *
     * @param host         ip address of host
     * @param portBindings list ob bindings that gets updated
     * @param port         docker port
     * @param hostPort     port on host
     */
    private void addHostPort(String host, Map<String, List<PortBinding>> portBindings,
                             int port, int hostPort) {

        List<PortBinding> hostPorts = new ArrayList<>();
        hostPorts.add(PortBinding.of(host, hostPort));
        portBindings.put(String.valueOf(port), hostPorts);
    }

}
