package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTrailerYardForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTrailerYardForecastEngine()
        val item = newRegionalTrailerYardForecast("code-401", "title-475")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
