package com.acme.transport.terminal.dwell.dwelldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDwellAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledDwellAdjustment("code-138", "title-291")
        assertEquals(ScheduledDwellAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledDwellAdjustment("code-138", "title-291")
        try {
            item.moveTo(ScheduledDwellAdjustmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDwellAdjustmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledDwellAdjustment("code-138", "title-291")
        item.status = ScheduledDwellAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledDwellAdjustmentStatus.DRAFT) }
    }
}
