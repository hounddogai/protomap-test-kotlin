package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newEmissionFactorPlan("code-468", "name-288")
        assertEquals(EmissionFactorPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newEmissionFactorPlan("code-468", "name-288")
        try {
            item.moveTo(EmissionFactorPlanStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorPlanStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEmissionFactorPlan("code-468", "name-288")
        item.status = EmissionFactorPlanStatus.STOPPED
        assertFails("move") { item.moveTo(EmissionFactorPlanStatus.IDLE) }
    }
}
