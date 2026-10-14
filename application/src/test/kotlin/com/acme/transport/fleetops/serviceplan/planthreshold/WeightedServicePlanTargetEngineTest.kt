package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedServicePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedServicePlanTargetEngine(newRegistry())
        val item = newWeightedServicePlanTarget("code-969", "label-619")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
