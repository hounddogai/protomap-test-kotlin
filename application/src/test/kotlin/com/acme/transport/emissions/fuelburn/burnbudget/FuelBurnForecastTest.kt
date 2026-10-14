package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelBurnForecast("code-371", "title-585")
        assertEquals(FuelBurnForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFuelBurnForecast("code-371", "title-585")
        try {
            item.moveTo(FuelBurnForecastStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnForecastStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelBurnForecast("code-371", "title-585")
        item.status = FuelBurnForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelBurnForecastStatus.REQUESTED) }
    }
}
