package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCarbonReportSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientCarbonReportSummary("code-530", "name-316")
        assertEquals(LenientCarbonReportSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientCarbonReportSummary("code-530", "name-316")
        try {
            item.moveTo(LenientCarbonReportSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCarbonReportSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientCarbonReportSummary("code-530", "name-316")
        item.status = LenientCarbonReportSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientCarbonReportSummaryStatus.PLANNED) }
    }
}
