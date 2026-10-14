package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelBurnPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledFuelBurnPlan("code-722", "label-835")
        assertEquals(ScheduledFuelBurnPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newScheduledFuelBurnPlan("code-722", "label-835")
        try {
            item.moveTo(ScheduledFuelBurnPlanStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelBurnPlanStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledFuelBurnPlan("code-722", "label-835")
        item.status = ScheduledFuelBurnPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledFuelBurnPlanStatus.REQUESTED) }
    }
}
