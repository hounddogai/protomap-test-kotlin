package com.acme.transport.crewing.roster.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newRosterAdjustment("code-463", "name-347")
        assertEquals(RosterAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRosterAdjustment("code-463", "name-347")
        try {
            item.moveTo(RosterAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRosterAdjustment("code-463", "name-347")
        item.status = RosterAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(RosterAdjustmentStatus.NEW) }
    }
}
