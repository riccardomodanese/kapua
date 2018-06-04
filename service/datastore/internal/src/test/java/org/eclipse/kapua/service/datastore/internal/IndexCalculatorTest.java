/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.internal;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreException;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class IndexCalculatorTest extends AbstractMessageStoreServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(IndexCalculatorTest.class);

    @Test
    public void testIndex() throws KapuaException, ParseException, InterruptedException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        // performTest(sdf.parse("01/01/2000 13:12"), sdf.parse("01/01/2020 13:12"), buildExpectedResult("1", 1, 2000, 1, 2020, new int[] {
        // 53,// 2000 for locale us - 52 for locale "Europe"
        // 52,// 2001
        // 52,// 2002
        // 52,// 2003
        // 53,// 2004
        // 53,// 2005 for locale us - 52 for locale "Europe"
        // 52,// 2006
        // 52,// 2007
        // 52,// 2008
        // 53,// 2009
        // 53,// 2010 for locale us - 52 for locale "Europe"
        // 53,// 2011 for locale us - 52 for locale "Europe"
        // 52,// 2012
        // 52,// 2013
        // 52,// 2014
        // 53,// 2015 for locale us - 52 for locale "Europe"
        // 53,// 2016 for locale us - 52 for locale "Europe"
        // 52,// 2017
        // 52,// 2018
        // 52,// 2019
        // 53// 2020 for locale us - 52 for locale "Europe"
        //
        // }));
        performTest(sdf.parse("02/01/2017 13:12"), sdf.parse("02/07/2017 13:12"), buildExpectedResult("1", 2, 2017, 26, 2017, null));
        performTest(sdf.parse("02/01/2017 13:12"), sdf.parse("01/07/2017 13:12"), buildExpectedResult("1", 2, 2017, 26, 2017, null));
        performTest(sdf.parse("01/01/2017 13:12"), sdf.parse("02/07/2017 13:12"), buildExpectedResult("1", 1, 2017, 26, 2017, null));
        performTest(sdf.parse("31/12/2016 13:12"), sdf.parse("02/07/2017 13:12"), buildExpectedResult("1", 1, 2017, 26, 2017, null));
        performTest(sdf.parse("01/01/2017 13:12"), sdf.parse("01/07/2017 13:12"), buildExpectedResult("1", 1, 2017, 26, 2017, null));

        performTest(sdf.parse("01/01/2017 13:12"), sdf.parse("08/01/2017 13:12"), buildExpectedResult("1", 1, 2017, 1, 2017, null));
        performTest(sdf.parse("01/01/2017 13:12"), sdf.parse("07/01/2017 13:12"), buildExpectedResult("1", 1, 2017, 1, 2017, null));
        performTest(sdf.parse("01/01/2017 13:12"), sdf.parse("06/01/2017 13:12"), null);
    }

    private void performTest(Date startDate, Date endDate, String[] expectedIndexes) throws DatastoreException {
        Calendar calStartDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"), KapuaDateUtils.getLocale());
        calStartDate.setTimeInMillis(startDate.getTime());
        Calendar calEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"), KapuaDateUtils.getLocale());
        calEndDate.setTimeInMillis(endDate.getTime());

        LOG.info("StartDate week {} - day {} *** EndDate week {} - day {}",
                calStartDate.get(Calendar.WEEK_OF_YEAR),
                calStartDate.get(Calendar.DAY_OF_WEEK),
                calEndDate.get(Calendar.WEEK_OF_YEAR),
                calEndDate.get(Calendar.DAY_OF_WEEK));

        String[] index = DatastoreUtils.convertToDataIndexes(KapuaEid.ONE, startDate.toInstant(), endDate.toInstant());
        compareResult(expectedIndexes, index);
    }

    private String[] buildExpectedResult(String scopeId, int startWeek, int startYear, int endWeek, int endYear, int[] weekCountByYear) {
        List<String> result = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            int startWeekForCurrentYear = startWeek;
            if (i != endYear) {
                startWeekForCurrentYear = 1;
            }
            int endWeekForCurrentYear = endWeek;
            if (i != endYear) {
                endWeekForCurrentYear = weekCountByYear[endYear - i];
            }
            for (int j = startWeekForCurrentYear; j <= endWeekForCurrentYear; j++) {
                result.add(String.format("%s-%s-%s", scopeId, i, (j < 10 ? "0" + j : j)));
            }
        }
        return result.toArray(new String[0]);
    }

    private void compareResult(String[] expected, String[] result) {
        if (result != null) {
            assertEquals("Wrong result size!", (expected != null ? expected.length : 0), result.length);
            for (int i = 0; i < result.length; i++) {
                assertEquals("Wrong result!", expected[i], result[i]);
            }
        } else {
            assertTrue("Wrong result size!", expected == null || expected.length <= 0);
        }
    }

}
