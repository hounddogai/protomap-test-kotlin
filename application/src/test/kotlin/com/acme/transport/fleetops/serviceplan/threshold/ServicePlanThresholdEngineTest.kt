package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanThresholdEngine(newRegistry())
        val item = newServicePlanThreshold("code-919", "title-373")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
