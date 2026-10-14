package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanTargetEngine(newRegistry())
        val item = newServicePlanTarget("code-302", "label-365")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
