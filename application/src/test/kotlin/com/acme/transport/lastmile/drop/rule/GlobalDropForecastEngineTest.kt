package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDropForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDropForecastEngine()
        val item = newGlobalDropForecast("code-294", "title-285")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
