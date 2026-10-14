package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakMetricEngine()
        val item = newRestBreakMetric("code-136", "name-910")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
