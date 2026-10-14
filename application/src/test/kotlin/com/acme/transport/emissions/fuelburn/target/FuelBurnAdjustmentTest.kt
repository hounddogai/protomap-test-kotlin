package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelBurnAdjustment("code-891", "title-703")
        assertEquals(FuelBurnAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFuelBurnAdjustment("code-891", "title-703")
        try {
            item.moveTo(FuelBurnAdjustmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnAdjustmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelBurnAdjustment("code-891", "title-703")
        item.status = FuelBurnAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(FuelBurnAdjustmentStatus.IDLE) }
    }
}
