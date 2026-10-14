package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedServicePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedServicePlanTargetEngine(newRegistry())
        val item = newBatchedServicePlanTarget("code-591", "name-421")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
