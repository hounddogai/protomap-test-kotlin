package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRouteOffsetSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalRouteOffsetSummary("code-180", "label-827")
        assertEquals(RegionalRouteOffsetSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalRouteOffsetSummary("code-180", "label-827")
        try {
            item.moveTo(RegionalRouteOffsetSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRouteOffsetSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalRouteOffsetSummary("code-180", "label-827")
        item.status = RegionalRouteOffsetSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalRouteOffsetSummaryStatus.PLANNED) }
    }
}
