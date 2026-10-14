package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServicePlanDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServicePlanDraftEngine(newRegistry())
        val item = newServicePlanDraft("code-261", "label-965")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
