package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCameraEventMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCameraEventMetricEngine()
        val item = newStrictCameraEventMetric("code-254", "title-29")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
