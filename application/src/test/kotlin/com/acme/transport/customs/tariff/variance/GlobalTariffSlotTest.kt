package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTariffSlotTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalTariffSlot("code-819", "name-199")
        assertEquals(GlobalTariffSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newGlobalTariffSlot("code-819", "name-199")
        try {
            item.moveTo(GlobalTariffSlotStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTariffSlotStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalTariffSlot("code-819", "name-199")
        item.status = GlobalTariffSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalTariffSlotStatus.OPEN) }
    }
}
