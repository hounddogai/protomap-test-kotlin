package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedFuelBurnGroup("code-92", "label-152")
        assertEquals(WeightedFuelBurnGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedFuelBurnGroup("code-92", "label-152")
        try {
            item.moveTo(WeightedFuelBurnGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedFuelBurnGroup("code-92", "label-152")
        item.status = WeightedFuelBurnGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedFuelBurnGroupStatus.REQUESTED) }
    }
}
