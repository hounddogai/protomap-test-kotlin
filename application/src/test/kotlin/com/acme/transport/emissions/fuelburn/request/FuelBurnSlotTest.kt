package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnSlotTest {
    @Test
    fun `starts in new`() {
        val item = newFuelBurnSlot("code-871", "name-63")
        assertEquals(FuelBurnSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFuelBurnSlot("code-871", "name-63")
        try {
            item.moveTo(FuelBurnSlotStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnSlotStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelBurnSlot("code-871", "name-63")
        item.status = FuelBurnSlotStatus.FAILED
        assertFails("move") { item.moveTo(FuelBurnSlotStatus.NEW) }
    }
}
