package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDockDoorCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveDockDoorCycle("code-975", "label-592")
        assertEquals(AdaptiveDockDoorCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveDockDoorCycle("code-975", "label-592")
        try {
            item.moveTo(AdaptiveDockDoorCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDockDoorCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveDockDoorCycle("code-975", "label-592")
        item.status = AdaptiveDockDoorCycleStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveDockDoorCycleStatus.DRAFT) }
    }
}
