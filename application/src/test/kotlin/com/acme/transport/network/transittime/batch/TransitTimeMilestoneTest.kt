package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newTransitTimeMilestone("code-282", "label-813")
        assertEquals(TransitTimeMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTransitTimeMilestone("code-282", "label-813")
        try {
            item.moveTo(TransitTimeMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTransitTimeMilestone("code-282", "label-813")
        item.status = TransitTimeMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(TransitTimeMilestoneStatus.OPEN) }
    }
}
