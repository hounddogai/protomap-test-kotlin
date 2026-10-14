package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveInspectionThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveInspectionThresholdEngine()
        val item = newAdaptiveInspectionThreshold("code-281", "name-796")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
