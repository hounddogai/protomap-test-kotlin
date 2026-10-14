package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelBurnSlotTest {
    @Test
    fun `starts in open`() {
        val item = newTieredFuelBurnSlot("code-481", "name-136")
        assertEquals(TieredFuelBurnSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTieredFuelBurnSlot("code-481", "name-136")
        try {
            item.moveTo(TieredFuelBurnSlotStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelBurnSlotStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredFuelBurnSlot("code-481", "name-136")
        item.status = TieredFuelBurnSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredFuelBurnSlotStatus.OPEN) }
    }
}
