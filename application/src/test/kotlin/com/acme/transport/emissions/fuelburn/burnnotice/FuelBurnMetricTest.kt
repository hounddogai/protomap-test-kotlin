package com.acme.transport.emissions.fuelburn.burnnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelBurnMetric("code-716", "name-126")
        assertEquals(FuelBurnMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFuelBurnMetric("code-716", "name-126")
        try {
            item.moveTo(FuelBurnMetricStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnMetricStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelBurnMetric("code-716", "name-126")
        item.status = FuelBurnMetricStatus.STOPPED
        assertFails("move") { item.moveTo(FuelBurnMetricStatus.IDLE) }
    }
}
