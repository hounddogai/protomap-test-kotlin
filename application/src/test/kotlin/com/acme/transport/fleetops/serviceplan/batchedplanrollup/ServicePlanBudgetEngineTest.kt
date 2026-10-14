package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanBudgetEngine(newRegistry())
        val item = newServicePlanBudget("code-779", "label-852")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
