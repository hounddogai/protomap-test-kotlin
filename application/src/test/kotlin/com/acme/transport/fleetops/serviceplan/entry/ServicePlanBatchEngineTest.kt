package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanBatchEngine(newRegistry())
        val item = newServicePlanBatch("code-636", "label-123")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
