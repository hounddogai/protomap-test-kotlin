package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServicePlanPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServicePlanPreferenceEngine(newRegistry())
        val item = newPendingServicePlanPreference("code-735", "title-873")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
