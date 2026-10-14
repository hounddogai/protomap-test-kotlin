package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveInspectionBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveInspectionBundleEngine()
        val item = newAdaptiveInspectionBundle("code-148", "name-831")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
