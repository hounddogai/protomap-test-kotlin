package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedSortationMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newCappedSortationMilestone("code-140", "label-421")
        assertEquals(CappedSortationMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedSortationMilestone("code-140", "label-421")
        try {
            item.moveTo(CappedSortationMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedSortationMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedSortationMilestone("code-140", "label-421")
        item.status = CappedSortationMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(CappedSortationMilestoneStatus.CREATED) }
    }
}
