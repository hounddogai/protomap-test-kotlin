package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientInspectionEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientInspectionEstimateEngine()
        val item = newLenientInspectionEstimate("code-945", "title-390")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
