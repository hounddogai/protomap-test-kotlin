package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRouteOffsetMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredRouteOffsetMilestone("code-583", "title-650")
        assertEquals(TieredRouteOffsetMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredRouteOffsetMilestone("code-583", "title-650")
        try {
            item.moveTo(TieredRouteOffsetMilestoneStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRouteOffsetMilestoneStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredRouteOffsetMilestone("code-583", "title-650")
        item.status = TieredRouteOffsetMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredRouteOffsetMilestoneStatus.PROPOSED) }
    }
}
