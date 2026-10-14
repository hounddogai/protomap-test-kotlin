package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCameraEventMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryCameraEventMilestone("code-879", "title-710")
        assertEquals(PrimaryCameraEventMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPrimaryCameraEventMilestone("code-879", "title-710")
        try {
            item.moveTo(PrimaryCameraEventMilestoneStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCameraEventMilestoneStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryCameraEventMilestone("code-879", "title-710")
        item.status = PrimaryCameraEventMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryCameraEventMilestoneStatus.DRAFT) }
    }
}
