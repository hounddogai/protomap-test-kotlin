package com.acme.transport.terminal.sortation.sortationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newSortationMilestone("code-806", "label-896")
        assertEquals(SortationMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSortationMilestone("code-806", "label-896")
        try {
            item.moveTo(SortationMilestoneStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationMilestoneStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSortationMilestone("code-806", "label-896")
        item.status = SortationMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(SortationMilestoneStatus.DRAFT) }
    }
}
