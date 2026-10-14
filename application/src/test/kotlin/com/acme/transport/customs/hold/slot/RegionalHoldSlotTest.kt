package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalHoldSlotTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalHoldSlot("code-370", "name-427")
        assertEquals(RegionalHoldSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalHoldSlot("code-370", "name-427")
        try {
            item.moveTo(RegionalHoldSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalHoldSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalHoldSlot("code-370", "name-427")
        item.status = RegionalHoldSlotStatus.FAILED
        assertFails("move") { item.moveTo(RegionalHoldSlotStatus.NEW) }
    }
}
