package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServicePlanRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServicePlanRollupEngine()
        val item = newPendingServicePlanRollup("code-643", "title-572")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
