package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCutoffForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCutoffForecastEngine()
        val item = newWeightedCutoffForecast("code-511", "name-218")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
