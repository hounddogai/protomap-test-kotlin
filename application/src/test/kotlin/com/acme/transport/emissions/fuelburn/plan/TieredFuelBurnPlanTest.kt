package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelBurnPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredFuelBurnPlan("code-615", "title-750")
        assertEquals(TieredFuelBurnPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTieredFuelBurnPlan("code-615", "title-750")
        try {
            item.moveTo(TieredFuelBurnPlanStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelBurnPlanStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredFuelBurnPlan("code-615", "title-750")
        item.status = TieredFuelBurnPlanStatus.STOPPED
        assertFails("move") { item.moveTo(TieredFuelBurnPlanStatus.IDLE) }
    }
}
