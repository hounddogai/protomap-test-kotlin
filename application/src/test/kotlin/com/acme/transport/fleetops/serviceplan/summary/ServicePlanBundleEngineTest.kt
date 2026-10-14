package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanBundleEngine(newRegistry())
        val item = newServicePlanBundle("code-601", "name-712")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
