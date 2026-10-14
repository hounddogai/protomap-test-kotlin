package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingIdlingMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingIdlingMilestone("code-503", "title-137")
        assertEquals(PendingIdlingMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingIdlingMilestone("code-503", "title-137")
        try {
            item.moveTo(PendingIdlingMilestoneStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingIdlingMilestoneStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingIdlingMilestone("code-503", "title-137")
        item.status = PendingIdlingMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingIdlingMilestoneStatus.REQUESTED) }
    }
}
