package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanWindowEngine(newRegistry())
        val item = newServicePlanWindow("code-965", "name-755")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
