package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingMetricEngine()
        val item = newIdlingMetric("code-783", "title-935")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
