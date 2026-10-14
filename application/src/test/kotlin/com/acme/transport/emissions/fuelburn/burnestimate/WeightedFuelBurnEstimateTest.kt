package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedFuelBurnEstimate("code-330", "name-815")
        assertEquals(WeightedFuelBurnEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newWeightedFuelBurnEstimate("code-330", "name-815")
        try {
            item.moveTo(WeightedFuelBurnEstimateStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnEstimateStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedFuelBurnEstimate("code-330", "name-815")
        item.status = WeightedFuelBurnEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedFuelBurnEstimateStatus.REQUESTED) }
    }
}
