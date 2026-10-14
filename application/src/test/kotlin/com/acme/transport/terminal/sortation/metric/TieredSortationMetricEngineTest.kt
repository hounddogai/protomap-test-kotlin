package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredSortationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredSortationMetricEngine()
        val item = newTieredSortationMetric("code-859", "title-608")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
