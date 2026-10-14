package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanExceptionEngine(newRegistry())
        val item = newServicePlanException("code-134", "label-283")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
