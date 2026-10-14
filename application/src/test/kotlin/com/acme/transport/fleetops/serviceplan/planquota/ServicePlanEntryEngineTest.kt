package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanEntryEngine(newRegistry())
        val item = newServicePlanEntry("code-805", "title-193")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
