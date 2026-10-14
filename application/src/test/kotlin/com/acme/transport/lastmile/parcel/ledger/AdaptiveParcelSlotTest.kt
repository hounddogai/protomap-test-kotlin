package com.acme.transport.lastmile.parcel.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveParcelSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveParcelSlot("code-775", "label-2")
        assertEquals(AdaptiveParcelSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveParcelSlot("code-775", "label-2")
        try {
            item.moveTo(AdaptiveParcelSlotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveParcelSlotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveParcelSlot("code-775", "label-2")
        item.status = AdaptiveParcelSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveParcelSlotStatus.REQUESTED) }
    }
}
