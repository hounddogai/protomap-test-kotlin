package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerYardMilestone("code-2", "name-593")
        assertEquals(TrailerYardMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTrailerYardMilestone("code-2", "name-593")
        try {
            item.moveTo(TrailerYardMilestoneStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardMilestoneStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerYardMilestone("code-2", "name-593")
        item.status = TrailerYardMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerYardMilestoneStatus.DRAFT) }
    }
}
