package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualNearMissForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualNearMissForecastEngine()
        val item = newManualNearMissForecast("code-933", "label-443")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
