package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServicePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServicePlanTargetEngine(newRegistry())
        val item = newPendingServicePlanTarget("code-926", "name-302")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
