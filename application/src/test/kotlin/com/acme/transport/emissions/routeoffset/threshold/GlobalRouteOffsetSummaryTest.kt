package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRouteOffsetSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalRouteOffsetSummary("code-17", "label-712")
        assertEquals(GlobalRouteOffsetSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalRouteOffsetSummary("code-17", "label-712")
        try {
            item.moveTo(GlobalRouteOffsetSummaryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRouteOffsetSummaryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalRouteOffsetSummary("code-17", "label-712")
        item.status = GlobalRouteOffsetSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalRouteOffsetSummaryStatus.REQUESTED) }
    }
}
