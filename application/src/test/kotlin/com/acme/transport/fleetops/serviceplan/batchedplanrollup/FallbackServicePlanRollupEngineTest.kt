package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServicePlanRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServicePlanRollupEngine(newRegistry())
        val item = newFallbackServicePlanRollup("code-232", "label-883")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
