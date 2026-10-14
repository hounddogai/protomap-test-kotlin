package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTransitTimeSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledTransitTimeSummary("code-453", "name-292")
        assertEquals(ScheduledTransitTimeSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledTransitTimeSummary("code-453", "name-292")
        try {
            item.moveTo(ScheduledTransitTimeSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTransitTimeSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledTransitTimeSummary("code-453", "name-292")
        item.status = ScheduledTransitTimeSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledTransitTimeSummaryStatus.CREATED) }
    }
}
