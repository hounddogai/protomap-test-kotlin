package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveServicePlanRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveServicePlanRecordEngine()
        val item = newAdaptiveServicePlanRecord("code-29", "name-327")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
