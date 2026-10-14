package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCameraEventMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCameraEventMetricEngine()
        val item = newLenientCameraEventMetric("code-39", "title-116")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
