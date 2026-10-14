package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelBurnVariance("code-490", "title-827")
        assertEquals(FuelBurnVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFuelBurnVariance("code-490", "title-827")
        try {
            item.moveTo(FuelBurnVarianceStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnVarianceStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelBurnVariance("code-490", "title-827")
        item.status = FuelBurnVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(FuelBurnVarianceStatus.IDLE) }
    }
}
