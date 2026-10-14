package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTransitTimeMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingTransitTimeMilestone("code-850", "name-116")
        assertEquals(PendingTransitTimeMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPendingTransitTimeMilestone("code-850", "name-116")
        try {
            item.moveTo(PendingTransitTimeMilestoneStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTransitTimeMilestoneStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingTransitTimeMilestone("code-850", "name-116")
        item.status = PendingTransitTimeMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingTransitTimeMilestoneStatus.REQUESTED) }
    }
}
