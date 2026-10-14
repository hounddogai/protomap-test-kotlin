package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimarySortationMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimarySortationMilestone("code-711", "name-873")
        assertEquals(PrimarySortationMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPrimarySortationMilestone("code-711", "name-873")
        try {
            item.moveTo(PrimarySortationMilestoneStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimarySortationMilestoneStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimarySortationMilestone("code-711", "name-873")
        item.status = PrimarySortationMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(PrimarySortationMilestoneStatus.DRAFT) }
    }
}
