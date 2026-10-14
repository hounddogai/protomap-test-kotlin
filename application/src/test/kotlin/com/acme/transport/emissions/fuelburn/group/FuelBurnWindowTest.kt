package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelBurnWindow("code-224", "title-98")
        assertEquals(FuelBurnWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newFuelBurnWindow("code-224", "title-98")
        try {
            item.moveTo(FuelBurnWindowStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnWindowStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelBurnWindow("code-224", "title-98")
        item.status = FuelBurnWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelBurnWindowStatus.PLANNED) }
    }
}
