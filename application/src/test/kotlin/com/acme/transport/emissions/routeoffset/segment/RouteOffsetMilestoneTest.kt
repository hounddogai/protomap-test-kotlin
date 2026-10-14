package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newRouteOffsetMilestone("code-958", "title-562")
        assertEquals(RouteOffsetMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRouteOffsetMilestone("code-958", "title-562")
        try {
            item.moveTo(RouteOffsetMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRouteOffsetMilestone("code-958", "title-562")
        item.status = RouteOffsetMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(RouteOffsetMilestoneStatus.PLANNED) }
    }
}
