package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newRouteOffsetSummary("code-206", "title-33")
        assertEquals(RouteOffsetSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRouteOffsetSummary("code-206", "title-33")
        try {
            item.moveTo(RouteOffsetSummaryStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetSummaryStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRouteOffsetSummary("code-206", "title-33")
        item.status = RouteOffsetSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RouteOffsetSummaryStatus.PROPOSED) }
    }
}
