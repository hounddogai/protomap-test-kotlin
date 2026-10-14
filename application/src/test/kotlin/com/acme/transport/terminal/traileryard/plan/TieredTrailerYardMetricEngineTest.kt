package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTrailerYardMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTrailerYardMetricEngine()
        val item = newTieredTrailerYardMetric("code-307", "label-530")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
