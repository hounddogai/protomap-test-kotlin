package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveInspectionEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveInspectionEstimateEngine(newRegistry())
        val item = newAdaptiveInspectionEstimate("code-307", "name-29")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
