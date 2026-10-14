package com.acme.transport.emissions.routeoffset.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRouteOffsetMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingRouteOffsetMilestone("code-902", "label-821")
        assertEquals(RollingRouteOffsetMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingRouteOffsetMilestone("code-902", "label-821")
        try {
            item.moveTo(RollingRouteOffsetMilestoneStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRouteOffsetMilestoneStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingRouteOffsetMilestone("code-902", "label-821")
        item.status = RollingRouteOffsetMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingRouteOffsetMilestoneStatus.PLANNED) }
    }
}
