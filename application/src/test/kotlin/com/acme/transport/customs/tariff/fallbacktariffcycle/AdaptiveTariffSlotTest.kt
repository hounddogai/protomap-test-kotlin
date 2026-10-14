package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTariffSlotTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveTariffSlot("code-674", "title-474")
        assertEquals(AdaptiveTariffSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveTariffSlot("code-674", "title-474")
        try {
            item.moveTo(AdaptiveTariffSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTariffSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveTariffSlot("code-674", "title-474")
        item.status = AdaptiveTariffSlotStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveTariffSlotStatus.NEW) }
    }
}
