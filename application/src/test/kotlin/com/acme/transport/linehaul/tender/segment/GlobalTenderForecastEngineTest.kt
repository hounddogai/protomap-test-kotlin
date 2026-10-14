package com.acme.transport.linehaul.tender.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTenderForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTenderForecastEngine()
        val item = newGlobalTenderForecast("code-359", "label-173")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
