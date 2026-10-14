package com.acme.transport.terminal.sortation.sortationadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSortationAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledSortationAdjustment("code-442", "title-888")
        assertEquals(ScheduledSortationAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledSortationAdjustment("code-442", "title-888")
        try {
            item.moveTo(ScheduledSortationAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSortationAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledSortationAdjustment("code-442", "title-888")
        item.status = ScheduledSortationAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledSortationAdjustmentStatus.IDLE) }
    }
}
