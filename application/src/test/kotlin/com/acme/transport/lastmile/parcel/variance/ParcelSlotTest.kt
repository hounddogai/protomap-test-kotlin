package com.acme.transport.lastmile.parcel.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelSlot("code-181", "label-433")
        assertEquals(ParcelSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newParcelSlot("code-181", "label-433")
        try {
            item.moveTo(ParcelSlotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelSlotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelSlot("code-181", "label-433")
        item.status = ParcelSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelSlotStatus.REQUESTED) }
    }
}
