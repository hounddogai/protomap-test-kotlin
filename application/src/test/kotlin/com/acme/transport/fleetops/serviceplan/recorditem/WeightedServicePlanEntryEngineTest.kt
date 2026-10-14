package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedServicePlanEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedServicePlanEntryEngine(newRegistry())
        val item = newWeightedServicePlanEntry("code-581", "name-383")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
