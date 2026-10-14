package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTireCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedTireCycle("code-411", "label-647")
        assertEquals(WeightedTireCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newWeightedTireCycle("code-411", "label-647")
        try {
            item.moveTo(WeightedTireCycleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTireCycleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedTireCycle("code-411", "label-647")
        item.status = WeightedTireCycleStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedTireCycleStatus.IDLE) }
    }
}
