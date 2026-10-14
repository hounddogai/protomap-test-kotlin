package com.acme.transport.network.cutoff.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCutoffForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCutoffForecastEngine()
        val item = newDefaultCutoffForecast("code-98", "label-565")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
