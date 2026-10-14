package com.acme.transport.linehaul.laneplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newLanePlanSlot("code-613", "name-375")
        assertEquals(LanePlanSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLanePlanSlot("code-613", "name-375")
        try {
            item.moveTo(LanePlanSlotStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanSlotStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLanePlanSlot("code-613", "name-375")
        item.status = LanePlanSlotStatus.CLOSED
        assertFails("move") { item.moveTo(LanePlanSlotStatus.DRAFT) }
    }
}
