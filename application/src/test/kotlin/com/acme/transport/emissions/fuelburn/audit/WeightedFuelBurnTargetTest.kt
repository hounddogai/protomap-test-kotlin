package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedFuelBurnTarget("code-217", "name-553")
        assertEquals(WeightedFuelBurnTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newWeightedFuelBurnTarget("code-217", "name-553")
        try {
            item.moveTo(WeightedFuelBurnTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedFuelBurnTarget("code-217", "name-553")
        item.status = WeightedFuelBurnTargetStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedFuelBurnTargetStatus.IDLE) }
    }
}
