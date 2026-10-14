package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAttemptMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackAttemptMilestone("code-535", "label-870")
        assertEquals(FallbackAttemptMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFallbackAttemptMilestone("code-535", "label-870")
        try {
            item.moveTo(FallbackAttemptMilestoneStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAttemptMilestoneStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackAttemptMilestone("code-535", "label-870")
        item.status = FallbackAttemptMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackAttemptMilestoneStatus.DRAFT) }
    }
}
