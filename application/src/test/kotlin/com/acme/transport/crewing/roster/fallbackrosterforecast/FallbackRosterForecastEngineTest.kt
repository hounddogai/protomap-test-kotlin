package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRosterForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRosterForecastEngine()
        val item = newFallbackRosterForecast("code-323", "title-810")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
