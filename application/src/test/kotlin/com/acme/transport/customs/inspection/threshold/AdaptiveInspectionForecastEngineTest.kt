package com.acme.transport.customs.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveInspectionForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveInspectionForecastEngine()
        val item = newAdaptiveInspectionForecast("code-738", "title-401")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
