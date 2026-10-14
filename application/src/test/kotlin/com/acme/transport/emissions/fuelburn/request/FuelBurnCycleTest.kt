package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnCycleTest {
    @Test
    fun `starts in open`() {
        val item = newFuelBurnCycle("code-535", "name-510")
        assertEquals(FuelBurnCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFuelBurnCycle("code-535", "name-510")
        try {
            item.moveTo(FuelBurnCycleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnCycleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelBurnCycle("code-535", "name-510")
        item.status = FuelBurnCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelBurnCycleStatus.OPEN) }
    }
}
