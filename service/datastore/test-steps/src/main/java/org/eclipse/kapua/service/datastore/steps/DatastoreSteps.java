/*******************************************************************************
 * Copyright (c) 2017, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.KapuaMessageFactory;
import org.eclipse.kapua.message.KapuaPayload;
import org.eclipse.kapua.message.KapuaPosition;
import org.eclipse.kapua.message.device.data.KapuaDataChannel;
import org.eclipse.kapua.message.device.data.KapuaDataMessage;
import org.eclipse.kapua.message.device.data.KapuaDataMessageFactory;
import org.eclipse.kapua.message.device.data.KapuaDataPayload;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.common.Session;
import org.eclipse.kapua.qa.common.SimulatedDevice;
import org.eclipse.kapua.qa.common.SimulatedDeviceApplication;
import org.eclipse.kapua.qa.common.StepData;
import org.eclipse.kapua.qa.common.TestBase;
import org.eclipse.kapua.qa.common.With;
import org.eclipse.kapua.qa.common.cucumber.CucMessageRange;
import org.eclipse.kapua.qa.common.cucumber.CucMetric;
import org.eclipse.kapua.qa.common.cucumber.CucTopic;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.datastore.ChannelInfoRegistryService;
import org.eclipse.kapua.service.datastore.ClientInfoRegistryService;
import org.eclipse.kapua.service.datastore.DatastoreObjectFactory;
import org.eclipse.kapua.service.datastore.MessageStoreService;
import org.eclipse.kapua.service.datastore.MetricInfoRegistryService;
import org.eclipse.kapua.service.datastore.client.ClientException;
import org.eclipse.kapua.service.datastore.client.ClientUnavailableException;
import org.eclipse.kapua.service.datastore.client.DatastoreClient;
import org.eclipse.kapua.service.datastore.client.model.IndexRequest;
import org.eclipse.kapua.service.datastore.internal.ChannelInfoRegistryServiceProxy;
import org.eclipse.kapua.service.datastore.internal.ClientInfoRegistryServiceProxy;
import org.eclipse.kapua.service.datastore.internal.DatastoreCacheManager;
import org.eclipse.kapua.service.datastore.internal.MetricInfoRegistryServiceProxy;
import org.eclipse.kapua.service.datastore.internal.client.DatastoreClientFactory;
import org.eclipse.kapua.service.datastore.internal.mediator.ChannelInfoField;
import org.eclipse.kapua.service.datastore.internal.mediator.ClientInfoField;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreChannel;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreMediator;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreUtils;
import org.eclipse.kapua.service.datastore.internal.mediator.MessageField;
import org.eclipse.kapua.service.datastore.internal.mediator.MessageStoreConfiguration;
import org.eclipse.kapua.service.datastore.internal.mediator.MetricInfoField;
import org.eclipse.kapua.service.datastore.internal.model.DataIndexBy;
import org.eclipse.kapua.service.datastore.internal.model.MetricsIndexBy;
import org.eclipse.kapua.service.datastore.internal.model.query.MessageQueryImpl;
import org.eclipse.kapua.service.datastore.internal.schema.ChannelInfoSchema;
import org.eclipse.kapua.service.datastore.internal.schema.ClientInfoSchema;
import org.eclipse.kapua.service.datastore.internal.schema.MessageSchema;
import org.eclipse.kapua.service.datastore.internal.schema.MetricInfoSchema;
import org.eclipse.kapua.service.datastore.internal.setting.DatastoreSettingKey;
import org.eclipse.kapua.service.datastore.model.ChannelInfo;
import org.eclipse.kapua.service.datastore.model.ChannelInfoListResult;
import org.eclipse.kapua.service.datastore.model.ClientInfo;
import org.eclipse.kapua.service.datastore.model.ClientInfoListResult;
import org.eclipse.kapua.service.datastore.model.DatastoreMessage;
import org.eclipse.kapua.service.datastore.model.MessageListResult;
import org.eclipse.kapua.service.datastore.model.MetricInfo;
import org.eclipse.kapua.service.datastore.model.MetricInfoListResult;
import org.eclipse.kapua.service.datastore.model.StorableId;
import org.eclipse.kapua.service.datastore.model.StorableIdFactory;
import org.eclipse.kapua.service.datastore.model.StorableListResult;
import org.eclipse.kapua.service.datastore.model.query.ClientInfoQuery;
import org.eclipse.kapua.service.datastore.model.query.SortDirection;
import org.eclipse.kapua.service.datastore.model.query.ChannelMatchPredicate;
import org.eclipse.kapua.service.datastore.model.query.AndPredicate;
import org.eclipse.kapua.service.datastore.model.query.TermPredicate;
import org.eclipse.kapua.service.datastore.model.query.StorablePredicateFactory;
import org.eclipse.kapua.service.datastore.model.query.MessageQuery;
import org.eclipse.kapua.service.datastore.model.query.ChannelInfoQuery;
import org.eclipse.kapua.service.datastore.model.query.MetricInfoQuery;
import org.eclipse.kapua.service.datastore.model.query.SortField;
import org.eclipse.kapua.service.datastore.model.query.StorableFetchStyle;
import org.eclipse.kapua.service.datastore.model.query.RangePredicate;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceCreator;
import org.eclipse.kapua.service.device.registry.DeviceFactory;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class DatastoreSteps extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreSteps.class);

    @Then("^Number of received data messages is different than (\\d+)$")
    public void numberOfReceivedDataMessagesIsDifferentThan(int numberOfMessages) {
        long count = (long) stepData.get("messageCountResult");
        assertNotEquals(numberOfMessages, count);
    }

    @And("^I set the following metrics with messages from the list \"([^\"]*)\"$")
    public void iSetTheFollowingMetricsFromTheList(String lstKey, List<CucMetric> metLst) throws Exception {
        List<KapuaDataMessage> tmpMsgLst = (List<KapuaDataMessage>) stepData.get(lstKey);

        for (CucMetric tmpMet : metLst) {
            KapuaDataMessage tmpMsg = tmpMsgLst.get(tmpMet.getMessage());
            tmpMsg.setPayload(dataMessageFactory.newKapuaDataPayload());

            switch (tmpMet.getType().trim().toLowerCase()) {
                case "string": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), tmpMet.getValue());
                    break;
                }
                case "int": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), Integer.valueOf(tmpMet.getValue()));
                    break;
                }
                case "double": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), Double.valueOf(tmpMet.getValue()));
                    break;
                }
                case "bool": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), Boolean.valueOf(tmpMet.getValue().trim().toUpperCase()));
                    break;
                }
                default: {
                    fail(String.format("Unknown metric type [%s]", tmpMet.getType()));
                    break;
                }
            }
        }
    }

    @When("^I create message query for following metrics$")
    public void iCreateMessageQueryForMetrics(List<String> metricList) throws Exception {
        Account account = (Account) stepData.get("LastAccount");
        List<MessageQuery> listOfMessageQueries = new ArrayList<>();

        try {
            for (String metricName : metricList) {
                MessageQuery messageQuery = createBaseMessageQuery(account.getId(), 100);
                messageQuery.setPredicate(storablePredicateFactory.newExistsPredicate(String.format(MessageSchema.MESSAGE_METRICS + ".%s", metricName)));

                stepData.put("messageQuery", messageQuery);
                listOfMessageQueries.add(messageQuery);
            }
            stepData.put("ListOfMessageQueries", listOfMessageQueries);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @And("^I count data messages for more metrics$")
    public void iCountDataMessagesForMoreMetrics() throws Exception {
        List<Long> metricCountList = new ArrayList<>();
        List<MessageQuery> messageQueries = (List<MessageQuery>) stepData.get("ListOfMessageQueries");

        for(MessageQuery messageQuery : messageQueries) {
           long countMetric = messageStoreService.count(messageQuery);
            metricCountList.add(countMetric);
        }
        stepData.put("MetricCountList", metricCountList);
    }

    @Then("^I count (\\d+) data messages$")
    public void iCountMessages(long metricCount) throws AssertionError {
        List<Long> listOfCounts = (List<Long>) (stepData.get("MetricCountList"));

        try {
            long endMetricCount = 0;

            for (Long num : listOfCounts) {
                endMetricCount += num;
            }
            assertEquals("This two values are not equal", metricCount, endMetricCount);
        } catch (AssertionError assertError) {
            verifyAssertionError(assertError);
        }
    }

    // *****************
    // * Inner Classes *
    // *****************
    public static class MetricEntry {

        private final String key;
        private final String type;
        private final String value;

        public MetricEntry(final String key, final String type, final String value) {
            this.key = key;
            this.type = type;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    protected KapuaLocator locator;
    private DeviceRegistryService deviceRegistryService;

    private DeviceFactory deviceFactory;

    private DatastoreClient datastoreClient;

    private DatastoreObjectFactory datastoreObjectFactory;
    private StorablePredicateFactory storablePredicateFactory;
    private StorableIdFactory storableIdFactory;

    private ChannelInfoRegistryService channelInfoRegistryService;
    private ChannelInfoRegistryServiceProxy channelInfoRegistryServiceProxy;

    private MetricInfoRegistryService metricInfoRegistryService;
    private MetricInfoRegistryServiceProxy metricInfoRegistryServiceProxy;

    private ClientInfoRegistryService clientInfoRegistryService;
    private ClientInfoRegistryServiceProxy clientInfoRegistryServiceProxy;

    private MessageStoreService messageStoreService;
    private KapuaMessageFactory messageFactory;
    private KapuaDataMessageFactory dataMessageFactory;

    private SimulatedDevice currentDevice;
    private Session session;
    private String currentApplication;

    @Inject
    public DatastoreSteps(final SimulatedDevice currentDevice, final Session session, StepData stepData) {
        super(stepData);
        this.currentDevice = currentDevice;
        this.session = session;
    }

    @After(value="@setup")
    public void setServices() {
        locator = KapuaLocator.getInstance();
        // Get instance of services used in different scenarios
        deviceRegistryService = locator.getService(DeviceRegistryService.class);
        deviceFactory = locator.getFactory(DeviceFactory.class);
        messageStoreService = locator.getService(MessageStoreService.class);
        messageFactory = locator.getFactory(KapuaMessageFactory.class);
        try {
            datastoreClient = DatastoreClientFactory.getInstance();
        } catch (ClientUnavailableException e) {
            throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e);
        }
        storablePredicateFactory = locator.getFactory(StorablePredicateFactory.class);
        datastoreObjectFactory = locator.getFactory(DatastoreObjectFactory.class);
        storableIdFactory = locator.getFactory(StorableIdFactory.class);
        channelInfoRegistryService = locator.getService(ChannelInfoRegistryService.class);
        channelInfoRegistryServiceProxy = new ChannelInfoRegistryServiceProxy();
        metricInfoRegistryService = locator.getService(MetricInfoRegistryService.class);
        metricInfoRegistryServiceProxy = new MetricInfoRegistryServiceProxy();
        clientInfoRegistryService = locator.getService(ClientInfoRegistryService.class);
        clientInfoRegistryServiceProxy = new ClientInfoRegistryServiceProxy();
        messageFactory = locator.getFactory(KapuaMessageFactory.class);
        dataMessageFactory = locator.getFactory(KapuaDataMessageFactory.class);
    }

    // *************************************
    // Definition of Cucumber scenario steps
    // *************************************

    @Before
    public void beforeScenario(Scenario scenario) {
        updateScenario(scenario);

    }

    @After(value="not (@setup or @teardown)", order=10)
    public void afterScenario() {
        try {
            deleteIndices();
        } catch (Exception e) {
            //do nothing. The datastore container may be destroyed during shutdown
            logger.warn("Error while deleting ES indeces: {}", e.getMessage());
        }
    }

    @Given("I delete indexes \"(.*)\"")
    public void deleteIndexes(String indexExp) throws Exception {
        DatastoreMediator.getInstance().deleteIndexes(indexExp);
        DatastoreMediator.getInstance().refreshAllIndexes();
    }

    @Given("^I delete all indices$")
    public void deleteIndices() throws Exception {

        DatastoreMediator.getInstance().deleteAllIndexes();
    }

    @When("^I refresh all indices$")
    public void refreshIndeces() throws Throwable {

        DatastoreMediator.getInstance().refreshAllIndexes();
    }

    @When("^I clear all the database caches$")
    public void clearDatabaseCaches() {

        DatastoreCacheManager.getInstance().getChannelsCache().invalidateAll();
        DatastoreCacheManager.getInstance().getClientsCache().invalidateAll();
        DatastoreCacheManager.getInstance().getMetricsCache().invalidateAll();
        DatastoreCacheManager.getInstance().getMetadataCache().invalidateAll();
    }

    @Given("I have a mock data application named \"(.*)\"")
    public void addMockDataApplication(final String applicationId) {
        Assert.assertFalse(currentDevice.isStarted());

        final SimulatedDeviceApplication app = new SimulatedDeviceApplication(applicationId);
        currentDevice.getMockApplications().put(applicationId, app);
    }

    @Given("I publish for the application \"(.*)\"")
    public void selectPublishApplication(final String applicationId) {
        currentApplication = applicationId;
    }

    @When("I publish on the topic \"(.*)\" timestamped now")
    public void publishMetric(final String topic, final List<MetricEntry> metrics) {

        final Map<String, Object> data = toData(metrics);

        final SimulatedDeviceApplication app = getMockApplication(currentApplication);
        app.publishData(topic, Instant.now(), data);
    }

    @Then("I expect the number of messages for this device to be (\\d+)")
    public void expectNumberOfMessages(long numberOfMessages) throws Exception {
        final MessageStoreService service = KapuaLocator.getInstance().getService(MessageStoreService.class);
        session.withLogin(() -> {
            With.withUserAccount(currentDevice.getAccountName(), account -> {

                // create new query

                final MessageQuery query = datastoreObjectFactory.newDatastoreMessageQuery(account.getId());

                // filter for client only

                query.setPredicate(storablePredicateFactory.newTermPredicate(MessageField.CLIENT_ID, currentDevice.getClientId()));

            // set query options
            query.setAskTotalCount(true);
            query.setLimit((int)numberOfMessages);

                // perform query

                final MessageListResult result = service.query(query);

                // eval just the size

                Assert.assertEquals(numberOfMessages, result.getSize());

                // eval the total count

                Assert.assertEquals(Long.valueOf(numberOfMessages), result.getTotalCount());

                // different approach -> same result

                Assert.assertEquals(numberOfMessages, service.count(query));
            });
        });
    }

    @Then("I delete the messages for this device")
    public void deleteMessages() throws Exception {
        final MessageStoreService service = KapuaLocator.getInstance().getService(MessageStoreService.class);
        session.withLogin(() -> {
            With.withUserAccount(currentDevice.getAccountName(), account -> {

                // create new query
                final MessageQuery query = datastoreObjectFactory.newDatastoreMessageQuery(account.getId());

                // filter for client only
                query.setPredicate(storablePredicateFactory.newTermPredicate(MessageField.CLIENT_ID, currentDevice.getClientId()));

                // set query options
                query.setAskTotalCount(true);
                query.setLimit(100);

                // perform delete
                service.delete(query);
            });
        });
    }

    @Then("I expect the latest captured message on channel \"(.*)\" to have the metrics")
    public void testMessageData(final String topic, final List<MetricEntry> expectedMetrics) throws Exception {
        final MessageStoreService service = KapuaLocator.getInstance().getService(MessageStoreService.class);
        session.withLogin(() -> {
            With.withUserAccount(currentDevice.getAccountName(), account -> {

                // start a new query

                final MessageQuery query = datastoreObjectFactory.newDatastoreMessageQuery(account.getId());

                // query for client and channel

                final AndPredicate and = storablePredicateFactory.newAndPredicate();
                and.getPredicates().add(storablePredicateFactory.newTermPredicate(MessageField.CLIENT_ID, currentDevice.getClientId()));
                and.getPredicates().add(storablePredicateFactory.newTermPredicate(MessageField.CHANNEL, topic));
                query.setPredicate(and);

                // sort by captured time

                query.setSortFields(Arrays.asList(SortField.descending(MessageField.CAPTURED_ON.field())));

                // perform the query

                final MessageListResult result = service.query(query);

                Assert.assertEquals(1, result.getSize());

                // get the first item

                final DatastoreMessage message = result.getFirstItem();
                Assert.assertEquals(currentDevice.getClientId(), message.getClientId());

                // get payload structure

                final KapuaPayload payload = message.getPayload();

                // assert metrics data

                final Map<String, Object> properties = payload.getMetrics();
                Assert.assertEquals(toData(expectedMetrics), properties);
            });
        });
    }

    @When("^I set the datastore indexing window to \"(.+)\"$")
    public void setDatastoreIndexingWindowTo(String window) {

        setDatastoreIndexingWindowOption(window);
        stepData.put("IndexingWindow", window);
    }

    @Then("^The index name is \"(.+)\"$")
    public void checkCurrentDateIndexName(String name) throws Exception {

        Date currentDate = (Date) stepData.get("Date");
        Instant instant = currentDate.toInstant();
        String window = (String) stepData.get("IndexingWindow");
        String indexName = "";

        primeException();
        try {
            indexName = DatastoreUtils.getDataIndexName(SYS_SCOPE_ID, instant.toEpochMilli(), window);
        } catch (KapuaException ex) {
            verifyException(ex);
        }

        Assert.assertEquals(name, indexName);
    }

    @Given("^I prepare a random message and save it as \"(.*)\"$")
    public void prepareAndRememberARandomMessage(String msgKey) throws Exception {

        KapuaDataMessage tmpMessage = createTestMessage(((Account) stepData.get("LastAccount")).getId(),
                ((Device) stepData.get("LastDevice")).getId(),
                ((Device) stepData.get("LastDevice")).getClientId(),
                "", "");
        stepData.put(msgKey, tmpMessage);
    }

    @Given("^I prepare a random message with capture date \"(.*)\" and save it as \"(.*)\"$")
    public void prepareAndRememberARandomMessage(String captureDate, String msgKey) throws Exception {

        KapuaDataMessage tmpMessage = createTestMessage(((Account) stepData.get("LastAccount")).getId(),
                ((Device) stepData.get("LastDevice")).getId(),
                ((Device) stepData.get("LastDevice")).getClientId(),
                null, captureDate);
        stepData.put(msgKey, tmpMessage);
    }

    @Given("^I prepare a random message with null payload and save it as \"(.*)\"$")
    public void prepareRandomMessageWithNullPayload(String msgKey) throws Exception {

        KapuaDataMessage tmpMessage = createTestMessage(
                ((Account) stepData.get("LastAccount")).getId(),
                ((Device) stepData.get("LastDevice")).getId(),
                ((Device) stepData.get("LastDevice")).getClientId(),
                "", "");
        tmpMessage.setPayload(null);
        stepData.put(msgKey, tmpMessage);
    }

    @Given("^I prepare a number of messages with the following details and remember the list as \"(.*)\"$")
    public void prepareAListOfMessagesWithDifferentTopics(String listKey, List<CucTopic> topics) throws Exception {

        List<KapuaDataMessage> tmpList = new ArrayList<>();
        KapuaDataMessage tmpMsg = null;

        for (CucTopic tmpTopic : topics) {
            tmpMsg = createTestMessage(
                    ((Account) stepData.get("LastAccount")).getId(),
                    ((Device) stepData.get("LastDevice")).getId(),
                    tmpTopic.getClientId(),
                    tmpTopic.getTopic(),
                    tmpTopic.getCaptured());
            tmpList.add(tmpMsg);
        }

        stepData.put(listKey, tmpList);
    }

    @Given("^I prepare a number of messages in the specified ranges and remember the list as \"(.*)\"$")
    public void prepareAListOfMessagesInTheSpecifiedRanges(String listKey, List<CucMessageRange> messages) throws Exception {

        List<KapuaDataMessage> tmpList = new ArrayList<>();
        KapuaDataMessage tmpMsg;
        Calendar tmpCal = Calendar.getInstance();

        for (CucMessageRange tmpMessage : messages) {
            Date startDate = KapuaDateUtils.parseDate(tmpMessage.getStartDate());
            Date endDate = KapuaDateUtils.parseDate(tmpMessage.getEndDate());
            long stepSeconds = (endDate.getTime() - startDate.getTime()) / (1000L * (tmpMessage.getCount().longValue() - 1));

            tmpCal.setTime(startDate);
            for (int cnt = 0; cnt <= tmpMessage.getCount(); cnt++) {
                tmpMsg = createTestMessage(
                        ((Account) stepData.get("LastAccount")).getId(),
                        ((Device) stepData.get("LastDevice")).getId(),
                        tmpMessage.getClientId(),
                        tmpMessage.getTopic(),
                        tmpCal.getTime());
                tmpList.add(tmpMsg);
                tmpCal.add(Calendar.SECOND, (int) stepSeconds);
            }
        }

        stepData.put(listKey, tmpList);
    }

    @And("^I set the following metrics to the message (\\d+) from the list \"(.+)\"$")
    public void appendMetricsToSelectedMessage(int idx, String lstKey, List<CucMetric> metLst) throws Exception {

        List<KapuaDataMessage> tmpMsgLst = (List<KapuaDataMessage>) stepData.get(lstKey);
        KapuaDataMessage tmpMsg = tmpMsgLst.get(idx);

        tmpMsg.setPayload(dataMessageFactory.newKapuaDataPayload());

        for (CucMetric tmpMet : metLst) {
            switch (tmpMet.getType().trim().toLowerCase()) {
                case "string": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), tmpMet.getValue());
                    break;
                }
                case "int": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), Integer.valueOf(tmpMet.getValue()));
                    break;
                }
                case "double": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), Double.valueOf(tmpMet.getValue()));
                    break;
                }
                case "bool": {
                    tmpMsg.getPayload().getMetrics().put(tmpMet.getMetric(), Boolean.valueOf(tmpMet.getValue().trim().toUpperCase()));
                    break;
                }
                default: {
                    fail(String.format("Unknown metric type [%s]", tmpMet.getType()));
                    break;
                }
            }
        }
    }

    @Given("^I prepare (\\d+) random messages and remember the list as \"(.*)\"$")
    public void prepareAndRememberANumberOfRandomMessages(int number, String lstKey) throws Exception {

        KapuaDataMessage tmpMsg;
        List<KapuaDataMessage> msgList = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            tmpMsg = createTestMessage(((Account) stepData.get("LastAccount")).getId(),
                    ((Device) stepData.get("LastDevice")).getId(),
                    ((Device) stepData.get("LastDevice")).getClientId(),
                    "", "");
            msgList.add(tmpMsg);
        }

        stepData.put(lstKey, msgList);
    }

    @Given("^I prepare (\\d+) randomly ordered messages and remember the list as \"(.*)\"$")
    public void prepareUnorderedRandomMessagesWithTopics(int number, String lstKey, List<CucTopic> topics) throws Exception {

        Account tmpAccount = (Account) stepData.get("LastAccount");
        KapuaDataMessage tmpMsg;
        List<KapuaDataMessage> tmpList = new ArrayList<>();
        String clientId1 = "test-device-1";
        Date sentOn1 = new Date(new Date().getTime() - 10000);
        Date capturedOn1 = new Date(new Date().getTime() - 500);
        DeviceCreator tmpDevCr1 = deviceFactory.newCreator(tmpAccount.getId(), clientId1);
        Device device1 = deviceRegistryService.create(tmpDevCr1);
        String clientId2 = "test-device-2";
        Date sentOn2 = new Date(sentOn1.getTime() + 1000);
        Date capturedOn2 = new Date(new Date().getTime());
        DeviceCreator tmpDevCr2 = deviceFactory.newCreator(tmpAccount.getId(), clientId2);
        Device device2 = deviceRegistryService.create(tmpDevCr2);
        Device tmpDev;
        Date sentOn, capturedOn;

        String[] metrics = new String[]{
                "m_order_metric1",
                "m_order_metric2",
                "m_order_metric3",
                "m_order_metric4",
                "m_order_metric5",
                "m_order_metric6"};
        String[] metricsValuesString = new String[]{
                "string_metric_1",
                "string_metric_2",
                "string_metric_3",
                "string_metric_4",
                "string_metric_5",
                "string_metric_6"};
        Date[] metricsValuesDate = new Date[]{
                new Date(new SimpleDateFormat("hh:MM:ss dd/MM/yyyy").parse("10:10:01 01/01/2017").getTime()),
                new Date(new SimpleDateFormat("hh:MM:ss dd/MM/yyyy").parse("10:10:02 01/01/2017").getTime()),
                new Date(new SimpleDateFormat("hh:MM:ss dd/MM/yyyy").parse("10:10:03 01/01/2017").getTime()),
                new Date(new SimpleDateFormat("hh:MM:ss dd/MM/yyyy").parse("10:10:04 01/01/2017").getTime()),
                new Date(new SimpleDateFormat("hh:MM:ss dd/MM/yyyy").parse("10:10:05 01/01/2017").getTime()),
                new Date(new SimpleDateFormat("hh:MM:ss dd/MM/yyyy").parse("10:10:06 01/01/2017").getTime())};
        int[] metricsValuesInt = new int[]{10, 20, 30, 40, 50, 60};
        float[] metricsValuesFloat = new float[]{0.002f, 10.12f, 20.22f, 33.33f, 44.44f, 55.66f};
        double[] metricsValuesDouble = new double[]{1.002d, 11.12d, 21.22d, 34.33d, 45.44d, 56.66d};
        boolean[] metricsValuesBoolean = new boolean[]{true, true, false, true, false, false};

        for (int i = 0; i < number; i++) {
            if (i < number / 4) {
                sentOn = sentOn1;
                capturedOn = capturedOn1;
            } else if (i < number / 2) {
                sentOn = sentOn1;
                capturedOn = capturedOn2;
            } else if (i < number * 3 / 4) {
                sentOn = sentOn2;
                capturedOn = capturedOn1;
            } else {
                sentOn = sentOn2;
                capturedOn = capturedOn2;
            }
            if (i % 2 == 0) {
                tmpDev = device1;
            } else {
                tmpDev = device2;
            }

            Date receivedOn = new Date();
            CucTopic tmpTopic = topics.get(i % topics.size());
            tmpMsg = createTestMessage(
                    tmpAccount.getId(),
                    tmpDev.getId(),
                    tmpDev.getClientId(),
                    tmpTopic.getTopic(),
                    "");
            tmpMsg.setCapturedOn(capturedOn);
            tmpMsg.setSentOn(sentOn);
            tmpMsg.setReceivedOn(receivedOn);
            tmpMsg.getPayload().getMetrics().put(metrics[0], metricsValuesDate[i % metricsValuesDate.length]);
            tmpMsg.getPayload().getMetrics().put(metrics[1], metricsValuesString[i % metricsValuesString.length]);
            tmpMsg.getPayload().getMetrics().put(metrics[2], metricsValuesInt[i % metricsValuesInt.length]);
            tmpMsg.getPayload().getMetrics().put(metrics[3], metricsValuesFloat[i % metricsValuesFloat.length]);
            tmpMsg.getPayload().getMetrics().put(metrics[4], metricsValuesBoolean[i % metricsValuesBoolean.length]);
            tmpMsg.getPayload().getMetrics().put(metrics[5], metricsValuesDouble[i % metricsValuesDouble.length]);
            tmpList.add(tmpMsg);
        }

        stepData.put(lstKey, tmpList);
    }

    @Given("^I store the message \"(.*)\" and remember its ID as \"(.*)\"$")
    public void insertRandomMessageIntoDatastore(String msgKey, String idKey) throws KapuaException {

        KapuaDataMessage tmpMessage = (KapuaDataMessage) stepData.get(msgKey);
        StorableId storeId = insertMessageInStore(tmpMessage);
        stepData.put(idKey, storeId);
    }

    @Given("^I store the message \"(.*)\" with the server time and remember its ID as \"(.*)\"$")
    public void insertRandomMessageIntoDatastoreWithCurrentTimestamp(String msgKey, String idKey) throws KapuaException {

        KapuaDataMessage tmpMessage = (KapuaDataMessage) stepData.get(msgKey);
        tmpMessage.setReceivedOn(new Date());
        StorableId storeId = insertMessageInStore(tmpMessage);
        stepData.put(idKey, storeId);
    }

    @Given("^I store the messages from list \"(.*)\" and remember the IDs as \"(.*)\"$")
    public void insertRandomMessagesIntoDatastore(String msgListKey, String idListKey) throws KapuaException {

        List<KapuaDataMessage> tmpMsgList = (List<KapuaDataMessage>) stepData.get(msgListKey);
        List<StorableId> tmpIdList = insertMessagesInStore(tmpMsgList);
        stepData.put(idListKey, tmpIdList);
    }

    @Given("^I store the messages from list \"(.*)\" with the server time and remember the IDs as \"(.*)\"$")
    public void insertRandomMessagesIntoDatastoreWithCurrentTimestamps(String msgListKey, String idListKey) throws KapuaException {

        List<KapuaDataMessage> tmpMsgList = (List<KapuaDataMessage>) stepData.get(msgListKey);
        StorableId tmpId = null;
        List<StorableId> tmpList = new ArrayList<>();

        for (KapuaDataMessage tmpMsg : tmpMsgList) {
            tmpMsg.setReceivedOn(new Date());
            tmpId = insertMessageInStore(tmpMsg);
            tmpList.add(tmpId);
        }

        stepData.put(idListKey, tmpList);
    }

    @Given("^I set the database to device timestamp indexing$")
    public void setDatabaseToDeviceTimestampIndexing() throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        updateConfiguration(messageStoreService, account.getId(), account.getScopeId(),
                DataIndexBy.DEVICE_TIMESTAMP, MetricsIndexBy.TIMESTAMP, 30, true);
    }

    @Given("^I set the database to server timestamp indexing$")
    public void setDatabaseToServerTimestampIndexing() throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        updateConfiguration(messageStoreService, account.getId(), account.getScopeId(),
                DataIndexBy.SERVER_TIMESTAMP, MetricsIndexBy.TIMESTAMP, 30, true);
    }

    @When("^I delete the datastore message with ID \"(.*)\"$")
    public void deleteDatastoreMessage(String idKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        StorableId msgId = (StorableId) stepData.get(idKey);
        messageStoreService.delete(account.getId(), msgId);
    }

    @When("^I pick the ID number (\\d+) from the list \"(.*)\" and remember it as \"(.*)\"$")
    public void pickAMessageIdFromAList(int index, String lstKey, String idKey) {

        List<StorableId> msgIds = (List<StorableId>) stepData.get(lstKey);
        StorableId tmpId = msgIds.get(index);
        stepData.put(idKey, tmpId);
    }

    @When("^I pick the ID of the channel number (\\d+) in the list \"(.*)\" and remember it as \"(.*)\"$")
    public void pickAChannelIdFromAList(int index, String lstKey, String idKey) {

        ChannelInfoListResult tmpLst = (ChannelInfoListResult) stepData.get(lstKey);
        StorableId tmpId = tmpLst.getItem(index).getId();
        stepData.put(idKey, tmpId);
    }

    @When("^I pick message number (\\d+) from the list \"(.+)\" and remember it as \"(.+)\"$")
    public void pickADatastoreMessageFromList(int index, String lstKey, String msgKey) {

        MessageListResult tmpList = (MessageListResult) stepData.get(lstKey);
        DatastoreMessage tmpMsg = tmpList.getItem(index);
        stepData.put(msgKey, tmpMsg);
    }

    @When("^I search for the last inserted message$")
    public void findLastInsertedMessage() throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        StorableId msgId = (StorableId) stepData.get("LastStoredMessageId");
        DatastoreMessage tmpMsg = messageStoreService.find(account.getId(), msgId, StorableFetchStyle.SOURCE_FULL);
        stepData.put("LastMessageInDatastore", tmpMsg);
    }

    @When("^I search for the last inserted message and save it as \"(.*)\"$")
    public void findLastInsertedMessage(String keyName) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        StorableId msgId = (StorableId) stepData.get("LastStoredMessageId");
        DatastoreMessage tmpMsg = messageStoreService.find(account.getId(), msgId, StorableFetchStyle.SOURCE_FULL);
        stepData.put(keyName, tmpMsg);
    }

    @When("^I search for a data message with ID \"(.*)\" and remember it as \"(.*)\"")
    public void searchForMessageInDatastore(String idKey, String msgKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        StorableId tmpId = (StorableId) stepData.get(idKey);
        DatastoreMessage tmpMsg = messageStoreService.find(account.getId(), tmpId, StorableFetchStyle.SOURCE_FULL);
        stepData.put(msgKey, tmpMsg);
    }

    @When("^I search for messages with IDs from the list \"(.*)\" and store them in the list \"(.*)\"$")
    public void searchForMessagesInTheDatastore(String idListKey, String msgListKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        List<StorableId> msgIdLst = (List<StorableId>) stepData.get(idListKey);
        List<DatastoreMessage> tmpMsgLst = new ArrayList<>();
        DatastoreMessage tmpMsg;

        for (StorableId tmpId : msgIdLst) {
            tmpMsg = messageStoreService.find(account.getId(), tmpId, StorableFetchStyle.SOURCE_FULL);
            tmpMsgLst.add(tmpMsg);
        }

        stepData.put(msgListKey, tmpMsgLst);
    }

    @When("^I perform a default query for the account messages and store the results as \"(.+)\"$")
    public void performADefaultMessageQuery(String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MessageQuery query = createBaseMessageQuery(account.getId(), 100);
        MessageListResult msgList = messageStoreService.query(query);
        stepData.put(lstKey, msgList);
    }

    @When("^I perform an ordered query for messages and store the results as \"(.*)\"$")
    public void performAnOrderedMessageQuery(String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MessageQuery query = getBaseMessageQuery(account.getId(), 100, getDefaultListOrdering());
        MessageListResult msgList = messageStoreService.query(query);
        stepData.put(lstKey, msgList);
    }

    @When("^I count the current account messages and store the count as \"(.*)\"$")
    public void countAccountMessages(String countKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MessageQuery msgQuery = createBaseMessageQuery(account.getId(), 100);

        long messageCount = messageStoreService.count(msgQuery);
        stepData.put(countKey, messageCount);
    }

    @When("^I query for the current account channel(?:|s) (?:|again )and store (?:it|them) as \"(.*)\"$")
    public void queryForAccountChannelInfo(String chnKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfoQuery tmpQuery = createBaseChannelInfoQuery(account.getId(), 100);
        tmpQuery.addFetchAttributes(ChannelInfoField.TIMESTAMP.field());
        ChannelInfoListResult tmpList = channelInfoRegistryService.query(tmpQuery);
        stepData.put(chnKey, tmpList);
    }

    @When("^I query for the current account channels in the date range \"(.+)\" to \"(.+)\" and store them as \"(.+)\"$")
    public void queryForAccountChannelInfoByDateRange(String start, String end, String lstKey) throws Exception {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfoQuery tmpQuery = createBaseChannelInfoQuery(account.getId(), 100);
        RangePredicate timestampPredicate = storablePredicateFactory.newRangePredicate(ChannelInfoField.TIMESTAMP.field(),
                KapuaDateUtils.parseDate(start), KapuaDateUtils.parseDate(end));
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(timestampPredicate);
        tmpQuery.setPredicate(andPredicate);
        tmpQuery.addFetchAttributes(ChannelInfoField.TIMESTAMP.field());
        ChannelInfoListResult tmpList = channelInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @When("^I query for the current account channels with the filter \"(.+)\" and store them as \"(.+)\"$")
    public void queryForAccountChannelInfoWithFiltering(String filter, String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        DatastoreChannel datastoreChannel = new DatastoreChannel(filter);
        ChannelInfoQuery tmpQuery = createBaseChannelInfoQuery(account.getId(), 100);
        ChannelMatchPredicate channelMatchPredicate = storablePredicateFactory.newChannelMatchPredicate(datastoreChannel.getChannelCleaned());
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(channelMatchPredicate);
        tmpQuery.setPredicate(andPredicate);
        tmpQuery.addFetchAttributes(ChannelInfoField.TIMESTAMP.field());
        ChannelInfoListResult tmpList = channelInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @Then("^The channel list \"(.+)\" contains the following topics$")
    public void checkThattheChannelInfoContainsTheSpecifiedTopics(String lstKey, List<CucTopic> topics) {

        ChannelInfoListResult tmpList = (ChannelInfoListResult) stepData.get(lstKey);
        Set<String> infoTopics = new HashSet<>();

        assertEquals("Wrong number of topics found!", tmpList.getSize(), topics.size());

        for (ChannelInfo tmpInfo : tmpList.getItems()) {
            infoTopics.add(tmpInfo.getName());
        }
        for (CucTopic tmpTop : topics) {
            assertTrue(String.format("The topic [%s] was not found!", tmpTop.getTopic()), infoTopics.contains(tmpTop.getTopic()));
        }
    }

    @When("^I count the current account channels and store the count as \"(.*)\"$")
    public void countAccountChannels(String countKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfoQuery tmpQuery = createBaseChannelInfoQuery(account.getId(), 100);

        long channelCount = channelInfoRegistryService.count(tmpQuery);
        stepData.put(countKey, channelCount);
    }

    @Then("^There (?:is|are) exactly (\\d+) channel(?:|s) in the list \"(.*)\"$")
    public void checkNumberOfQueriedChannels(int cnt, String lstKey) {

        assertEquals(cnt, ((ChannelInfoListResult) stepData.get(lstKey)).getSize());
    }

    @Then("^Client \"(.+)\" first published on a channel in the list \"(.+)\" on \"(.+)\"$")
    public void checkFirstPublishDateOnChannel(String clientId, String lstKey, String date) throws Exception {

        ChannelInfoListResult chnList = (ChannelInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (ChannelInfo tmpInfo : chnList.getItems()) {
            if (tmpInfo.getClientId().equals(clientId)) {
                assertEquals(tmpCaptured, tmpInfo.getFirstMessageOn());
                return;
            }
        }
        fail(String.format("No channel matches the client id [%s]", clientId));
    }

    @Then("^Client \"(.+)\" last published on a channel in the list \"(.+)\" on \"(.+)\"$")
    public void checkLastPublishDateOnChannel(String clientId, String lstKey, String date) throws Exception {

        ChannelInfoListResult chnList = (ChannelInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (ChannelInfo tmpInfo : chnList.getItems()) {
            if (tmpInfo.getClientId().equals(clientId)) {
                assertEquals(tmpCaptured, tmpInfo.getLastMessageOn());
                return;
            }
        }
        fail(String.format("No channel matches the client id [%s]", clientId));
    }

    @When("^I delete all channels from the list \"(.*)\"$")
    public void deleteAllChannelsFromList(String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfoListResult tmpList = (ChannelInfoListResult) stepData.get(lstKey);

        for (ChannelInfo tmpItem : tmpList.getItems()) {
            channelInfoRegistryServiceProxy.delete(account.getId(), tmpItem.getId());
        }
    }

    @When("^I delete the channel with the ID \"(.*)\"$")
    public void deleteChannelWithId(String idKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        StorableId tmpId = (StorableId) stepData.get(idKey);

        channelInfoRegistryServiceProxy.delete(account.getId(), tmpId);
    }

    @When("^I query for the current account metrics (?:|again )and store (?:it|them) as \"(.*)\"$")
    public void queryForAccountMetrics(String metricKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(account.getId(), 100);
        tmpQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());

        stepData.put("metricInfoQuery", tmpQuery);
        MetricInfoListResult tmpList = metricInfoRegistryService.query(tmpQuery);
        stepData.put(metricKey, tmpList);
    }

    @When("^I query for the current account metrics in the date range \"(.+)\" to \"(.+)\" and store them as \"(.+)\"$")
    public void queryForAccountMetricsInfoByDateRange(String start, String end, String lstKey) throws Exception {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(account.getId(), 100);
        RangePredicate timestampPredicate = storablePredicateFactory.newRangePredicate(MetricInfoField.TIMESTAMP_FULL.field(),
                KapuaDateUtils.parseDate(start), KapuaDateUtils.parseDate(end));
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(timestampPredicate);
        tmpQuery.setPredicate(andPredicate);
        tmpQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());

        stepData.put("metricInfoQuery", tmpQuery);
        MetricInfoListResult tmpList = metricInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @When("^I perform an ordered query for the current account metrics in the date range \"(.+)\" to \"(.+)\" and store them as \"(.+)\"$")
    public void queryForAccountMetricsInfoByDateRangeOrdered(String start, String end, String lstKey) throws Exception {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(account.getId(), 100);
        RangePredicate timestampPredicate = storablePredicateFactory.newRangePredicate(MetricInfoField.TIMESTAMP_FULL.field(),
                KapuaDateUtils.parseDate(start), KapuaDateUtils.parseDate(end));
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(timestampPredicate);
        tmpQuery.setPredicate(andPredicate);
        tmpQuery.setSortFields(getNamedMetricOrdering().stream().map(OrderConstraint::getField).collect(Collectors.toList()));
        tmpQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());

        stepData.put("metricInfoQuery", tmpQuery);
        MetricInfoListResult tmpList = metricInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @When("^I perform an ordered query for metrics and store the results as \"(.*)\"$")
    public void performAnOrderedMetricQuery(String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery query = getBaseMetricQuery(account.getId(), 100, getNamedMetricOrdering());
        query.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());
        MetricInfoListResult metList = metricInfoRegistryService.query(query);
        stepData.put(lstKey, metList);
    }

    @When("^I count the current account metrics and store the count as \"(.*)\"$")
    public void countAccountMetrics(String countKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(account.getId(), 100);

        stepData.put("metricInfoQuery", tmpQuery);
        stepData.remove(countKey);
        long metricCount = metricInfoRegistryService.count(tmpQuery);
        stepData.put(countKey, metricCount);
    }

    @When("^I query for the metrics in topic \"(.*)\" and store them as \"(.*)\"$")
    public void queryForTopicMetrics(String topic, String lstKey) throws KapuaException {

        stepData.remove("metricInfoQuery");
        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(account.getId(), 100);
        tmpQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        TermPredicate tmpPred = storablePredicateFactory.newTermPredicate(MetricInfoField.CHANNEL, topic);
        MetricInfoListResult tmpList = null;

        andPredicate.getPredicates().add(tmpPred);
        tmpQuery.setPredicate(andPredicate);
        stepData.put("metricInfoQuery", tmpQuery);
        stepData.remove(lstKey);
        tmpList = metricInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @When("^I query for the metrics from client \"(.*)\" and store them as \"(.*)\"$")
    public void queryForClientMetrics(String clientId, String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(account.getId(), 100);
        tmpQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        TermPredicate tmpPred = storablePredicateFactory.newTermPredicate(MetricInfoField.CLIENT_ID, clientId);
        MetricInfoListResult tmpList = null;

        andPredicate.getPredicates().add(tmpPred);
        tmpQuery.setPredicate(andPredicate);
        stepData.put("metricInfoQuery", tmpQuery);
        tmpList = metricInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @Then("^There (?:is|are) exactly (\\d+) metric(?:|s) in the list \"(.*)\"$")
    public void checkNumberOfQueriedMetrics(int cnt, String lstKey) {

        MetricInfoListResult tmpResults = (MetricInfoListResult) stepData.get(lstKey);
        assertEquals(cnt, tmpResults.getSize());
    }

    @Then("^Client \"(.+)\" first published a metric in the list \"(.+)\" on \"(.+)\"$")
    public void checkFirstPublishDateOfClientMetric(String clientId, String lstKey, String date) throws Exception {

        MetricInfoListResult metList = (MetricInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (MetricInfo tmpMet : metList.getItems()) {
            if (tmpMet.getClientId().equals(clientId)) {
                assertEquals(tmpMet.getFirstMessageOn(), tmpCaptured);
                return;
            }
        }
        fail(String.format("No metric matches the client id [%s]", clientId));
    }

    @Then("^Client \"(.+)\" last published a metric in the list \"(.+)\" on \"(.+)\"$")
    public void checkLastPublishDateOfClientMetric(String clientId, String lstKey, String date) throws Exception {

        MetricInfoListResult metList = (MetricInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (MetricInfo tmpMet : metList.getItems()) {
            if (tmpMet.getClientId().equals(clientId)) {
                assertEquals(tmpMet.getLastMessageOn(), tmpCaptured);
                return;
            }
        }
        fail(String.format("No metric matches the client id [%s]", clientId));
    }

    @Then("^The metric \"(.+)\" was first published in the list \"(.+)\" on \"(.+)\"$")
    public void checkFirstPublishDateOfMetricInList(String metric, String lstKey, String date) throws Exception {

        MetricInfoListResult metList = (MetricInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (MetricInfo tmpMet : metList.getItems()) {
            if (tmpMet.getName().equals(metric)) {
                assertEquals(tmpMet.getFirstMessageOn(), tmpCaptured);
                return;
            }
        }
        fail(String.format("There is no metric [%s]", metric));
    }

    @Then("^The metric \"(.+)\" was last published in the list \"(.+)\" on \"(.+)\"$")
    public void checkLastPublishDateOfMetricInList(String metric, String lstKey, String date) throws Exception {

        MetricInfoListResult metList = (MetricInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (MetricInfo tmpMet : metList.getItems()) {
            if (tmpMet.getName().equals(metric)) {
                assertEquals(tmpMet.getLastMessageOn(), tmpCaptured);
                return;
            }
        }
        fail(String.format("There is no metric [%s]", metric));
    }

    @When("^I delete all metrics from the list \"(.*)\"$")
    public void deleteAllMetricsFromList(String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoListResult tmpList = (MetricInfoListResult) stepData.get(lstKey);

        for (MetricInfo tmpItem : tmpList.getItems()) {
            metricInfoRegistryServiceProxy.delete(account.getId(), tmpItem.getId());
        }
    }

    @When("^I query for the current account client(?:|s) (?:|again )and store (?:it|them) as \"(.*)\"$")
    public void queryForAccountClientInfo(String clientKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoQuery tmpQuery = createBaseClientInfoQuery(account.getId(), 100);
        tmpQuery.addFetchAttributes(ClientInfoField.TIMESTAMP.field());
        ClientInfoListResult tmpList = clientInfoRegistryService.query(tmpQuery);
        stepData.put(clientKey, tmpList);
    }

    @When("^I query for current account clients in the date range \"(.+)\" to \"(.+)\" and store them as \"(.+)\"$")
    public void queryForAccountClientInfoByDateRange(String start, String end, String lstKey) throws Exception {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoQuery tmpQuery = createBaseClientInfoQuery(account.getId(), 100);
        RangePredicate timestampPredicate = storablePredicateFactory.newRangePredicate(ClientInfoField.TIMESTAMP.field(),
                KapuaDateUtils.parseDate(start), KapuaDateUtils.parseDate(end));
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(timestampPredicate);
        tmpQuery.setPredicate(andPredicate);
        tmpQuery.addFetchAttributes(ClientInfoField.TIMESTAMP.field());

        ClientInfoListResult tmpList = clientInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @When("^I query for the current account client with the Id \"(.+)\" and store it as \"(.+)\"$")
    public void queryForAccountClientInfoByClientId(String clientId, String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoQuery tmpQuery = createBaseClientInfoQuery(account.getId(), 100);
        TermPredicate clientIdPredicate = storablePredicateFactory.newTermPredicate(MetricInfoField.CLIENT_ID, clientId);
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(clientIdPredicate);
        tmpQuery.setPredicate(andPredicate);
        tmpQuery.addFetchAttributes(ClientInfoField.TIMESTAMP.field());

        ClientInfoListResult tmpList = clientInfoRegistryService.query(tmpQuery);
        stepData.put(lstKey, tmpList);
    }

    @And("^I create message query for metric \"(.*)\"$")
    public void iQueryForMessages(String metricName) throws Exception {
        Account account = (Account) stepData.get("LastAccount");

        try {
            MessageQuery messageQuery = createBaseMessageQuery(account.getId(), 100);
            messageQuery.setPredicate(storablePredicateFactory.newExistsPredicate(String.format(MessageSchema.MESSAGE_METRICS + ".%s", metricName)));

            stepData.put("messageQuery", messageQuery);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }


    @When("^I count the current account clients and store the count as \"(.*)\"$")
    public void countAccountClients(String countKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoQuery tmpQuery = createBaseClientInfoQuery(account.getId(), 100);

        long clientCount = clientInfoRegistryService.count(tmpQuery);
        stepData.put(countKey, clientCount);
    }

    @Then("^There (?:is|are) exactly (\\d+) client(?:|s) in the list \"(.*)\"$")
    public void checkNumberOfQueriedClients(int cnt, String lstKey) {

        assertEquals(cnt, ((ClientInfoListResult) stepData.get(lstKey)).getSize());
    }

    @Then("^Client \"(.+)\" first message in the list \"(.+)\" is on \"(.+)\"$")
    public void checkFirstPublishDateForClient(String clientId, String lstKey, String date) throws Exception {

        ClientInfoListResult cliList = (ClientInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (ClientInfo tmpInfo : cliList.getItems()) {
            if (tmpInfo.getClientId().equals(clientId)) {
                assertEquals(tmpInfo.getFirstMessageOn(), tmpCaptured);
                return;
            }
        }
        fail(String.format("No client info item matches the client id [%s]", clientId));
    }

    @Then("^Client \"(.+)\" last message in the list \"(.+)\" is on \"(.+)\"$")
    public void checkLastPublishDateForClient(String clientId, String lstKey, String date) throws Exception {

        ClientInfoListResult cliList = (ClientInfoListResult) stepData.get(lstKey);
        Date tmpCaptured = KapuaDateUtils.parseDate(date);

        for (ClientInfo tmpInfo : cliList.getItems()) {
            if (tmpInfo.getClientId().equals(clientId)) {
                assertEquals(tmpInfo.getLastMessageOn(), tmpCaptured);
                return;
            }
        }
        fail(String.format("No client info item matches the client id [%s]", clientId));
    }

    @When("^I delete all clients from the list \"(.*)\"$")
    public void deleteAllClientsFromList(String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoListResult tmpList = (ClientInfoListResult) stepData.get(lstKey);

        for (ClientInfo tmpItem : tmpList.getItems()) {
            clientInfoRegistryServiceProxy.delete(account.getId(), tmpItem.getId());
        }
    }

    @When("^I delete client number (\\d+) from the list \"(.*)\"$")
    public void deleteClientFromList(int index, String lstKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoListResult tmpList = (ClientInfoListResult) stepData.get(lstKey);

        clientInfoRegistryServiceProxy.delete(account.getId(), tmpList.getItem(index).getId());
    }

    @When("^I search for data message with id \"(.*)\"")
    public void messageStoreFind(String storeId) throws Exception {
        Account account = (Account) stepData.get("LastAccount");
        try {
            primeException();
            DatastoreMessage tmpMsg = messageStoreService.find(account.getId(), storableIdFactory.newStorableId(storeId), StorableFetchStyle.SOURCE_FULL);
            stepData.put("message", tmpMsg);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @Then("^I don't find message$")
    public void dontFindMessage() {

        DatastoreMessage message = (DatastoreMessage) stepData.get("message");
        assertNull(message);
    }

    @Then("^Message \"(.*)\" is null$")
    public void checkThatTheMessageIsActuallyNull(String msgKey) {

        assertNull(stepData.get(msgKey));
    }

    @Then("^The datastore message \"(.*)\" matches the prepared message \"(.*)\"$")
    public void checkThatTheStoredMessageMatchesTheOriginal(String datastoreMsgKey, String originalMsgKey) throws KapuaException {

        KapuaDataMessage origMsg = (KapuaDataMessage) stepData.get(originalMsgKey);
        DatastoreMessage foundMsg = (DatastoreMessage) stepData.get(datastoreMsgKey);

        checkThatTheInsertedMessageMatchesTheOriginal(origMsg, foundMsg);

        isChannelForFirstMessageInStoreOK(foundMsg.getDatastoreId(), foundMsg.getTimestamp());
        isClientForFirstMessageInStoreOK(foundMsg.getDatastoreId(), foundMsg.getTimestamp());
        if (origMsg.getPayload() != null) {
            isMetricForFirstMessageInStoreOK(foundMsg.getDatastoreId(), foundMsg.getTimestamp());
        }
    }

    @Then("^The datastore messages in list \"(.*)\" matches the prepared messages in list \"(.*)\"$")
    public void checkThatTheStoredMessagesMatchTheOriginals(String datastoreMsgLstKey, String originalMsgLstKey) throws KapuaException {

        List<KapuaDataMessage> origMsgs = (List<KapuaDataMessage>) stepData.get(originalMsgLstKey);
        List<DatastoreMessage> dataMsg = (List<DatastoreMessage>) stepData.get(datastoreMsgLstKey);

        for (int i = 0; i < origMsgs.size(); i++) {
            checkThatTheInsertedMessageMatchesTheOriginal(origMsgs.get(i), dataMsg.get(i));
        }
    }

    @Then("^The datastore messages \"(.*)\" and \"(.*)\" match$")
    public void checkSavedMessages(String firstMsg, String secondMsg) throws KapuaException {

        DatastoreMessage origMsg = (DatastoreMessage) stepData.get(firstMsg);
        DatastoreMessage foundMsg = (DatastoreMessage) stepData.get(secondMsg);

        checkThatDatastoreMessagesMatch(origMsg, foundMsg);

        isChannelForFirstMessageInStoreOK(foundMsg.getDatastoreId(), foundMsg.getTimestamp());
        isClientForFirstMessageInStoreOK(foundMsg.getDatastoreId(), foundMsg.getTimestamp());
        if ((origMsg.getPayload() != null) || (foundMsg.getPayload() != null)) {
            isMetricForFirstMessageInStoreOK(foundMsg.getDatastoreId(), foundMsg.getTimestamp());
        }
    }

    @When("^I search for channel info with id \"(.*)\"")
    public void channelInfoFind(String storableId) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfo channelInfo = channelInfoRegistryService.find(account.getId(), storableIdFactory.newStorableId(storableId));
        stepData.put("channelInfo", channelInfo);
    }

    @Then("^I don't find channel info$")
    public void dontFindChannelInfo() {

        ChannelInfo channelInfo = (ChannelInfo) stepData.get("channelInfo");
        assertNull(channelInfo);
    }

    @Then("^The channel info items \"(.+)\" match the prepared messages in \"(.+)\"$")
    public void checkThatChannelInfoMatchesTheMessageData(String channelKey, String msgKey) {

        ChannelInfoListResult tmpChn = (ChannelInfoListResult) stepData.get(channelKey);
        List<KapuaDataMessage> tmpMsgs = (List<KapuaDataMessage>) stepData.get(msgKey);

        checkChannelInfoAgainstPreparedMessages(tmpChn, tmpMsgs);
    }

    @When("^I search for metric info with id \"(.*)\"")
    public void metricInfoFind(String storableId) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfo tmpInfo = metricInfoRegistryService.find(account.getId(), storableIdFactory.newStorableId(storableId));
        stepData.put("metricInfo", tmpInfo);
    }

    @Then("^I don't find metric info$")
    public void dontFindMetricInfo() {

        MetricInfo metricInfo = (MetricInfo) stepData.get("metricInfo");
        assertNull(metricInfo);
    }

    @Then("^The metric info items \"(.+)\" match the prepared messages in \"(.+)\"$")
    public void checkThatMetricInfoMatchesTheMessageData(String metricKey, String msgKey) {

        MetricInfoListResult tmpMet = (MetricInfoListResult) stepData.get(metricKey);
        List<KapuaDataMessage> tmpMsgs = (List<KapuaDataMessage>) stepData.get(msgKey);

        checkMetricInfoAgainstPreparedMessages(tmpMet, tmpMsgs);
    }

    @When("^I search for client info with id \"(.*)\"")
    public void clientInfoFind(String storableId) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfo tmpInfo = clientInfoRegistryService.find(account.getId(), storableIdFactory.newStorableId(storableId));
        stepData.put("clientInfo", tmpInfo);
    }

    @Then("^I don't find client info$")
    public void dontFindClientInfo() {

        ClientInfo clientInfo = (ClientInfo) stepData.get("clientInfo");
        assertNull(clientInfo);
    }

    @Then("^The client info items \"(.+)\" match the prepared messages in \"(.+)\"$")
    public void checkThatClientInfoMatchesTheMessageData(String infoKey, String msgKey) {

        ClientInfoListResult tmpList = (ClientInfoListResult) stepData.get(infoKey);
        List<KapuaDataMessage> tmpMsgs = (List<KapuaDataMessage>) stepData.get(msgKey);

        checkClientInfoAgainstPreparedMessages(tmpList, tmpMsgs);
    }

    @Given("^I create message query for current account with limit (\\d+)$")
    public void createMessageQueryForAccount(int limit) {

        Account account = (Account) stepData.get("LastAccount");
        MessageQuery messageQuery = createBaseMessageQuery(account.getId(), limit);
        stepData.put("messageQuery", messageQuery);
    }

    @Given("^I create message query for current account from \"(.+)\" to \"(.+)\" with limit (\\d+)$")
    public void createMessageQueryForAccount(String fromDate, String toDate, Integer limit) throws Exception {

        Account account = (Account) stepData.get("LastAccount");

        try {
            Date startDate = KapuaDateUtils.parseDate(fromDate);
            Date endDate = KapuaDateUtils.parseDate(toDate);
            MessageQuery messageQuery = createBaseMessageQuery(account.getId(), limit);
            messageQuery.setPredicate(storablePredicateFactory.newRangePredicate(MessageField.TIMESTAMP.field(), startDate, endDate));

            stepData.put("messageQuery", messageQuery);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    @When("^I query for data message$")
    public void queryForDataMessage() throws KapuaException {

        MessageQuery messageQuery = (MessageQuery) stepData.get("messageQuery");
        MessageListResult result = messageStoreService.query(messageQuery);
        stepData.put("messageListResult", result);
    }

    @When("^I delete the messages based on the last query$")
    public void deleteMessagesByQuery() throws KapuaException {

        MessageQuery messageQuery = (MessageQuery) stepData.get("messageQuery");
        messageStoreService.delete(messageQuery);
    }

    @When("^I delete the the message with the ID \"(.+)\" from the current account$")
    public void deleteMessageWithId(String id) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        messageStoreService.delete(account.getId(), storableIdFactory.newStorableId(id));
    }

    @Then("^I get empty message list result$")
    public void getEmptyMessageListResult() {

        MessageListResult result = (MessageListResult) stepData.get("messageListResult");
        assertTrue(result.isEmpty());
    }

    @When("^I count for data messages?$")
    public void countForDataMessage() throws KapuaException {

        MessageQuery messageQuery = (MessageQuery) stepData.get("messageQuery");
        long count = messageStoreService.count(messageQuery);
        stepData.put("messageCountResult", count);
    }

    @Then("^I get message count (\\d+)$")
    public void getDesiredMessageCountResult(int desiredCount) throws AssertionError {

        try {
            primeException();
            long count = (long) stepData.get("messageCountResult");
            assertEquals("This two values are not equal", desiredCount, count);
        } catch (AssertionError assertError) {
            verifyAssertionError(assertError);
        }
    }

    @Given("^I create channel info query for current account with limit (\\d+)$")
    public void createChannelInofQueryForAccount(int limit) {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfoQuery channelInfoQuery = createBaseChannelInfoQuery(account.getId(), limit);
        stepData.put("channelInfoQuery", channelInfoQuery);
    }

    @Given("^I query for the channel info of the client \"(.+)\" and store the result as \"(.+)\"$")
    public void createChannelInofQueryForClientId(String clientId, String resKey) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        ChannelInfoQuery channelInfoQuery = createBaseChannelInfoQuery(account.getId(), 100);
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        TermPredicate clientPredicate = storablePredicateFactory.newTermPredicate(ChannelInfoField.CLIENT_ID, clientId);
        andPredicate.getPredicates().add(clientPredicate);
        channelInfoQuery.setPredicate(andPredicate);
        channelInfoQuery.addFetchAttributes(ChannelInfoField.TIMESTAMP.field());
        ChannelInfoListResult infoRes = channelInfoRegistryService.query(channelInfoQuery);

        stepData.put(resKey, infoRes);
    }

    @When("^I query for channel info$")
    public void queryForChannelInfo() throws KapuaException {

        ChannelInfoQuery channelInfoQuery = (ChannelInfoQuery) stepData.get("channelInfoQuery");
        channelInfoQuery.addFetchAttributes(ChannelInfoField.TIMESTAMP.field());
        ChannelInfoListResult result = channelInfoRegistryService.query(channelInfoQuery);
        stepData.put("channelInfoListResult", result);
    }

    @When("^I delete the channel info data based on the last query$")
    public void deleteChannelInfoByQuery() throws KapuaException {

        ChannelInfoQuery tmpQuery = (ChannelInfoQuery) stepData.get("channelInfoQuery");
        channelInfoRegistryServiceProxy.delete(tmpQuery);
    }

    @When("^I delete the the channel info data with the ID \"(.+)\" from the current account$")
    public void deleteChannelInfoWithId(String id) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        channelInfoRegistryServiceProxy.delete(account.getId(), storableIdFactory.newStorableId(id));
    }

    @Then("^I get empty channel info list result$")
    public void getEmptyChannelInfoListResult() {

        ChannelInfoListResult result = (ChannelInfoListResult) stepData.get("channelInfoListResult");
        assertTrue(result.isEmpty());
    }

    @When("^I count for channel info$")
    public void countForChannelInfo() throws KapuaException {

        ChannelInfoQuery channelInfoQuery = (ChannelInfoQuery) stepData.get("channelInfoQuery");
        long count = channelInfoRegistryService.count(channelInfoQuery);
        stepData.put("channelInfoCountResult", count);
    }

    @Then("^I get channel info count (\\d+)$")
    public void getDesiredChannelInfoCountResult(int desiredCount) {

        long count = (long) stepData.get("channelInfoCountResult");
        assertEquals(desiredCount, count);
    }

    @Given("^I create metric info query for current account with limit (\\d+)$")
    public void createMetricInfoQueryForAccount(int limit) {

        Account account = (Account) stepData.get("LastAccount");
        MetricInfoQuery metricInfoQuery = createBaseMetricInfoQuery(account.getId(), limit);
        stepData.put("metricInfoQuery", metricInfoQuery);
    }

    @When("^I query for metric info$")
    public void queryForMetricInfo() throws KapuaException {

        MetricInfoQuery metricInfoQuery = (MetricInfoQuery) stepData.get("metricInfoQuery");
        metricInfoQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());
        MetricInfoListResult result = metricInfoRegistryService.query(metricInfoQuery);
        stepData.put("metricInfoListResult", result);
    }

    @Then("^I get empty metric info list result$")
    public void getEmptyMetricInfoListResult() {

        MetricInfoListResult result = (MetricInfoListResult) stepData.get("metricInfoListResult");
        assertTrue(result.isEmpty());
    }

    @When("^I delete the metric info data based on the last query$")
    public void deleteMetricsInfoByQuery() throws KapuaException {

        MetricInfoQuery tmpQuery = (MetricInfoQuery) stepData.get("metricInfoQuery");
        metricInfoRegistryServiceProxy.delete(tmpQuery);
    }

    @When("^I delete the the metric info data with the ID \"(.+)\" from the current account$")
    public void deleteMetricsInfoWithId(String id) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        metricInfoRegistryServiceProxy.delete(account.getId(), storableIdFactory.newStorableId(id));
    }

    @When("^I count for metric info$")
    public void countForMetricInfo() throws KapuaException {

        MetricInfoQuery metricInfoQuery = (MetricInfoQuery) stepData.get("metricInfoQuery");
        long count = metricInfoRegistryService.count(metricInfoQuery);
        stepData.put("metricInfoCountResult", count);
    }

    @Then("^I get metric info count (\\d+)$")
    public void getDesiredMetricInfoCountResult(int desiredCount) {

        long count = (long) stepData.get("metricInfoCountResult");
        assertEquals(desiredCount, count);
    }

    @Given("^I create client info query for current account with limit (\\d+)$")
    public void createClientInfoQueryForAccount(int limit) {

        Account account = (Account) stepData.get("LastAccount");
        ClientInfoQuery clientInfoQuery = createBaseClientInfoQuery(account.getId(), limit);
        stepData.put("clientInfoQuery", clientInfoQuery);
    }

    @When("^I query for client info$")
    public void queryForClientInfo() throws KapuaException {

        ClientInfoQuery clientInfoQuery = (ClientInfoQuery) stepData.get("clientInfoQuery");
        clientInfoQuery.addFetchAttributes(ClientInfoField.TIMESTAMP.field());
        ClientInfoListResult result = clientInfoRegistryService.query(clientInfoQuery);
        stepData.put("clientInfoListResult", result);
    }

    @Then("^I get empty client info list result$")
    public void getEmptyClientInfoListResult() {

        ClientInfoListResult result = (ClientInfoListResult) stepData.get("clientInfoListResult");
        assertTrue(result.isEmpty());
    }

    @When("^I delete the client info data based on the last query$")
    public void deleteClientInfoByQuery() throws KapuaException {

        ClientInfoQuery tmpQuery = (ClientInfoQuery) stepData.get("clientInfoQuery");
        clientInfoRegistryServiceProxy.delete(tmpQuery);
    }

    @When("^I delete the the client info data with the ID \"(.+)\" from the current account$")
    public void deleteClientInfoWithId(String id) throws KapuaException {

        Account account = (Account) stepData.get("LastAccount");
        clientInfoRegistryServiceProxy.delete(account.getId(), storableIdFactory.newStorableId(id));
    }

    @When("^I count for client info$")
    public void countForClientInfo() throws KapuaException {

        ClientInfoQuery clientInfoQuery = (ClientInfoQuery) stepData.get("clientInfoQuery");
        long count = clientInfoRegistryService.count(clientInfoQuery);
        stepData.put("clientInfoCountResult", count);
    }

    @Then("^I get client info count (\\d+)$")
    public void getDesiredClientInfoCountResult(int desiredCount) {

        long count = (long) stepData.get("clientInfoCountResult");
        assertEquals(desiredCount, count);
    }

    @Then("^The value of \"(.*)\" is exactly (\\d+)$")
    public void checkValueOfIntegerItem(String valKey, long value) {
        assertEquals(value, (long) stepData.get(valKey));
    }

    @Then("^The messages in the list \"(.*)\" are stored in the default order$")
    public void checkListForDefaultMessageOrdering(String lstKey) {

        MessageListResult msgLst = (MessageListResult) stepData.get(lstKey);
        checkListOrder(msgLst, getDefaultListOrdering());
    }

    @Then("^The metrics in the list \"(.*)\" are ordered by name$")
    public void checkListForNamedMetricOrdering(String lstKey) {

        MetricInfoListResult metLst = (MetricInfoListResult) stepData.get(lstKey);
        checkListOrder(metLst, getNamedMetricOrdering());
    }

    @Given("^Dataservice config enabled (.*), dataTTL (\\d+), rxByteLimit (\\d+), dataIndexBy \"(.*)\"$")
    public void configureDatastoreService(String enabled, int dataTTL, int rxByteLimit, String dataIndexBy) throws Exception {
        Map<String, Object> settings = new HashMap<>();
        settings.put("enabled", enabled.equalsIgnoreCase("TRUE"));
        settings.put("dataTTL", dataTTL);
        settings.put("rxByteLimit", rxByteLimit);
        settings.put("dataIndexBy", dataIndexBy);
        messageStoreService.setConfigValues(KapuaId.ONE, null, settings);
    }

    @When("^I delete the indexes from \"(.+)\" to \"(.+)\"$")
    public void deleteIndexesBetweenDates(String fromDate, String toDate) throws Exception {

        primeException();
        try {
            String[] indexes = DatastoreUtils.convertToDataIndexes(getDataIndexesByAccount(getCurrentScopeId()), KapuaDateUtils.parseDate(fromDate).toInstant(),
                    KapuaDateUtils.parseDate(toDate).toInstant());
            datastoreClient.deleteIndexes(indexes);
        } catch (Exception ex) {
            verifyException(ex);
        }
    }

    // *******************
    // * Private Helpers *
    // *******************

    private static Map<String, Object> toData(List<MetricEntry> metrics) {
        final Map<String, Object> data = new HashMap<>();

        for (final MetricEntry entry : metrics) {

            final String key = entry.getKey();
            final String stringValue = entry.getValue();
            final String type = entry.getType();

            switch (type.toUpperCase()) {
                case "STRING":
                    data.put(key, stringValue);
                    break;
                case "INT32":
                    data.put(key, Integer.valueOf(stringValue));
                    break;
                case "INT64":
                    data.put(key, Long.valueOf(stringValue));
                    break;
                case "DOUBLE":
                    data.put(key, Double.parseDouble(stringValue));
                    break;
                case "BOOLEAN":
                    data.put(key, Boolean.valueOf(stringValue));
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown type: %s", type));
            }
        }
        return data;
    }

    private SimulatedDeviceApplication getMockApplication(final String applicationId) {
        final SimulatedDeviceApplication app = currentDevice.getMockApplications().get(applicationId);
        if (app == null) {
            throw new IllegalStateException(String.format("Application '%s' not found in current setup", applicationId));
        }
        return app;
    }

    private KapuaDataPayload createRandomTestPayload() throws Exception {

        KapuaDataPayload tmpTestPayload = dataMessageFactory.newKapuaDataPayload();

        byte[] randomPayload = new byte[128];
        random.nextBytes(randomPayload);
        String stringPayload = "Hello first message on!";
        byte[] tmpByteArrayPayload = ArrayUtils.addAll(randomPayload, stringPayload.getBytes());
        tmpTestPayload.setBody(tmpByteArrayPayload);

        DateFormat dfBr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dfBr.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        Date dateBr = dfBr.parse("01/04/2017 03:00:00");
        DateFormat dfLA = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dfLA.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date dateLA = dfLA.parse("01/04/2017 03:00:00");
        DateFormat dfHK = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dfHK.setTimeZone(TimeZone.getTimeZone("Asia/Hong_Kong"));
        Date dateHK = dfHK.parse("01/04/2017 03:00:00");
        DateFormat dfSyd = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dfSyd.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        Date dateSyd = dfSyd.parse("01/04/2017 03:00:00");

        Map<String, Object> tmpMetrics = new HashMap<>();
        tmpMetrics.put("float", random.nextFloat() * 100);
        tmpMetrics.put("float_int", Float.valueOf(random.nextInt()));
        tmpMetrics.put("double", random.nextDouble() * 100);
        tmpMetrics.put("double_int", Float.valueOf(random.nextInt()));
        tmpMetrics.put("integer", random.nextInt());
        tmpMetrics.put("long", random.nextLong());
        tmpMetrics.put("long_int", (long) random.nextInt());
        tmpMetrics.put("string_value", Integer.toString(random.nextInt()));
        tmpMetrics.put("date_value_brussels", dateBr);
        tmpMetrics.put("date_value_la", dateLA);
        tmpMetrics.put("date_value_hk", dateHK);
        tmpMetrics.put("date_value_sydney", dateSyd);
        tmpTestPayload.setMetrics(tmpMetrics);

        return tmpTestPayload;
    }

    private KapuaPosition createRandomTestPosition(Date timeStamp) {

        KapuaPosition tmpPosition = messageFactory.newPosition();
        tmpPosition.setAltitude(20000.0 * random.nextDouble()); // 0 .. 20000
        tmpPosition.setHeading(360.0 * random.nextDouble()); // 0 .. 360
        tmpPosition.setLatitude(180.0 * (random.nextDouble() - 0.5)); // -90 .. 90
        tmpPosition.setLongitude(360.0 * (random.nextDouble() - 0.5)); // -180 .. 180
        tmpPosition.setPrecision(100.0 * random.nextDouble()); // 0 .. 100
        tmpPosition.setSatellites(random.nextInt(30)); // 0 .. 30
        tmpPosition.setSpeed(500.0 * random.nextDouble()); // 0 .. 500
        tmpPosition.setStatus(random.nextInt(10)); // 0 .. 10
        tmpPosition.setTimestamp(timeStamp);

        return tmpPosition;
    }

    private KapuaDataMessage createTestMessage(KapuaId scopeId, KapuaId deviceId, String clientId, String topic, String captured)
            throws Exception {

        String tmpTopic = ((topic != null) && !topic.isEmpty()) ? topic : "default/test/topic";
        String tmpClientId = (clientId != null) ? clientId : ((Device) stepData.get("LastDevice")).getClientId();
        KapuaDataMessage tmpMessage = dataMessageFactory.newKapuaDataMessage();

        Date tmpRecDate = new Date();
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.add(Calendar.MINUTE, -10);
        Date tmpSentDate = tmpCal.getTime();

        Date tmpCaptured = tmpRecDate;
        if ((captured != null) && !captured.isEmpty()) {
            tmpCaptured = KapuaDateUtils.parseDate(captured);
        }

        tmpMessage.setReceivedOn(tmpRecDate);
        tmpMessage.setSentOn(tmpSentDate);
        tmpMessage.setCapturedOn(tmpCaptured);
        tmpMessage.setClientId(tmpClientId);
        tmpMessage.setDeviceId(deviceId);
        tmpMessage.setScopeId(scopeId);

        KapuaDataChannel tmpChannel = dataMessageFactory.newKapuaDataChannel();
        tmpChannel.setSemanticParts(new ArrayList<>(Arrays.asList(tmpTopic.split("/"))));
        tmpMessage.setChannel(tmpChannel);

        tmpMessage.setPayload(createRandomTestPayload());
        tmpMessage.setPosition(createRandomTestPosition(tmpSentDate));

        return tmpMessage;
    }

    private KapuaDataMessage createTestMessage(KapuaId scopeId, KapuaId deviceId, String clientId, String topic, Date captured)
            throws Exception {

        String tmpTopic = (topic != null) ? topic : "default/test/topic";
        String tmpClientId = (clientId != null) ? clientId : ((Device) stepData.get("LastDevice")).getClientId();
        KapuaDataMessage tmpMessage = dataMessageFactory.newKapuaDataMessage();

        Date tmpCaptured = (captured != null) ? captured : new Date();
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.setTime(tmpCaptured);
        tmpCal.add(Calendar.MINUTE, -10);
        Date tmpSentDate = tmpCal.getTime();

        tmpMessage.setReceivedOn(tmpCaptured);
        tmpMessage.setSentOn(tmpSentDate);
        tmpMessage.setCapturedOn(tmpCaptured);
        tmpMessage.setClientId(tmpClientId);
        tmpMessage.setDeviceId(deviceId);
        tmpMessage.setScopeId(scopeId);

        KapuaDataChannel tmpChannel = dataMessageFactory.newKapuaDataChannel();
        tmpChannel.setSemanticParts(new ArrayList<>(Arrays.asList(tmpTopic.split("/"))));
        tmpMessage.setChannel(tmpChannel);

        tmpMessage.setPayload(createRandomTestPayload());
        tmpMessage.setPosition(createRandomTestPosition(tmpSentDate));

        return tmpMessage;
    }

    private StorableId insertMessageInStore(KapuaDataMessage message) throws KapuaException {
        return messageStoreService.store(message);
    }

    private List<StorableId> insertMessagesInStore(List<KapuaDataMessage> messages) throws KapuaException {

        StorableId tmpId = null;
        List<StorableId> tmpList = new ArrayList<>();

        for (KapuaDataMessage tmpMsg : messages) {
            tmpId = insertMessageInStore(tmpMsg);
            tmpList.add(tmpId);
        }

        return tmpList;
    }

    private boolean areSemanticPartsEqual(List<String> parts1, List<String> parts2) {

        if ((parts1 == null) && (parts2 == null)) {
            return true;
        }
        if ((parts1 != null) && (parts2 != null)) {
            assertEquals(parts1.size(), parts2.size());
            for (int i = 0; i < parts1.size(); i++) {
                assertEquals(parts1.get(i), parts2.get(i));
            }
        }
        return true;
    }

    private boolean areMetricsEqual(Map<String, Object> properties1, Map<String, Object> properties2) {

        assertEquals(properties1.size(), properties2.size());

        if (properties1.size() > 0) {
            for (String key : properties1.keySet()) {
                assertTrue(properties2.containsKey(key));
                assertEquals(properties1.get(key), properties2.get(key));
            }
        }
        return true;
    }

    private void isChannelForFirstMessageInStoreOK(StorableId msgId, Date storedOn) throws KapuaException {

        KapuaId tmpAccId = ((Account) stepData.get("LastAccount")).getId();
        String tmpClId = ((Device) stepData.get("LastDevice")).getClientId();
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(storablePredicateFactory.newTermPredicate(ChannelInfoField.CLIENT_ID, tmpClId));
        ChannelInfoQuery channelInfoQuery = createBaseChannelInfoQuery(tmpAccId, 100);
        channelInfoQuery.setPredicate(andPredicate);
        channelInfoQuery.addFetchAttributes(ChannelInfoField.TIMESTAMP.field());
        ChannelInfoListResult channelInfoList = channelInfoRegistryService.query(channelInfoQuery);

        assertNotNull("Cannot find the channel info registry!", channelInfoList);
        assertNotEquals("Cannot find the channel info registry!", channelInfoList.getSize(), 0);
        assertNotNull("Cannot find the channel info registry!", channelInfoList.getFirstItem());
        assertEquals("Wrong channel info message id!", channelInfoList.getFirstItem().getFirstMessageId(), msgId);
        assertEquals("Wrong channel info message on!", channelInfoList.getFirstItem().getFirstMessageOn(), storedOn);
    }

    private void isClientForFirstMessageInStoreOK(StorableId msgId, Date storedOn) throws KapuaException {

        KapuaId tmpAccId = ((Account) stepData.get("LastAccount")).getId();
        String tmpClId = ((Device) stepData.get("LastDevice")).getClientId();
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(storablePredicateFactory.newTermPredicate(ClientInfoField.CLIENT_ID, tmpClId));
        ClientInfoQuery clientInfoQuery = createBaseClientInfoQuery(tmpAccId, 100);
        clientInfoQuery.setPredicate(andPredicate);
        clientInfoQuery.addFetchAttributes(ClientInfoField.TIMESTAMP.field());

        ClientInfoListResult clientInfoList = clientInfoRegistryService.query(clientInfoQuery);

        assertNotNull("Cannot find the client info registry!", clientInfoList);
        assertNotEquals("Cannot find the client info registry!", clientInfoList.getSize(), 0);
        assertNotNull("Cannot find the client info registry!", clientInfoList.getFirstItem());
        assertEquals("Wrong client info message id!", clientInfoList.getFirstItem().getFirstMessageId(), msgId);
        assertEquals("Wrong client info message on!", clientInfoList.getFirstItem().getFirstMessageOn(), storedOn);
    }

    private void isMetricForFirstMessageInStoreOK(StorableId msgId, Date storedOn) throws KapuaException {

        KapuaId tmpAccId = ((Account) stepData.get("LastAccount")).getId();
        String tmpClId = ((Device) stepData.get("LastDevice")).getClientId();
        AndPredicate andPredicate = storablePredicateFactory.newAndPredicate();
        andPredicate.getPredicates().add(storablePredicateFactory.newTermPredicate(MetricInfoField.CLIENT_ID, tmpClId));
        MetricInfoQuery metricInfoQuery = createBaseMetricInfoQuery(tmpAccId, 100);
        metricInfoQuery.setPredicate(andPredicate);
        metricInfoQuery.addFetchAttributes(MetricInfoField.TIMESTAMP_FULL.field());

        MetricInfoListResult metricInfoList = metricInfoRegistryService.query(metricInfoQuery);

        assertNotNull("Cannot find the metric info registry!", metricInfoList);
        assertNotEquals("Cannot find the metric info registry!", metricInfoList.getSize(), 0);
        assertNotNull("Cannot find the metric info registry!", metricInfoList.getFirstItem());
        assertEquals("Wrong metric info message id!", metricInfoList.getFirstItem().getFirstMessageId(), msgId);
        assertEquals("Wrong metric info message on!", metricInfoList.getFirstItem().getFirstMessageOn(), storedOn);
    }

    private boolean arePositionsEqual(KapuaPosition position1, KapuaPosition position2) {

        assertEquals(position1.getAltitude(), position2.getAltitude());
        assertEquals(position1.getHeading(), position2.getHeading());
        assertEquals(position1.getLatitude(), position2.getLatitude());
        assertEquals(position1.getLongitude(), position2.getLongitude());
        assertEquals(position1.getPrecision(), position2.getPrecision());
        assertEquals(position1.getSatellites(), position2.getSatellites());
        assertEquals(position1.getSpeed(), position2.getSpeed());
        assertEquals(position1.getStatus(), position2.getStatus());
        assertEquals(position1.getTimestamp(), position2.getTimestamp());
        return true;
    }

    // create a basic message query with default parameters. Also set the requested ordering.
    private MessageQuery getBaseMessageQuery(KapuaId scopeId, int limit, List<OrderConstraint<?>> order) {

        MessageQuery tmpQuery = createBaseMessageQuery(scopeId, limit,
                order.stream().map(OrderConstraint::getField).collect(Collectors.toList()));

        return tmpQuery;
    }

    // create a basic metric query with default parameters. Also set the requested ordering.
    private MetricInfoQuery getBaseMetricQuery(KapuaId scopeId, int limit, List<OrderConstraint<?>> order) {

        MetricInfoQuery tmpQuery = createBaseMetricInfoQuery(scopeId, limit);
        tmpQuery.setSortFields(order.stream().map(OrderConstraint::getField).collect(Collectors.toList()));

        return tmpQuery;
    }

    // Check whether the message that was inserted into the message store matches the originally prepared message
    private boolean checkThatTheInsertedMessageMatchesTheOriginal(KapuaDataMessage origMsg, DatastoreMessage foundMsg) throws KapuaException {

        assertTrue(areSemanticPartsEqual(origMsg.getChannel().getSemanticParts(), foundMsg.getChannel().getSemanticParts()));
        if (origMsg.getPayload() != null) {
            assertArrayEquals(origMsg.getPayload().getBody(), foundMsg.getPayload().getBody());
            assertTrue(areMetricsEqual(origMsg.getPayload().getMetrics(), foundMsg.getPayload().getMetrics()));
        }
        assertTrue(arePositionsEqual(origMsg.getPosition(), foundMsg.getPosition()));
        assertTrue(foundMsg.getTimestamp().compareTo(origMsg.getReceivedOn()) >= 0);
        assertTrue(foundMsg.getTimestamp().compareTo(new Date(origMsg.getReceivedOn().getTime() + 10000)) <= 0);
        assertEquals(origMsg.getCapturedOn(), foundMsg.getCapturedOn());
        assertEquals(origMsg.getSentOn(), foundMsg.getSentOn());
        assertEquals(origMsg.getReceivedOn(), foundMsg.getReceivedOn());

        return true;
    }

    // Check that two datastore messages match
    private boolean checkThatDatastoreMessagesMatch(DatastoreMessage firstMsg, DatastoreMessage secondMsg) throws KapuaException {

        assertEquals(firstMsg.getDatastoreId().toString(), secondMsg.getDatastoreId().toString());
        assertTrue(areSemanticPartsEqual(firstMsg.getChannel().getSemanticParts(), secondMsg.getChannel().getSemanticParts()));
        if ((firstMsg.getPayload() != null) || (secondMsg.getPayload() != null)) {
            assertArrayEquals(firstMsg.getPayload().getBody(), secondMsg.getPayload().getBody());
            assertTrue(areMetricsEqual(firstMsg.getPayload().getMetrics(), secondMsg.getPayload().getMetrics()));
        }
        assertTrue(arePositionsEqual(firstMsg.getPosition(), secondMsg.getPosition()));
        assertEquals(firstMsg.getTimestamp(), secondMsg.getTimestamp());
        assertEquals(firstMsg.getCapturedOn(), secondMsg.getCapturedOn());
        assertEquals(firstMsg.getSentOn(), secondMsg.getSentOn());
        assertEquals(firstMsg.getReceivedOn(), secondMsg.getReceivedOn());

        return true;
    }

    // Check whether the supplied channel info list contains all the client ids and topic names that were
    // defined by the messages
    private void checkChannelInfoAgainstPreparedMessages(ChannelInfoListResult chnInfo, List<KapuaDataMessage> msgLst) {

        Set<String> msgTopics = new HashSet<>();
        Set<String> msgClients = new HashSet<>();
        Set<String> infoTopics = new HashSet<>();
        Set<String> infoClients = new HashSet<>();

        assertNotNull("No channel info data!", chnInfo);
        assertNotNull("No messages to compare to!", msgLst);

        for (KapuaDataMessage tmpMsg : msgLst) {
            msgClients.add(tmpMsg.getClientId());
            msgTopics.add(tmpMsg.getChannel().toString());
        }
        for (ChannelInfo tmpInfo : chnInfo.getItems()) {
            infoClients.add(tmpInfo.getClientId());
            infoTopics.add(tmpInfo.getName());
        }

        assertEquals("The number of clients does not match!", msgClients.size(), infoClients.size());
        assertEquals("The number of topics does not match!", msgTopics.size(), infoTopics.size());

        for (String tmpTopic : msgTopics) {
            assertTrue(String.format("The topic [%s] is not found in the info list!", tmpTopic), infoTopics.contains(tmpTopic));
        }

        for (String tmpClient : msgClients) {
            assertTrue(String.format("The client id [%s] is not found in the info list!", tmpClient), infoClients.contains(tmpClient));
        }
    }

    // Check whether the supplied metric info list contains all the client ids and metric names that were
    // defined by the messages
    private void checkMetricInfoAgainstPreparedMessages(MetricInfoListResult metInfo, List<KapuaDataMessage> msgLst) {

        Set<String> msgMetrics = new HashSet<>();
        Set<String> msgClients = new HashSet<>();
        Set<String> infoMetrics = new HashSet<>();
        Set<String> infoClients = new HashSet<>();

        assertNotNull("No channel info data!", metInfo);
        assertNotNull("No messages to compare to!", msgLst);

        for (KapuaDataMessage tmpMsg : msgLst) {
            msgClients.add(tmpMsg.getClientId());
            for (String tmpMet : tmpMsg.getPayload().getMetrics().keySet()) {
                msgMetrics.add(tmpMet);
            }
        }
        for (MetricInfo tmpMet : metInfo.getItems()) {
            infoClients.add(tmpMet.getClientId());
            infoMetrics.add(tmpMet.getName());
        }

        assertEquals("The number of clients does not match!", msgClients.size(), infoClients.size());
        assertEquals("The number of topics does not match!", msgMetrics.size(), infoMetrics.size());

        for (String tmpMetric : msgMetrics) {
            assertTrue(String.format("The topic [%s] is not found in the info list!", tmpMetric), infoMetrics.contains(tmpMetric));
        }

        for (String tmpClient : msgClients) {
            assertTrue(String.format("The client id [%s] is not found in the info list!", tmpClient), infoClients.contains(tmpClient));
        }
    }

    // Check whether the supplied metric info list contains all the client ids and metric names that were
    // defined by the messages
    private void checkClientInfoAgainstPreparedMessages(ClientInfoListResult cliInfo, List<KapuaDataMessage> msgLst) {

        Set<String> msgClients = new HashSet<>();
        Set<String> infoClients = new HashSet<>();

        assertNotNull("No client info data!", cliInfo);
        assertNotNull("No messages to compare to!", msgLst);

        for (KapuaDataMessage tmpMsg : msgLst) {
            msgClients.add(tmpMsg.getClientId());
        }
        for (ClientInfo tmpClient : cliInfo.getItems()) {
            infoClients.add(tmpClient.getClientId());
        }

        assertEquals("The number of clients does not match!", msgClients.size(), infoClients.size());

        for (String tmpClient : msgClients) {
            assertTrue(String.format("The client id [%s] is not found in the info list!", tmpClient), infoClients.contains(tmpClient));
        }
    }

    private List<OrderConstraint<?>> getDefaultListOrdering() {

        List<OrderConstraint<?>> sort = new ArrayList<>();

        sort.add(orderConstraint(SortField.descending(MessageSchema.MESSAGE_SENT_ON), Date.class));
        sort.add(orderConstraint(SortField.ascending(MessageSchema.MESSAGE_TIMESTAMP), Date.class));
        sort.add(orderConstraint(SortField.descending(MessageSchema.MESSAGE_CLIENT_ID), String.class));

        List<SortField> order = new ArrayList<>();
        order.add(SortField.descending(MessageSchema.MESSAGE_TIMESTAMP));

        return sort;
    }

    private List<OrderConstraint<?>> getNamedMetricOrdering() {

        List<OrderConstraint<?>> sort = new ArrayList<>();

        sort.add(orderConstraint(SortField.ascending(MetricInfoSchema.METRIC_MTR_NAME_FULL), String.class));

        return sort;
    }

    private static class OrderConstraint<T extends Comparable<T>> {

        private final SortField field;
        private final Function<Object, T> valueFunction;

        public OrderConstraint(final SortField field, final Function<Object, T> valueFunction) {
            this.field = field;
            this.valueFunction = valueFunction;
        }

        public SortField getField() {
            return field;
        }

        public boolean validate(final Object previousItem, final Object currentItem) {
            final T v1 = valueFunction.apply(previousItem);
            final T v2 = valueFunction.apply(currentItem);

            if (!v2.equals(v1)) {
                checkNextValueCoherence(field, v2, v1);
                return true;
            }
            return false;
        }
    }

    private static <T extends Comparable<T>> OrderConstraint<T> orderConstraint(SortField field, Class<T> clazz) {

        return new OrderConstraint<>(field, fieldFunction(field, clazz));
    }

    private static <T extends Comparable<T>> Function<Object, T> fieldFunction(SortField field, Class<T> clazz) {

        return item -> getValue(item, field.getField(), clazz);
    }

    /**
     * Check if the message result list is correctly ordered by the provided criteria (list of fields and ordering)
     *
     * @param result
     * @param sortFieldList
     */
    private static void checkListOrder(StorableListResult<?> result, List<OrderConstraint<?>> sortFieldList) {

        Object previousItem = null;
        for (Object item : result.getItems()) {
            for (OrderConstraint<?> field : sortFieldList) {
                if (previousItem != null) {
                    if (field.validate(previousItem, item)) {
                        break;
                    }
                } else {
                    break;
                }
            }
            previousItem = item;
        }
    }

    /**
     * Check if the next value (it must be not equals, so the equals condition must be checked before calling this method) is coherent with its ordering criteria
     *
     * @param field
     * @param currentValue
     * @param previousValue
     */
    private static <T extends Comparable<T>> void checkNextValueCoherence(final SortField field, final T currentValue, final T previousValue) {

        if (SortDirection.ASC.equals(field.getSortDirection())) {
            assertTrue(String.format("The field [%s] is not correctly ordered as [%s] (%s -> %s)!", field.getField(), field.getSortDirection(), currentValue, previousValue),
                    currentValue.compareTo(previousValue) > 0);
        } else {
            assertTrue(String.format("The field [%s] is not correctly ordered as [%s] (%s -> %s)!", field.getField(), field.getSortDirection(), currentValue, previousValue),
                    currentValue.compareTo(previousValue) < 0);
        }
    }

    /**
     * Return the value of the field name provided (assuming that this value is a Comparable)
     *
     * @param field
     * @return
     */
    private static <T> T getValue(Object object, String field, Class<T> clazz) {

        try {
            Class<?> objetcClass = object.getClass();
            String getterFieldName = getFieldName(field, true);
            Method getMethod = getMethod(objetcClass, getterFieldName, "get");
            if (getMethod != null) {
                return clazz.cast(getMethod.invoke(object, new Object[0]));
            }
            getMethod = getMethod(objetcClass, getterFieldName, "is");
            if (getMethod != null) {
                return clazz.cast(getMethod.invoke(object, new Object[0]));
            }
            // else try by field access
            String fieldName = getFieldName(field, false);
            Field objField = getField(objetcClass, fieldName);
            if (objField != null) {
                return clazz.cast(objField.get(object));
            } else {
                throw new IllegalArgumentException(String.format("Cannot find getter for field [%s] or field [%s] or the field value is not a Comparable value!", field, field));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Cannot find getter for field [%s] or field [%s] or the field value is not a Comparable value!", field, field));
        }
    }

    /**
     * Return the suffix field name to compose (in a different method) the getter name.
     * It removes the _ and append the remaining part capitalizing the first letter (if capitalizeFirstLetter = true)
     *
     * @param field
     * @return
     */
    private static String getFieldName(String field, boolean capitalizeFirstLetter) {

        String str[] = cleanupFieldName(field);

        StringBuilder fieldNameSb = new StringBuilder();
        if (capitalizeFirstLetter) {
            fieldNameSb.append(str[0].substring(0, 1).toUpperCase()).append(str[0].substring(1));
        } else {
            fieldNameSb.append(str[0]);
        }

        for (int i = 1; i < str.length; i++) {
            fieldNameSb.append(str[i].substring(0, 1).toUpperCase()).append(str[i].substring(1));
        }

        return fieldNameSb.toString();
    }

    private static String[] cleanupFieldName(String field) {

        int lastDot = field.lastIndexOf('.');
        if (lastDot > -1) {
            field = field.substring(lastDot + 1, field.length());
        }

        String str[] = field.split("_");
        if (str.length <= 0) {
            throw new IllegalArgumentException(String.format("Invalid field name [%s]", field));
        }
        return str;
    }

    /**
     * Return the method combining the prefix and the field name provided
     *
     * @param objetcClass
     * @param field
     * @param prefix
     * @return
     */
    private static Method getMethod(Class<?> objetcClass, String field, String prefix) {

        String fieldName = prefix + field.substring(0, 1).toUpperCase() + field.substring(1);

        Method objMethod = null;
        do {
            try {
                objMethod = objetcClass.getMethod(fieldName, new Class[0]);
            } catch (NoSuchMethodException e) {
                objetcClass = objetcClass.getSuperclass();
            }
        } while (objMethod == null && objetcClass != null);

        return objMethod;
    }

    /**
     * Return the field combining the prefix and the field name provided
     *
     * @param objectClass
     * @param field
     * @return
     */
    private static Field getField(Class<?> objectClass, final String field) {

        Field objField = null;
        do {
            try {
                objField = objectClass.getDeclaredField(field);
                objField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                objectClass = objectClass.getSuperclass();
            }
        } while (objField == null && objectClass != null);

        return objField;
    }

    /**
     * Update the store service configuration with the provided values
     *
     * @param messageStoreService
     * @param scopeId
     * @param dataIndexBy
     * @param metricsIndexBy
     * @param dataTTL
     * @param storageEnabled
     * @throws KapuaException
     */
    private void updateConfiguration(MessageStoreService messageStoreService, KapuaId scopeId, KapuaId parentId, DataIndexBy dataIndexBy, MetricsIndexBy metricsIndexBy, int dataTTL, boolean storageEnabled)
            throws KapuaException {

        Map<String, Object> config = messageStoreService.getConfigValues(scopeId);
        if (config == null) {
            config = new HashMap<>();
        }
        if (dataIndexBy != null) {
            config.put(MessageStoreConfiguration.CONFIGURATION_DATA_INDEX_BY_KEY, dataIndexBy.name());
        }
        if (metricsIndexBy != null) {
            config.put(MessageStoreConfiguration.CONFIGURATION_METRICS_INDEX_BY_KEY, metricsIndexBy.name());
        }
        config.put(MessageStoreConfiguration.CONFIGURATION_DATA_TTL_KEY, dataTTL);
        config.put(MessageStoreConfiguration.CONFIGURATION_DATA_STORAGE_ENABLED_KEY, storageEnabled);
        messageStoreService.setConfigValues(scopeId, parentId, config);
    }

    private String[] getDataIndexesByAccount(KapuaId scopeId) throws ClientException {
        return datastoreClient.findIndexes(new IndexRequest(scopeId.toStringId() + "-*")).getIndexes();
    }

    private void setDatastoreIndexingWindowOption(String windowOption) {
        System.setProperty(DatastoreSettingKey.INDEXING_WINDOW_OPTION.key(), windowOption.trim().toLowerCase());
    }

    /**
     * Creating query for data messages with reasonable defaults.
     *
     * @param scopeId
     *            scope
     * @param limit
     *            limit results
     * @return query
     */
    public MessageQuery createBaseMessageQuery(KapuaId scopeId, int limit) {

        MessageQuery query = new MessageQueryImpl(scopeId);
        query.setAskTotalCount(true);
        query.setFetchStyle(StorableFetchStyle.SOURCE_FULL);
        query.setLimit(limit);
        query.setOffset(0);
        List<SortField> order = new ArrayList<>();
        order.add(SortField.descending(MessageSchema.MESSAGE_TIMESTAMP));
        query.setSortFields(order);

        return query;
    }

    /**
     * Creating query for data messages with reasonable defaults and user specified ordering.
     *
     * @param scopeId scope
     * @param limit limit results
     * @param order the required result ordering
     * @return query
     */
    public MessageQuery createBaseMessageQuery(KapuaId scopeId, int limit, List<SortField> order) {

        MessageQuery query = new MessageQueryImpl(scopeId);
        query.setAskTotalCount(true);
        query.setFetchStyle(StorableFetchStyle.SOURCE_FULL);
        query.setLimit(limit);
        query.setOffset(0);
        query.setSortFields(order);

        return query;
    }

    /**
     * Creating query for channel info with reasonable defaults.
     *
     * @param scopeId
     *            scope
     * @param limit
     *            limit results
     * @return query
     */
    public ChannelInfoQuery createBaseChannelInfoQuery(KapuaId scopeId, int limit) {

        ChannelInfoQuery query = datastoreObjectFactory.newChannelInfoQuery(scopeId);
        query.setAskTotalCount(true);
        query.setFetchStyle(StorableFetchStyle.SOURCE_FULL);
        query.setLimit(limit);
        query.setOffset(0);
        List<SortField> order = new ArrayList<>();
        order.add(SortField.descending(ChannelInfoSchema.CHANNEL_TIMESTAMP));
        query.setSortFields(order);

        return query;
    }

    /**
     * Creating query for metric info with reasonable defaults.
     *
     * @param scopeId
     *            scope
     * @param limit
     *            limit results
     * @return query
     */
    public MetricInfoQuery createBaseMetricInfoQuery(KapuaId scopeId, int limit) {

        MetricInfoQuery query = datastoreObjectFactory.newMetricInfoQuery(scopeId);
        query.setAskTotalCount(true);
        query.setFetchStyle(StorableFetchStyle.SOURCE_FULL);
        query.setLimit(limit);
        query.setOffset(0);
        List<SortField> order = new ArrayList<>();
        order.add(SortField.descending(MetricInfoSchema.METRIC_MTR_TIMESTAMP));
        query.setSortFields(order);

        return query;
    }

    /**
     * Creating query for client info with reasonable defaults.
     *
     * @param scopeId
     *            scope
     * @param limit
     *            limit results
     * @return query
     */
    public ClientInfoQuery createBaseClientInfoQuery(KapuaId scopeId, int limit) {

        ClientInfoQuery query = datastoreObjectFactory.newClientInfoQuery(scopeId);
        query.setAskTotalCount(true);
        query.setFetchStyle(StorableFetchStyle.SOURCE_FULL);
        query.setLimit(limit);
        query.setOffset(0);
        List<SortField> order = new ArrayList<>();
        order.add(SortField.descending(ClientInfoSchema.CLIENT_TIMESTAMP));
        query.setSortFields(order);

        return query;
    }
}
