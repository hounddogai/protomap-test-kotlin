package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerYardMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalTrailerYardMilestone("code-825", "title-543")
        assertEquals(GlobalTrailerYardMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newGlobalTrailerYardMilestone("code-825", "title-543")
        try {
            item.moveTo(GlobalTrailerYardMilestoneStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerYardMilestoneStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalTrailerYardMilestone("code-825", "title-543")
        item.status = GlobalTrailerYardMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalTrailerYardMilestoneStatus.REQUESTED) }
    }
}
