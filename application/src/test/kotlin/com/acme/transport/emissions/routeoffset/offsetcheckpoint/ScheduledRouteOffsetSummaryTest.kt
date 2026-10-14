package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRouteOffsetSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledRouteOffsetSummary("code-99", "name-612")
        assertEquals(ScheduledRouteOffsetSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledRouteOffsetSummary("code-99", "name-612")
        try {
            item.moveTo(ScheduledRouteOffsetSummaryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRouteOffsetSummaryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledRouteOffsetSummary("code-99", "name-612")
        item.status = ScheduledRouteOffsetSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledRouteOffsetSummaryStatus.OPEN) }
    }
}
