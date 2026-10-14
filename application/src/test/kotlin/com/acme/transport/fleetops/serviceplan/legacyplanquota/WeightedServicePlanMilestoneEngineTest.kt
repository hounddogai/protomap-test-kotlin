package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedServicePlanMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedServicePlanMilestoneEngine(newRegistry())
        val item = newWeightedServicePlanMilestone("code-838", "label-22")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
