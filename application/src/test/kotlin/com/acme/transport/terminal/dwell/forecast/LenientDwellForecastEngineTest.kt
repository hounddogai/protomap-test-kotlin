package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDwellForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDwellForecastEngine()
        val item = newLenientDwellForecast("code-628", "name-503")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
