/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.internal.mediator;

import com.google.common.hash.Hashing;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.MessageStoreService;
import org.eclipse.kapua.service.datastore.internal.setting.DatastoreSettingKey;
import org.eclipse.kapua.service.datastore.internal.setting.DatastoreSettings;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Datastore utility class
 *
 * @since 1.0.0
 */
public class DatastoreUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatastoreUtils.class);
    private static final MessageStoreService MESSAGE_STORE_SERVICE = KapuaLocator.getInstance().getService(MessageStoreService.class);

    private DatastoreUtils() {
    }

    private static final char SPECIAL_DOT = '.';
    private static final String SPECIAL_DOT_ESC = "$2e";

    private static final char SPECIAL_DOLLAR = '$';
    private static final String SPECIAL_DOLLAR_ESC = "$24";

    public static final CharSequence ILLEGAL_CHARS = "\"\\/*?<>|,. ";

    public static final String CLIENT_METRIC_TYPE_STRING = "string";
    public static final String CLIENT_METRIC_TYPE_INTEGER = "integer";
    public static final String CLIENT_METRIC_TYPE_LONG = "long";
    public static final String CLIENT_METRIC_TYPE_FLOAT = "float";
    public static final String CLIENT_METRIC_TYPE_DOUBLE = "double";
    public static final String CLIENT_METRIC_TYPE_DATE = "date";
    public static final String CLIENT_METRIC_TYPE_BOOLEAN = "boolean";
    public static final String CLIENT_METRIC_TYPE_BINARY = "binary";

    public static final String CLIENT_METRIC_TYPE_STRING_ACRONYM = "str";
    public static final String CLIENT_METRIC_TYPE_INTEGER_ACRONYM = "int";
    public static final String CLIENT_METRIC_TYPE_LONG_ACRONYM = "lng";
    public static final String CLIENT_METRIC_TYPE_FLOAT_ACRONYM = "flt";
    public static final String CLIENT_METRIC_TYPE_DOUBLE_ACRONYM = "dbl";
    public static final String CLIENT_METRIC_TYPE_DATE_ACRONYM = "dte";
    public static final String CLIENT_METRIC_TYPE_BOOLEAN_ACRONYM = "bln";
    public static final String CLIENT_METRIC_TYPE_BINARY_ACRONYM = "bin";

    public static final String INDEXING_WINDOW_OPTION = "indexingWindow";
    public static final String INDEXING_WINDOW_OPTION_WEEK = "WEEK";
    public static final String INDEXING_WINDOW_OPTION_DAY = "DAY";
    public static final String INDEXING_WINDOW_OPTION_HOUR = "HOUR";

    private static final DateTimeFormatter DATA_INDEX_FORMATTER_WEEK = DateTimeFormatter
            .ofPattern("YYYY-ww")
            .withLocale(KapuaDateUtils.getLocale())
            .withResolverStyle(ResolverStyle.STRICT)
            .withZone(KapuaDateUtils.getTimeZone());
    private static final DateTimeFormatter DATA_INDEX_FORMATTER_DAY = DateTimeFormatter
            .ofPattern("YYYY-ww-dd")
            .withLocale(KapuaDateUtils.getLocale())
            .withResolverStyle(ResolverStyle.STRICT)
            .withZone(KapuaDateUtils.getTimeZone());
    private static final DateTimeFormatter DATA_INDEX_FORMATTER_HOUR = DateTimeFormatter
            .ofPattern("YYYY-ww-dd-HH")
            .withLocale(KapuaDateUtils.getLocale())
            .withResolverStyle(ResolverStyle.STRICT)
            .withZone(KapuaDateUtils.getTimeZone());
    private static final TemporalField WEEK_OF_MONTH = WeekFields.of(KapuaDateUtils.getLocale()).weekOfMonth();
    private static final TemporalField WEEK_OF_YEAR = WeekFields.of(KapuaDateUtils.getLocale()).weekOfYear();

    /**
     * Return the hash code for the provided components (typically components are a sequence of account - client id - channel ...)
     *
     * @param components
     * @return
     */
    public static String getHashCode(String... components) {
        String concatString = "";
        for (String str : components) {
            concatString = concatString.concat(str);
        }

        byte[] hashCode = Hashing.sha256()
                .hashString(concatString, StandardCharsets.UTF_8)
                .asBytes();

        // ES 5.2 FIX
        // return Base64.encodeBytes(hashCode);
        return Base64.getEncoder().encodeToString(hashCode);
    }

    private static String normalizeIndexName(String name) {
        String normName = null;
        try {
            DatastoreUtils.checkIdxAliasName(name);
            normName = name;
        } catch (IllegalArgumentException exc) {
            LOG.trace(exc.getMessage(), exc);
            normName = name.toLowerCase().replace(ILLEGAL_CHARS, "_");
            DatastoreUtils.checkIdxAliasName(normName);
        }
        return normName;
    }

    /**
     * Normalize the metric name to be compliant to Kapua/Elasticserach constraints.<br>
     * It escapes the '$' and '.'
     *
     * @param name
     * @return
     * @since 1.0.0
     */
    public static String normalizeMetricName(String name) {
        String newName = name;
        if (newName.contains(".")) {
            newName = newName.replace(String.valueOf(SPECIAL_DOLLAR), SPECIAL_DOLLAR_ESC);
            newName = newName.replace(String.valueOf(SPECIAL_DOT), SPECIAL_DOT_ESC);
            LOG.trace(String.format("Metric %s contains a special char '%s' that will be replaced with '%s'", name, String.valueOf(SPECIAL_DOT), SPECIAL_DOT_ESC));
        }
        return newName;
    }

    /**
     * Restore the metric name, so switch back to the 'not escaped' values for '$' and '.'
     *
     * @param normalizedName
     * @return
     * @since 1.0.0
     */
    public static String restoreMetricName(String normalizedName) {
        String oldName = normalizedName;
        oldName = oldName.replace(SPECIAL_DOT_ESC, String.valueOf(SPECIAL_DOT));
        oldName = oldName.replace(SPECIAL_DOLLAR_ESC, String.valueOf(SPECIAL_DOLLAR));
        return oldName;
    }

    /**
     * Return the metric parts for the composed metric name (split the metric name by '.')
     *
     * @param fullName
     * @return
     */
    public static String[] getMetricParts(String fullName) {
        return fullName == null ? null : fullName.split(Pattern.quote("."));
    }

    /**
     * Check the index alias correctness.<br>
     * The alias cnnot be null, starts with '_', contains uppercase character or contains {@link DatastoreUtils#ILLEGAL_CHARS}
     *
     * @param alias
     * @since 1.0.0
     */
    public static void checkIdxAliasName(String alias) {
        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException(String.format("Alias name cannot be %s", alias == null ? "null" : "empty"));
        }
        if (alias.startsWith("_")) {
            throw new IllegalArgumentException(String.format("Alias name cannot start with _"));
        }
        for (int i = 0; i < alias.length(); i++) {
            if (Character.isUpperCase(alias.charAt(i))) {
                throw new IllegalArgumentException(String.format("Alias name cannot contain uppercase chars [found %s]", alias.charAt(i)));
            }
        }
        if (alias.contains(ILLEGAL_CHARS)) {
            throw new IllegalArgumentException(String.format("Alias name cannot contain special chars [found oneof %s]", ILLEGAL_CHARS));
        }
    }

    /**
     * Check the index name ({@link DatastoreUtils#checkIdxAliasName(String index)}
     *
     * @param index
     * @since 1.0.0
     */
    public static void checkIdxName(String index) {
        DatastoreUtils.checkIdxAliasName(index);
    }

    /**
     * Normalize the index alias name and replace the '-' with '_'
     *
     * @param alias
     * @return
     * @since 1.0.0
     */
    public static String normalizeIndexAliasName(String alias) {
        String aliasName = normalizeIndexName(alias);
        aliasName = aliasName.replace("-", "_");
        return aliasName;
    }

    /**
     * Normalize the account index name and and the suffix '-*'
     *
     * @param scopeId
     * @return
     */
    public static String getDataIndexName(KapuaId scopeId) {
        final StringBuilder sb = new StringBuilder();
        final String prefix = DatastoreSettings.getInstance().getString(DatastoreSettingKey.INDEX_PREFIX);
        if (StringUtils.isNotEmpty(prefix)) {
            sb.append(prefix).append("-");
        }
        String indexName = DatastoreUtils.normalizedIndexName(scopeId.toStringId());
        sb.append(indexName).append("-*");
        return sb.toString();
    }

    /**
     * Get the data index for the specified base name and timestamp
     *
     * @param scopeId
     * @param timestamp
     * @return
     */
    public static String getDataIndexName(KapuaId scopeId, long timestamp) throws KapuaException {
        try {
            final StringBuilder sb = new StringBuilder();
            final String prefix = DatastoreSettings.getInstance().getString(DatastoreSettingKey.INDEX_PREFIX);
            if (StringUtils.isNotEmpty(prefix)) {
                sb.append(prefix).append("-");
            }
            final String actualName = DatastoreUtils.normalizedIndexName(scopeId.toStringId());
            sb.append(actualName).append('-');
            String indexingWindowOption = MESSAGE_STORE_SERVICE.getConfigValues(scopeId).get(INDEXING_WINDOW_OPTION).toString();
            DateTimeFormatter formatter;
            switch (indexingWindowOption) {
                default:
                case INDEXING_WINDOW_OPTION_WEEK:
                    formatter = DATA_INDEX_FORMATTER_WEEK;
                    break;
                case INDEXING_WINDOW_OPTION_DAY:
                    formatter = DATA_INDEX_FORMATTER_DAY;
                    break;
                case INDEXING_WINDOW_OPTION_HOUR:
                    formatter = DATA_INDEX_FORMATTER_HOUR;
                    break;
            }
            formatter.formatTo(Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC), sb);
            return sb.toString();
        } catch (KapuaException kaex) {
            LOG.error("Error fetching MessageStoreService configuration", kaex);
            throw kaex;
        }
    }

    /**
     * Get the Kapua index name for the specified base name
     *
     * @param scopeId
     * @return
     * @since 1.0.0
     */
    public static String getRegistryIndexName(KapuaId scopeId) {
        final StringBuilder sb = new StringBuilder();
        final String prefix = DatastoreSettings.getInstance().getString(DatastoreSettingKey.INDEX_PREFIX);
        if (StringUtils.isNotEmpty(prefix)) {
            sb.append(prefix).append("-");
        }
        String indexName = DatastoreUtils.normalizedIndexName(scopeId.toStringId());
        sb.append(".").append(indexName);
        return sb.toString();
    }

    /**
     * Normalize the index ({@link DatastoreUtils#normalizeIndexName(String index)}
     *
     * @param index
     * @return
     */
    public static String normalizedIndexName(String index) {
        return normalizeIndexName(index);
    }

    /**
     * Return the list of the data indexes between start and end instant by scope id.
     *
     * @param scopeId
     * @param start
     * @param end
     * @return
     * @throws DatastoreException
     */
    public static String[] convertToDataIndexes(KapuaId scopeId, Instant start, Instant end) throws DatastoreException {
        List<String> indexes = new ArrayList<>();

        String indexWindowSize = null;
        try {
            indexWindowSize = MESSAGE_STORE_SERVICE.getConfigValues(scopeId).get(INDEXING_WINDOW_OPTION).toString();
        } catch (KapuaException kaex) {
            throw new ConfigurationException("Unable to read account settings", kaex);
        }

        switch (indexWindowSize) {
            case "WEEK":
                indexes = convertToDataIndexesByWeek(scopeId, start, end);
                break;
        }

        return indexes.toArray(new String[0]);
    }

    private static List<String> convertToDataIndexesByWeek(KapuaId scopeId, Instant start, Instant end) throws DatastoreException {
        // drop partial week so start from "from + 1 week" to "end - 1 week" included
        // if the start date is not the first day of the week and the end date is not the end day of the week
        Instant startInstant = getFirstDayOfTheWeek(start);
        Instant endInstant = getLastDayOfTheWeek(end);
        // this code:
        // int startYear = Year.from(startInstant).getValue();
        // int endYear = Year.from(startInstant).getValue();
        // throws java.time.temporal.UnsupportedTemporalTypeException: Unsupported field: Year
        // switch to use oldest library (Calendar)
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(KapuaDateUtils.getTimeZone()), KapuaDateUtils.getLocale());
        cal.setTime(Date.from(startInstant));
        int startYear = cal.get(Calendar.YEAR);
        cal.setTime(Date.from(endInstant));
        int endYear = cal.get(Calendar.YEAR);

        List<String> indexes = new ArrayList<>();
        while (startInstant.isBefore(endInstant) || areInThesameWeek(startInstant, endInstant)) {
            String index;
            try {
                index = DatastoreUtils.getDataIndexName(scopeId, startInstant.toEpochMilli());
            } catch (KapuaException kaex) {
                throw new ConfigurationException("Error while generating index name", kaex);
            }
            LOG.info("Adding index: {}", index);
            indexes.add(index);
            startInstant = startInstant.plus(7, ChronoUnit.DAYS);
        }
        //add last week of years (this algorithm can skip the last week of the year depending on the start date)
        for (int i = startYear; i < endYear; i++) {
            // get last year week
            for (int k = 0; k < 7; k++) {
                try {
                    String dateToCheck = String.format("%s-12-%sT06:00:00.000Z", i, (31 - k));
                    Instant instantToCheck = KapuaDateUtils.parseDate(dateToCheck).toInstant();
                    if (instantToCheck.isBefore(start)) {
                        break;
                    }
                    String indexToAdd = DatastoreUtils.getDataIndexName(scopeId, KapuaDateUtils.parseDate(dateToCheck).toInstant().toEpochMilli());
                    LOG.info("Index to add {} - date {}", indexToAdd, dateToCheck);
                    if (!indexes.contains(indexToAdd)) {
                        LOG.info("Adding index: {}", indexToAdd);
                        indexes.add(indexToAdd);
                        LOG.debug(">>> Add index {} - date {}", indexToAdd, dateToCheck);
                    } else {
                        LOG.debug("Index {} already present in the list", indexToAdd);
                    }
                } catch (ParseException e) {
                    LOG.error("Cannot evaluate week of the year for the date", e);
                    throw new DatastoreException(DatastoreErrorCodes.INTERNAL_ERROR, e);
                } catch (KapuaException kaex) {
                    throw new ConfigurationException("Error while generating index name", kaex);
                }
            }
        }
        return indexes;
    }

    private static List<String> convertToDataIndexesByDay(KapuaId scopeId, Instant start, Instant end) throws DatastoreException {
        List<String> indexes = new ArrayList<>();

        return indexes;
    }


    private static List<String> convertToDataIndexesByHour(KapuaId scopeId, Instant start, Instant end) throws DatastoreException {
        List<String> indexes = new ArrayList<>();

        return indexes;
    }

    public static List<String> filterIndexesBeforeDate(KapuaId scopeId, String[] indexes, Instant startInstant) throws DatastoreException {
        //see https://docs.oracle.com/javase/8/docs/api/java/util/List.html#remove-int-
        List<String> filteredIndexes = new ArrayList<>();
        String lastIndexToInclude;
        try {
            lastIndexToInclude = DatastoreUtils.getDataIndexName(scopeId, getLastDayOfTheWeek(startInstant).toEpochMilli());
        } catch (KapuaException kaex) {
            throw new ConfigurationException("Error while generating index name", kaex);
        }
        for (String index : indexes) {
            if (lastIndexToInclude.compareTo(index) >= 0) {
                filteredIndexes.add(index);
            }
        }
        return filteredIndexes;
    }

    private static Instant getLastDayOfTheWeek(Instant instant) {
        while (!isEndingDayOfTheWeek(instant)) {
            instant = instant.minus(1, ChronoUnit.DAYS);
        }
        return instant;
    }

    private static Instant getFirstDayOfTheWeek(Instant instant) {
        while (!isStartingDayOfTheWeek(instant)) {
            instant = instant.plus(1, ChronoUnit.DAYS);
        }
        return instant;
    }

    private static boolean isStartingDayOfTheWeek(Instant instant) {
        LocalDate localDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate localDateNextDay = instant.atZone(ZoneOffset.UTC).minusDays(1).toLocalDate();
        return localDate.get(WEEK_OF_YEAR) != localDateNextDay.get(WEEK_OF_YEAR);
        // DayOfWeek firstDayOfWeek = WeekFields.of(KapuaDateUtils.getLocale()).getFirstDayOfWeek();
        // LocalDate startOfCurrentWeek = localDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
        // return startOfCurrentWeek.isEqual(localDate);
    }

    private static boolean isEndingDayOfTheWeek(Instant instant) {
        LocalDate localDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate localDateNextDay = instant.atZone(ZoneOffset.UTC).plusDays(1).toLocalDate();
        return localDate.get(WEEK_OF_YEAR) != localDateNextDay.get(WEEK_OF_YEAR);
    }

    private static boolean areInThesameWeek(Instant instant1, Instant instant2) {
        LocalDate localDate1 = instant1.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate localDate2 = instant2.atZone(ZoneOffset.UTC).toLocalDate();
        return localDate1.get(WEEK_OF_YEAR) == localDate2.get(WEEK_OF_YEAR);
    }

    /**
     * Get the full metric name used to store the metric in Elasticsearch.<br>
     * The full metric name is composed by the metric and the type acronym as suffix ('.' is used as separator between the 2 parts)
     *
     * @param name
     * @param type
     * @return
     */
    public static String getMetricValueQualifier(String name, String type) {
        String shortType = DatastoreUtils.getClientMetricFromAcronym(type);
        return String.format("%s.%s", name, shortType);
    }

    /**
     * Get the client metric type from the metric value type
     *
     * @param clazz
     * @return
     * @since 1.0.0
     */
    public static String getClientMetricFromType(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("Metric value must not be null");
        }
        String value;
        if (clazz == String.class) {
            value = CLIENT_METRIC_TYPE_STRING;
        } else if (clazz == Integer.class) {
            value = CLIENT_METRIC_TYPE_INTEGER;
        } else if (clazz == Long.class) {
            value = CLIENT_METRIC_TYPE_LONG;
        } else if (clazz == Float.class) {
            value = CLIENT_METRIC_TYPE_FLOAT;
        } else if (clazz == Double.class) {
            value = CLIENT_METRIC_TYPE_DOUBLE;
        } else if (clazz == Boolean.class) {
            value = CLIENT_METRIC_TYPE_BOOLEAN;
        } else if (clazz == Date.class) {
            value = CLIENT_METRIC_TYPE_DATE;
        } else if (clazz == byte[].class) {
            value = CLIENT_METRIC_TYPE_BINARY;
        } else {
            throw new IllegalArgumentException(String.format("Metric value type for "));
        }
        return value;
    }

    /**
     * Get the client metric type acronym for the given client metric type full name
     *
     * @param acronym
     * @return
     * @since 1.0.0
     */
    public static String getClientMetricFromAcronym(String acronym) {
        if (CLIENT_METRIC_TYPE_STRING.equals(acronym)) {
            return CLIENT_METRIC_TYPE_STRING_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_INTEGER.equals(acronym)) {
            return CLIENT_METRIC_TYPE_INTEGER_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_LONG.equals(acronym)) {
            return CLIENT_METRIC_TYPE_LONG_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_FLOAT.equals(acronym)) {
            return CLIENT_METRIC_TYPE_FLOAT_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_DOUBLE.equals(acronym)) {
            return CLIENT_METRIC_TYPE_DOUBLE_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_BOOLEAN.equals(acronym)) {
            return CLIENT_METRIC_TYPE_BOOLEAN_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_DATE.equals(acronym)) {
            return CLIENT_METRIC_TYPE_DATE_ACRONYM;
        }
        if (CLIENT_METRIC_TYPE_BINARY.equals(acronym)) {
            return CLIENT_METRIC_TYPE_BINARY_ACRONYM;
        }
        throw new IllegalArgumentException(String.format("Unknown type [%s]", acronym));
    }

    /**
     * Check if the metric type is date
     *
     * @param acronym
     * @return
     */
    public static boolean isDateMetric(String acronym) {
        return CLIENT_METRIC_TYPE_DATE_ACRONYM.equals(acronym);
    }

    /**
     * Convert the metric value class type (Kapua side) to the proper string type description (client side)
     *
     * @param aClass
     * @return
     * @since 1.0.0
     */
    public static <T> String convertToClientMetricType(Class<T> aClass) {
        if (aClass == String.class) {
            return CLIENT_METRIC_TYPE_STRING;
        }
        if (aClass == Integer.class) {
            return CLIENT_METRIC_TYPE_INTEGER;
        }
        if (aClass == Long.class) {
            return CLIENT_METRIC_TYPE_LONG;
        }
        if (aClass == Float.class) {
            return CLIENT_METRIC_TYPE_FLOAT;
        }
        if (aClass == Double.class) {
            return CLIENT_METRIC_TYPE_DOUBLE;
        }
        if (aClass == Boolean.class) {
            return CLIENT_METRIC_TYPE_BOOLEAN;
        }
        if (aClass == Date.class) {
            return CLIENT_METRIC_TYPE_DATE;
        }
        if (aClass == byte[].class) {
            return CLIENT_METRIC_TYPE_BINARY;
        }
        throw new IllegalArgumentException(String.format("Unknown type [%s]", aClass.getName()));
    }

    /**
     * Convert the client metric type to the corresponding Kapua type
     *
     * @param clientType
     * @return
     * @since 1.0.0
     */
    public static Class<?> convertToKapuaType(String clientType) {
        Class<?> clazz;
        if (CLIENT_METRIC_TYPE_STRING.equals(clientType)) {
            clazz = String.class;
        } else if (CLIENT_METRIC_TYPE_INTEGER.equals(clientType)) {
            clazz = Integer.class;
        } else if (CLIENT_METRIC_TYPE_LONG.equals(clientType)) {
            clazz = Long.class;
        } else if (CLIENT_METRIC_TYPE_FLOAT.equals(clientType)) {
            clazz = Float.class;
        } else if (CLIENT_METRIC_TYPE_DOUBLE.equals(clientType)) {
            clazz = Double.class;
        } else if (CLIENT_METRIC_TYPE_BOOLEAN.equals(clientType)) {
            clazz = Boolean.class;
        } else if (CLIENT_METRIC_TYPE_DATE.equals(clientType)) {
            clazz = Date.class;
        } else if (CLIENT_METRIC_TYPE_BINARY.equals(clientType)) {
            clazz = byte[].class;
        } else {
            throw new IllegalArgumentException(String.format("Unknown type [%s]", clientType));
        }
        return clazz;
    }

    /**
     * Convert the metric value to the correct type using the metric acronym type
     *
     * @param acronymType
     * @param value
     * @return
     * @since 1.0.0
     */
    public static Object convertToCorrectType(String acronymType, Object value) {
        Object convertedValue = null;
        if (CLIENT_METRIC_TYPE_DOUBLE_ACRONYM.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Double(((Number) value).doubleValue());
            } else if (value instanceof String) {
                convertedValue = Double.parseDouble((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Double!", getValueClass(value)));
            }
        } else if (CLIENT_METRIC_TYPE_FLOAT_ACRONYM.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Float(((Number) value).floatValue());
            } else if (value instanceof String) {
                convertedValue = Float.parseFloat((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Double!", getValueClass(value)));
            }
        } else if (CLIENT_METRIC_TYPE_INTEGER_ACRONYM.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Integer(((Number) value).intValue());
            } else if (value instanceof String) {
                convertedValue = Integer.parseInt((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Double!", getValueClass(value)));
            }
        } else if (CLIENT_METRIC_TYPE_LONG_ACRONYM.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Long(((Number) value).longValue());
            } else if (value instanceof String) {
                convertedValue = Long.parseLong((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Long!", getValueClass(value)));
            }
        } else if (CLIENT_METRIC_TYPE_DATE_ACRONYM.equals(acronymType)) {
            if (value instanceof Date) {
                convertedValue = (Date) value;
            } else if (value instanceof String) {
                try {
                    convertedValue = KapuaDateUtils.parseDate((String) value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(
                            String.format("Type [%s] cannot be converted to Date. Allowed format [%s] - Value to convert [%s]!", getValueClass(value), KapuaDateUtils.ISO_DATE_PATTERN,
                                    value));
                }
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Date!", getValueClass(value)));
            }
        } else {
            // no need to translate for others field type
            convertedValue = value;
        }
        return convertedValue;
    }

    private static String getValueClass(Object value) {
        return value != null ? value.getClass().toString() : "null";
    }

}
