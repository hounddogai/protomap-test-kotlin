package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newTariffSlot("code-707", "label-499")
        assertEquals(TariffSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTariffSlot("code-707", "label-499")
        try {
            item.moveTo(TariffSlotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffSlotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTariffSlot("code-707", "label-499")
        item.status = TariffSlotStatus.CLOSED
        assertFails("move") { item.moveTo(TariffSlotStatus.DRAFT) }
    }
}
