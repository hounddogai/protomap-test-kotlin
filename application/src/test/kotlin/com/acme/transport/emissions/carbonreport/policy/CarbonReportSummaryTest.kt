package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newCarbonReportSummary("code-958", "title-804")
        assertEquals(CarbonReportSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCarbonReportSummary("code-958", "title-804")
        try {
            item.moveTo(CarbonReportSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCarbonReportSummary("code-958", "title-804")
        item.status = CarbonReportSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(CarbonReportSummaryStatus.PLANNED) }
    }
}
