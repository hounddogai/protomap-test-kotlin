package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedServicePlanManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedServicePlanManifestEngine(newRegistry())
        val item = newWeightedServicePlanManifest("code-935", "title-19")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
