package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffForecastEngine()
        val item = newCutoffForecast("code-679", "title-620")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
