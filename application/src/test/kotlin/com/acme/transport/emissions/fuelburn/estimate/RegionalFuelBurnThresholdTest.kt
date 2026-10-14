package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelBurnThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalFuelBurnThreshold("code-257", "label-979")
        assertEquals(RegionalFuelBurnThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalFuelBurnThreshold("code-257", "label-979")
        try {
            item.moveTo(RegionalFuelBurnThresholdStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelBurnThresholdStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalFuelBurnThreshold("code-257", "label-979")
        item.status = RegionalFuelBurnThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalFuelBurnThresholdStatus.PLANNED) }
    }
}
