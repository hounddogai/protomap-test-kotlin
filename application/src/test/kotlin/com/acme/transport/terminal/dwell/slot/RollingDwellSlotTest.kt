package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDwellSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingDwellSlot("code-555", "title-191")
        assertEquals(RollingDwellSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRollingDwellSlot("code-555", "title-191")
        try {
            item.moveTo(RollingDwellSlotStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDwellSlotStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingDwellSlot("code-555", "title-191")
        item.status = RollingDwellSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingDwellSlotStatus.PLANNED) }
    }
}
