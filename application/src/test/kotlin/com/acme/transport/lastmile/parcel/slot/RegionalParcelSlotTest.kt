package com.acme.transport.lastmile.parcel.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelSlotTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalParcelSlot("code-830", "title-3")
        assertEquals(RegionalParcelSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalParcelSlot("code-830", "title-3")
        try {
            item.moveTo(RegionalParcelSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalParcelSlot("code-830", "title-3")
        item.status = RegionalParcelSlotStatus.FAILED
        assertFails("move") { item.moveTo(RegionalParcelSlotStatus.NEW) }
    }
}
