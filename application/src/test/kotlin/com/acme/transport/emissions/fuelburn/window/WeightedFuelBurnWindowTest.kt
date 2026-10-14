package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedFuelBurnWindow("code-463", "name-771")
        assertEquals(WeightedFuelBurnWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedFuelBurnWindow("code-463", "name-771")
        try {
            item.moveTo(WeightedFuelBurnWindowStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnWindowStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedFuelBurnWindow("code-463", "name-771")
        item.status = WeightedFuelBurnWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedFuelBurnWindowStatus.PROPOSED) }
    }
}
